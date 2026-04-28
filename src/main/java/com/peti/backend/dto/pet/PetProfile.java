package com.peti.backend.dto.pet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Pet profile configuration stored as JSON in the pet context field")
public record PetProfile(

    @Schema(description = "Pet weight in kg, null if unknown", example = "12.5")
    @DecimalMin(value = "0.01", message = "Weight must be positive")
    BigDecimal weightKg,

    @Schema(description = "Pet sex", example = "MALE", allowableValues = {"MALE", "FEMALE", "UNKNOWN"})
    @NotNull(message = "Sex must not be null")
    Sex sex,

    @Schema(description = "Sterilization status", example = "YES", allowableValues = {"YES", "NO", "UNKNOWN"})
    @NotNull(message = "Sterilization status must not be null")
    TriState sterilized,

    @Schema(description = "Vaccination status", example = "YES", allowableValues = {"YES", "NO", "UNKNOWN"})
    @NotNull(message = "Vaccination status must not be null")
    TriState vaccinated,

    @Schema(description = "Gets along with dogs", example = "YES", allowableValues = {"YES", "NO", "UNKNOWN"})
    @NotNull(message = "Gets along with dogs must not be null")
    TriState getsAlongWithDogs,

    @Schema(description = "Gets along with cats", example = "UNKNOWN", allowableValues = {"YES", "NO", "UNKNOWN"})
    @NotNull(message = "Gets along with cats must not be null")
    TriState getsAlongWithCats,

    @Schema(description = "Gets along with kids", example = "YES", allowableValues = {"YES", "NO", "UNKNOWN"})
    @NotNull(message = "Gets along with kids must not be null")
    TriState getsAlongWithKids,

    @Schema(description = "Known allergies, free text", example = "Chicken protein")
    @Size(max = 1000, message = "Allergies text must not exceed 1000 characters")
    String allergies,

    @Schema(description = "Medication and schedule, free text", example = "Apoquel 16mg daily at 8am")
    @Size(max = 1000, message = "Medicine text must not exceed 1000 characters")
    String medicineSchedule,

    @Schema(description = "Vet clinic info: address, phone, etc.", example = "VetClinic Kyiv, +380441234567")
    @Size(max = 1000, message = "Vet info must not exceed 1000 characters")
    String vetInfo,

    @Schema(description = "Additional details about the pet", example = "Afraid of fireworks")
    @Size(max = 2000, message = "Additional details must not exceed 2000 characters")
    String additionalDetails,

    @Schema(description = "Short description of the pet", example = "Friendly golden retriever, loves swimming")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description
) {

  public enum Sex {
    MALE, FEMALE, UNKNOWN
  }

  public enum TriState {
    YES, NO, UNKNOWN
  }
}

