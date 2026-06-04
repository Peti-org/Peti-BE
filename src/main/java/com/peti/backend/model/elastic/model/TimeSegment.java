package com.peti.backend.model.elastic.model;

import java.time.LocalDateTime;

/**
 * Internal model representing a discrete time segment with a fixed
 * available capacity and its applicable service configuration and rrule id to which it bounds.
 */
public record TimeSegment(
    LocalDateTime timeFrom,
    LocalDateTime timeTo,
    int capacity
) {}

