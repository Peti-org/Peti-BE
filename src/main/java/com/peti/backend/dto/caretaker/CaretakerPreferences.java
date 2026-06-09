package com.peti.backend.dto.caretaker;

import com.peti.backend.model.internal.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.Map;

@Schema(description = "Caretaker service preferences and pricing configuration")
public record CaretakerPreferences(
    @Schema(description = "Services the caretaker offers, keyed by service type")
    @NotNull(message = "Services map must not be null")
    @Valid
    Map<ServiceType, ServiceConfig> services
) {

  public CaretakerPreferences {
    if (services == null) {
      services = new EnumMap<>(ServiceType.class);
    }
  }

  public ServiceConfig getService(ServiceType type) {
    return services.get(type);
  }

  public ServiceConfig getService(String typeName) {
    ServiceType type = ServiceType.fromName(typeName);
    return type != null ? services.get(type) : null;
  }
}

