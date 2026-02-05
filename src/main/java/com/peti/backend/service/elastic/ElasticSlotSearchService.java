package com.peti.backend.service.elastic;

import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest.PetFilter;
import com.peti.backend.dto.elastic.ElasticSlotSearchResponse;
import com.peti.backend.dto.elastic.SlotSearchResult;
import com.peti.backend.dto.elastic.SlotSearchResult.CaretakerPreferencesSummary;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.ElasticSlotDocument.CaretakerPreferences;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import com.peti.backend.service.elastic.PriceCalculationService.PetInfo;
import com.peti.backend.service.elastic.PriceCalculationService.PriceBreakdown;
import com.peti.backend.service.elastic.PriceCalculationService.PriceCalculationResult;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSlotSearchService {

  private final ElasticSlotRepository slotRepository;
  private final PriceCalculationService priceCalculationService;
  private final ElasticsearchOperations elasticsearchOperations;

  /**
   * Search for available slots, picking best slot per caretaker.
   * Uses Elasticsearch aggregations for efficient filtering and grouping.
   * Calculates price based on requested duration and pets.
   */
  public ElasticSlotSearchResponse searchSlots(ElasticSlotSearchRequest rawRequest) {
    ElasticSlotSearchRequest request = rawRequest.withDefaults();
    
    log.debug("Searching slots with request: {}", request);
    
    // Build Elasticsearch query with all filters
    Query query = buildSearchQuery(request);
    
    // Build aggregation to group by caretaker and get best slot per caretaker
    // We fetch more buckets than needed to account for price filtering
    int bucketsToFetch = (request.page() + 3) * request.pageSize();
    
    NativeQuery searchQuery = NativeQuery.builder()
        .withQuery(query)
        .withAggregation("caretakers", buildCaretakerAggregation(request, bucketsToFetch))
        .withMaxResults(0) // We only need aggregations, not individual documents
        .build();
    
    // Execute search
    SearchHits<ElasticSlotDocument> searchHits = elasticsearchOperations.search(
        searchQuery, 
        ElasticSlotDocument.class
    );
    
    // Extract slots from aggregations
    List<ElasticSlotDocument> bestSlots = extractBestSlotsFromAggregations(searchHits);
    
    log.debug("Found {} best slots from aggregations", bestSlots.size());
    
    // Convert pets for price calculation
    List<PetInfo> petInfos = request.pets() != null 
        ? request.pets().stream()
            .map(p -> new PetInfo(p.animalType(), p.size()))
            .toList()
        : List.of(new PetInfo("dog", "medium")); // default pet
    
    // Calculate prices and filter out slots without valid prices
    List<SlotSearchResult> results = bestSlots.stream()
        .map(slot -> toSearchResult(slot, petInfos))
        .filter(result -> result.priceBreakdown() != null) // Only include slots with valid price
        .sorted(getComparator(request))
        .collect(Collectors.toList());
    
    // Apply pagination
    int start = request.page() * request.pageSize();
    int end = Math.min(start + request.pageSize(), results.size());
    
    List<SlotSearchResult> pagedResults = start < results.size() 
        ? results.subList(start, end) 
        : List.of();
    
    return ElasticSlotSearchResponse.of(pagedResults, request.page(), request.pageSize(), results.size());
  }

  /**
   * Build Elasticsearch query with all filters applied.
   * This pushes filtering to Elasticsearch instead of doing it in Java.
   */
  private Query buildSearchQuery(ElasticSlotSearchRequest request) {
    List<Query> mustQueries = new ArrayList<>();
    
    // Date range filter (required)
    mustQueries.add(Query.of(q -> q.range(r -> r.date(d -> d
        .field("date")
        .gte(request.dateFrom().format(DateTimeFormatter.ISO_LOCAL_DATE))
        .lte(request.dateTo().format(DateTimeFormatter.ISO_LOCAL_DATE))
    ))));
    
    // Time filters
    if (request.timeFrom() != null) {
      mustQueries.add(Query.of(q -> q.range(r -> r.term(t -> t
          .field("timeFrom")
          .lte(request.timeFrom().toString())
      ))));
    }
    
    if (request.timeTo() != null) {
      mustQueries.add(Query.of(q -> q.range(r -> r.term(t -> t
          .field("timeTo")
          .gte(request.timeTo().toString())
      ))));
    }
    
    // City filter
    if (request.cityId() != null) {
      mustQueries.add(Query.of(q -> q.term(t -> t
          .field("caretakerCityId")
          .value(FieldValue.of(request.cityId()))
      )));
    }
    
    // Rating filter
    if (request.minRating() != null) {
      mustQueries.add(Query.of(q -> q.range(r -> r.number(n -> n
          .field("caretakerRating")
          .gte((double)request.minRating())
      ))));
    }
    
    // Capacity filter - must fit all pets
    int minCapacity = request.pets() != null ? request.pets().size() : 1;
    mustQueries.add(Query.of(q -> q.range(r -> r.number(n -> n
        .field("capacity")
        .gte((double)minCapacity)
    ))));
    
    // Pet preferences filters
    if (request.pets() != null && !request.pets().isEmpty()) {
      addPetPreferenceFilters(mustQueries, request);
    }
    
    // Build bool query
    return Query.of(q -> q.bool(BoolQuery.of(b -> b.must(mustQueries))));
  }

  /**
   * Add pet preference filters to query.
   * Filters by animal type, size, weight, and special needs.
   */
  private void addPetPreferenceFilters(List<Query> mustQueries, ElasticSlotSearchRequest request) {
    List<Query> petFilters = new ArrayList<>();
    
    for (PetFilter pet : request.pets()) {
      List<Query> singlePetFilters = new ArrayList<>();
      
      // Animal type filter
      if (pet.animalType() != null) {
        singlePetFilters.add(Query.of(q -> q.bool(BoolQuery.of(b -> b
            .should(Query.of(sq -> sq.term(t -> t
                .field("caretakerPreferences.acceptedAnimalTypes")
                .value(FieldValue.of(pet.animalType()))
            )))
            .should(Query.of(sq -> sq.term(t -> t
                .field("caretakerPreferences.acceptedAnimalTypes")
                .value(FieldValue.of("all"))
            )))
            .minimumShouldMatch("1")
        ))));
      }
      
      // Size filter
      if (pet.size() != null) {
        singlePetFilters.add(Query.of(q -> q.bool(BoolQuery.of(b -> b
            .should(Query.of(sq -> sq.term(t -> t
                .field("caretakerPreferences.acceptedSizes")
                .value(FieldValue.of(pet.size()))
            )))
            .should(Query.of(sq -> sq.term(t -> t
                .field("caretakerPreferences.acceptedSizes")
                .value(FieldValue.of("all"))
            )))
            .minimumShouldMatch("1")
        ))));
      }
      
      // Weight filter
      if (pet.weightKg() != null) {
        singlePetFilters.add(Query.of(q -> q.range(r -> r.number(n -> n
            .field("caretakerPreferences.maxWeightKg")
            .gte(pet.weightKg())
        ))));
      }
      
      // Special needs filter
      if (Boolean.TRUE.equals(pet.hasSpecialNeeds())) {
        singlePetFilters.add(Query.of(q -> q.term(t -> t
            .field("caretakerPreferences.acceptsSpecialNeeds")
            .value(FieldValue.of(true))
        )));
      }
      
      if (!singlePetFilters.isEmpty()) {
        petFilters.add(Query.of(q -> q.bool(BoolQuery.of(b -> b.must(singlePetFilters)))));
      }
    }
    
    // Apply pet match mode
    if (!petFilters.isEmpty()) {
      if (request.petMatchMode() == ElasticSlotSearchRequest.PetMatchMode.ALL) {
        // All pets must match - add all filters to must
        mustQueries.addAll(petFilters);
      } else {
        // At least one pet must match - use should with minimum 1
        mustQueries.add(Query.of(q -> q.bool(BoolQuery.of(b -> b
            .should(petFilters)
            .minimumShouldMatch("1")
        ))));
      }
    }
  }

  /**
   * Build aggregation to group by caretaker and get best slot per caretaker.
   * Uses Terms aggregation with Top Hits sub-aggregation.
   */
  private Aggregation buildCaretakerAggregation(ElasticSlotSearchRequest request, int size) {
    // Build top hits aggregation to get best slot per caretaker
    // Sort by capacity (desc) and duration (desc) to get the best slot
    TopHitsAggregation topHitsAgg = TopHitsAggregation.of(th -> th
        .size(1) // Only take the best slot
        .sort(s -> s.field(f -> f.field("capacity").order(SortOrder.Desc)))
        .sort(s -> s.field(f -> f.field("durationMinutes").order(SortOrder.Desc)))
    );
    
    // Build terms aggregation to group by caretaker
    return Aggregation.of(a -> a
        .terms(TermsAggregation.of(t -> t
            .field("caretakerId.keyword")
            .size(size)
        ))
        .aggregations("best_slot", Aggregation.of(sa -> sa.topHits(topHitsAgg)))
    );
  }

  /**
   * Extract best slots from aggregation results.
   */
  private List<ElasticSlotDocument> extractBestSlotsFromAggregations(SearchHits<ElasticSlotDocument> searchHits) {
    List<ElasticSlotDocument> bestSlots = new ArrayList<>();
    
    if (searchHits.hasAggregations()) {
      var aggregationsContainer = searchHits.getAggregations();
      
      if (aggregationsContainer instanceof ElasticsearchAggregations esAggs) {
        // Access aggregations map - Spring Data wraps each agg as ElasticsearchAggregation
        var springAggregations = esAggs.aggregationsAsMap();
        var caretakersWrapper = springAggregations.get("caretakers");
        
        if (caretakersWrapper != null) {
          // Get native Elasticsearch aggregate from wrapper
          var caretakersAggregate = caretakersWrapper.aggregation().getAggregate();
          
          if (caretakersAggregate.isSterms()) {
            var buckets = caretakersAggregate.sterms().buckets().array();
            
            for (var bucket : buckets) {
              var bestSlotAggregate = bucket.aggregations().get("best_slot");
              
              if (bestSlotAggregate != null && bestSlotAggregate.isTopHits()) {
                var hits = bestSlotAggregate.topHits().hits().hits();
                
                if (!hits.isEmpty()) {
                  var hit = hits.get(0);
                  // Convert JsonData source to ElasticSlotDocument
                  String json = hit.source().toJson().toString();
                  ElasticSlotDocument slot = elasticsearchOperations
                      .getElasticsearchConverter()
                      .read(ElasticSlotDocument.class, 
                          org.springframework.data.elasticsearch.core.document.Document.parse(json));
                  
                  // Set the ID from hit
                  slot.setId(hit.id());
                  bestSlots.add(slot);
                }
              }
            }
          }
        }
      }
    }
    
    return bestSlots;
  }

  /**
   * Get all slots (for debugging/admin purposes).
   */
  public List<ElasticSlotDocument> getAllSlots() {
    return StreamSupport.stream(slotRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  /**
   * Get slot by ID.
   */
  public Optional<ElasticSlotDocument> getSlotById(String id) {
    return slotRepository.findById(id);
  }

  /**
   * Get slots for a caretaker.
   */
  public List<ElasticSlotDocument> getSlotsByCaretaker(String caretakerId) {
    return slotRepository.findByCaretakerIdAndDateBetween(
        caretakerId, 
        LocalDate.now(), 
        LocalDate.now().plusDays(30)
    );
  }

  /**
   * Save a slot (for testing purposes).
   */
  public ElasticSlotDocument saveSlot(ElasticSlotDocument slot) {
    return slotRepository.save(slot);
  }

  /**
   * Delete a slot.
   */
  public void deleteSlot(String id) {
    slotRepository.deleteById(id);
  }

  /**
   * Delete all slots for a caretaker on a specific date.
   * Used for regenerating slots after booking.
   */
  public void deleteSlotsByCaretakerAndDate(String caretakerId, LocalDate date) {
    log.info("Deleting all slots for caretaker {} on date {}", caretakerId, date);
    slotRepository.deleteByCaretakerIdAndDate(caretakerId, date);
  }

  /**
   * Bulk save slots.
   * More efficient than saving one by one.
   */
  public List<ElasticSlotDocument> bulkSaveSlots(List<ElasticSlotDocument> slots) {
    log.info("Bulk saving {} slots", slots.size());
    return StreamSupport.stream(
        slotRepository.saveAll(slots).spliterator(), 
        false
    ).collect(Collectors.toList());
  }

  /**
   * Replace all slots for a caretaker on a specific date.
   * Deletes old slots and creates new ones in a single operation.
   * This is the recommended way to update slots after booking.
   */
  public List<ElasticSlotDocument> replaceSlotsByCaretakerAndDate(
      String caretakerId, 
      LocalDate date, 
      List<ElasticSlotDocument> newSlots
  ) {
    log.info("Replacing all slots for caretaker {} on date {} with {} new slots", 
        caretakerId, date, newSlots.size());
    
    // Delete old slots
    deleteSlotsByCaretakerAndDate(caretakerId, date);
    
    // Save new slots
    return bulkSaveSlots(newSlots);
  }

  /**
   * Count all slots.
   */
  public long countSlots() {
    return slotRepository.count();
  }

  // === Private helper methods ===

  /**
   * Convert slot to search result with calculated price.
   */
  private SlotSearchResult toSearchResult(ElasticSlotDocument slot, List<PetInfo> pets) {
    PriceCalculationResult priceResult = priceCalculationService.calculatePrice(slot, pets);
    PriceBreakdown breakdown = priceResult.success() ? priceResult.breakdown() : null;
    
    if (!priceResult.success()) {
      log.debug("Price calculation failed for slot {}: {}", slot.getId(), priceResult.error());
    }
    
    return new SlotSearchResult(
        slot.getId(),
        slot.getDate(),
        slot.getTimeFrom(),
        slot.getTimeTo(),
        slot.getCapacity(),
        slot.getDurationMinutes(),
        slot.getCaretakerId(),
        slot.getCaretakerFirstName(),
        slot.getCaretakerLastName(),
        slot.getCaretakerRating(),
        slot.getCaretakerCityId(),
        slot.getCaretakerCityName(),
        buildPreferencesSummary(slot.getCaretakerPreferences()),
        breakdown
    );
  }

  /**
   * Build preferences summary from caretaker preferences.
   */
  private CaretakerPreferencesSummary buildPreferencesSummary(CaretakerPreferences prefs) {
    if (prefs == null) {
      return new CaretakerPreferencesSummary(List.of(), List.of(), null, null, null, null);
    }
    return new CaretakerPreferencesSummary(
        prefs.getAcceptedAnimalTypes() != null ? prefs.getAcceptedAnimalTypes() : List.of(),
        prefs.getAcceptedSizes() != null ? prefs.getAcceptedSizes() : List.of(),
        prefs.getMaxWeightKg(),
        prefs.getMaxPetsAtOnce(),
        prefs.getHasOutdoorSpace(),
        prefs.getAcceptsSpecialNeeds()
    );
  }

  /**
   * Get comparator based on sort request.
   */
  private Comparator<SlotSearchResult> getComparator(ElasticSlotSearchRequest request) {
    Comparator<SlotSearchResult> comparator = switch (request.sortBy()) {
      case RATING -> Comparator.comparing(SlotSearchResult::caretakerRating, Comparator.nullsLast(Comparator.naturalOrder()));
      case PRICE -> Comparator.comparing(
          r -> r.priceBreakdown() != null ? r.priceBreakdown().total() : null, 
          Comparator.nullsLast(Comparator.naturalOrder())
      );
      case AVAILABILITY -> Comparator.comparing(SlotSearchResult::durationMinutes, Comparator.nullsLast(Comparator.naturalOrder()));
      case DATE -> Comparator.comparing(SlotSearchResult::date, Comparator.nullsLast(Comparator.naturalOrder()));
    };
    
    return request.sortDirection() == ElasticSlotSearchRequest.SortDirection.DESC 
        ? comparator.reversed() 
        : comparator;
  }
}
