package com.peti.backend.model.elastic.model;

import java.time.LocalTime;

/**
 * A continuous time range with a start and end time.
 */
public record TimeRange(LocalTime timeFrom, LocalTime timeTo) {}

