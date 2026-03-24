package com.peti.backend.model.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceType {

  WALKING("walking"),
  SITTING("sitting"),
  TRAINING("training"),
  GROOMING("grooming"),
  VET("vet"),
  UNDEFINED("undefined");

  private static final Map<String, ServiceType> NAME_TO_SERVICE = Collections.unmodifiableMap(
      initializeNameToServiceMap());

  private final String name;

  private static Map<String, ServiceType> initializeNameToServiceMap() {
    Map<String, ServiceType> map = new HashMap<>();
    for (ServiceType service : values()) {
      map.put(service.name.toLowerCase(), service);
    }
    return map;
  }

  public static ServiceType fromName(String name) {
    if (name == null) {
      return null;
    }
    return NAME_TO_SERVICE.getOrDefault(name.toLowerCase(), UNDEFINED);
  }
}
