package com.peti.backend.controller.maintenance;

import com.peti.backend.dto.stats.PlatformStatisticsDto;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.service.content.PlatformStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminStatController {

  private final PlatformStatisticsService statisticsService;

  @HasAdminRole
  @PostMapping("/regenerate")
  @Operation(summary = "Force regenerate platform statistics")
  public ResponseEntity<PlatformStatisticsDto> regenerateStatistics() {
    return ResponseEntity.ok(statisticsService.regenerate());
  }
}

