package com.peti.backend.model.elastic.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Input data representing a booking that reduces available capacity for a time range.
 */
public record BookingInput(
    LocalDateTime timeFrom,
    LocalDateTime timeTo,
    int bookedCapacity
) {}

