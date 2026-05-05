package com.peti.backend.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.pet.PetProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeepCloner {

  private final ObjectMapper objectMapper;

  public CaretakerPreferences deepCopyPreference(CaretakerPreferences preferences) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(preferences);
      return objectMapper.readValue(bytes, new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException("Deep copy failed", e);
    }
  }

  public PetProfile deepCopyPetProfile(PetProfile petProfile) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(petProfile);
      return objectMapper.readValue(bytes, new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException("Deep copy failed", e);
    }
  }

}
