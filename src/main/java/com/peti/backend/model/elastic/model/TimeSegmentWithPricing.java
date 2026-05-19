package com.peti.backend.model.elastic.model;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Internal model representing a discrete time segment with a fixed
 * available capacity and its applicable service configuration and rrule id to which it bounds.
 */
public record TimeSegmentWithPricing(
    LocalDateTime timeFrom,
    LocalDateTime timeTo,
    int capacity
) {}

