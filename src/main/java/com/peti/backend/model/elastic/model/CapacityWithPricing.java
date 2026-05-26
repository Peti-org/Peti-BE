package com.peti.backend.model.elastic.model;

/**
 * Internal model combining available capacity with the ServiceConfig
 * that applies at a given timeline boundary.
 */
public record CapacityWithPricing(int capacity) {}

