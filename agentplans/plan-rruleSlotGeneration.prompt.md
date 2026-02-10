# Plan: RRULE-based Automated Slot Generation

Implement a scheduled job that generates caretaker slots for the next 14 days based on RRULE configurations, with manual admin trigger capability and full test coverage.

## Steps

1. **Add RRULE parsing library** to `pom.xml` — Add `rrule4j` dependency for RFC 5545 RRULE parsing and date occurrence generation.

2. **Create `RRuleSlotGenerator` service** in `com.peti.backend.service` — Implement core logic to parse RRules from `CaretakerRRule` entities, generate date occurrences for next 14 days, check existing slots in `SlotRepository`, and create only missing slots using `SlotService` methods.

3. **Create `SlotGenerationScheduler` component** in `com.peti.backend.service` — Add `@Scheduled(cron = "0 0 2 * * *")` method that runs daily at 2 AM, fetches all active RRules via `CaretakerRRuleRepository`, and delegates to `RRuleSlotGenerator` with error handling and logging.

4. **Add admin endpoint** in `SlotController` or new controller — Create `POST /api/admin/slots/generate` endpoint with `@HasAdminRole` annotation to manually trigger slot generation job.

5. **Implement efficient slot checking** in `SlotRepository` — Add query method `existsByCaretakerIdAndDateAndTimeFromAndTimeTo()` or batch check using `findAllByCaretaker_CaretakerIdAndDateBetween()` to avoid duplicate slot creation.

6. **Write comprehensive tests** — Create `RRuleSlotGeneratorTest` with mocks testing RRULE parsing, date generation, duplicate detection; `SlotGenerationSchedulerTest` for scheduled execution; integration test for admin endpoint with security; verify batch operations don't overload memory/CPU.

## Further Considerations

1. **RRULE time information** — RRules in `CaretakerRRule` only store recurrence pattern and dates. Need to add `time_from`, `time_to`, `slot_duration`, `capacity`, `price`, `type` columns to generate complete `Slot` entities, or store as JSON in existing fields?
   `capacity`, `price` - need to be mocked for now. `type` must be added to rrule table and make alll nececari changes with tast also
2. **Batch size optimization** — Should process all caretakers at once or batch by chunks (e.g., 100 caretakers per transaction) to prevent memory issues with large datasets?
must be batch optimistation
3. **Enable scheduling configuration** — Add `@EnableScheduling` to main application class and allow cron expression configuration via `application.properties` for environment-specific scheduling?
cron must be configured wia application.properties
## Current Project Context

### Existing Structure
- **Entity**: `CaretakerRRule` with fields: `rruleId`, `caretaker`, `rrule` (String), `dtstart`, `dtend`, `description`, `createdAt`
- **Entity**: `Slot` with fields: `slotId`, `caretaker`, `date`, `timeFrom`, `timeTo`, `type`, `price`, `currency`, `capacity`, `occupiedCapacity`, `available`, `creationTime`
- **Repository**: `CaretakerRRuleRepository` with `findAllByCaretaker_CaretakerId(UUID)`
- **Repository**: `SlotRepository` with `findAllByCaretaker_CaretakerIdAndDateBetween(UUID, Date, Date)`
- **Service**: `CaretakerRRuleService` - CRUD operations for RRules
- **Service**: `SlotService` - slot creation with time division using `SlotDivider`
- **Controller**: `SlotController` - exposes RRule endpoints at `/api/slots/rrules` (CARETAKER role)

### Key Patterns
- Using Lombok for entities (`@Getter`, `@Setter`, `@EqualsAndHashCode`)
- Mock-based unit tests with `ResourceLoader` for JSON test data
- Security annotations: `@HasAdminRole`, `@HasCaretakerRole`, `@HasUserRole`
- Services use `@Transactional` for data modifications
- Entity references via `EntityManager.getReference()` to avoid unnecessary queries

### Example RRULE Format
```json
{
  "rrule": "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR",
  "dtstart": "2026-02-01T09:00:00",
  "dtend": "2026-12-31T17:00:00",
  "description": "Weekdays 9am-5pm"
}
```

## Implementation Notes

### Required Changes to CaretakerRRule Schema
The current `CaretakerRRule` entity only stores the recurrence pattern but lacks information needed to generate complete slots. We need to extend it with:
- `time_from` (Time) - Daily start time
- `time_to` (Time) - Daily end time
- `slot_duration_minutes` (Integer) - Duration of each slot (e.g., 30)
- `slot_type` (String) - Type of service
- `price` (BigDecimal) - Price per slot
- `currency` (String) - Currency code
- `capacity` (Integer) - Max capacity per slot

### Algorithm
1. For each active `CaretakerRRule`:
   - Parse RRULE string using library (e.g., `biweekly`)
   - Generate date occurrences between TODAY and TODAY+14 days
   - For each occurrence date:
     - Check if slots already exist for that date + time range (need to find other solution as there will be so many slots to check)
     - If not, create slots using `SlotService.divideAndCreateSlots()`
2. Batch process in chunks to avoid memory issues
3. Log generation statistics (slots created, skipped, errors)

### Testing Strategy
- **Unit tests**: Mock RRULE parsing, test date generation logic, verify duplicate detection
- **Integration tests**: Test verify scheduler runs, test admin endpoint security
- **Performance tests**: Generate slots for 100+ caretakers, measure memory/CPU usage
- **Test data**: Create sample RRules in test resources (daily, weekly, complex patterns)

## Acceptance Criteria
- [ ] Scheduled job runs daily at 2 AM without errors
- [ ] Job generates slots only for next 14 days
- [ ] No duplicate slots are created
- [ ] Admin can manually trigger job via API endpoint
- [ ] Job completes in reasonable time (<5 min for 1000 caretakers)
- [ ] Memory usage stays under acceptable limits
- [ ] All functionality covered by tests (>80% coverage)
- [ ] Code follows existing project style and patterns

