package com.peti.backend.controller.content;

import com.peti.backend.dto.stats.PlatformStatisticsDto;
import com.peti.backend.service.content.PlatformStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Platform statistics")
public class StatController {

  private final PlatformStatisticsService statisticsService;

  @GetMapping
  @Operation(summary = "Get platform statistics", description = "Returns cached platform statistics")
  public ResponseEntity<PlatformStatisticsDto> getStatistics() {
    return ResponseEntity.ok(statisticsService.getStatistics());
  }
}

