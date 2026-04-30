package com.peti.backend.model.elastic.model;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;

/**
 * Internal model combining available capacity with the ServiceConfig
 * that applies at a given timeline boundary.
 */
public record CapacityWithPricing(int capacity, ServiceConfig serviceConfig) {}

