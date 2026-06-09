package com.peti.backend.dto.caretaker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Breed inclusion/exclusion configuration using breed IDs")
public record BreedConfig(
    @Schema(description = "True = only listed breeds are accepted; false = listed breeds are excluded")
    boolean isIncluded,

    @Schema(description = "Breed IDs to include (when isIncluded=true)")
    @NotNull(message = "Include list must not be null")
    @Size(max = 100, message = "Cannot specify more than 100 included breeds")
    List<Integer> include,

    @Schema(description = "Breed IDs to exclude (when isIncluded=false)")
    @NotNull(message = "Exclude list must not be null")
    @Size(max = 100, message = "Cannot specify more than 100 excluded breeds")
    List<Integer> exclude
) {}

