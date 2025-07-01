package com.peti.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
@Tag(name = "Maintenance", description = "Endpoint only needed for maintenance don't expose them publicly")
public class MaintenanceController {

  @GetMapping("/ping")
  public ResponseEntity<String> ping() {
    return ResponseEntity.ok("ok");
  }
}
