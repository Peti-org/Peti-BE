package com.peti.backend.service.elastic.model;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import java.time.LocalTime;

/**
 * Internal model representing a discrete time segment with a fixed
 * available capacity and its applicable service configuration.
 */
public record TimeSegmentWithPricing(
    LocalTime timeFrom,
    LocalTime timeTo,
    int capacity,
    ServiceConfig serviceConfig
) {}

