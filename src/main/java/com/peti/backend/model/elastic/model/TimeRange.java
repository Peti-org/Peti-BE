package com.peti.backend.model.elastic.model;

import java.time.LocalDateTime;

/**
 * A continuous time range with a start and end time.
 */
public record TimeRange(LocalDateTime timeFrom, LocalDateTime timeTo) {}

