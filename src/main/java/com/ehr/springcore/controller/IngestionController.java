package com.ehr.springcore.controller;

import com.ehr.springcore.model.EventRequest;
import com.ehr.springcore.model.IngestResponse;
import com.ehr.springcore.model.MetricRequest;
import com.ehr.springcore.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingest")
@Tag(name = "Ingestion", description = "Analytics ingestion endpoints")
@SecurityRequirement(name = "X-API-Key")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/events")
    @Operation(
        summary = "Ingest an event",
        description = "Accepts and stores analytics events",
        responses = {
            @ApiResponse(responseCode = "200", description = "Event ingested successfully",
                content = @Content(schema = @Schema(implementation = IngestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    @Parameter(name = "X-API-Key", description = "API Key for authentication", required = true, in = ParameterIn.HEADER)
    public ResponseEntity<IngestResponse> ingestEvent(@Valid @RequestBody EventRequest event) {
        ingestionService.ingestEvent(event);
        return ResponseEntity.ok(new IngestResponse("success", "Event ingested successfully"));
    }

    @PostMapping("/metrics")
    @Operation(
        summary = "Ingest a metric",
        description = "Accepts and stores analytics metrics",
        responses = {
            @ApiResponse(responseCode = "200", description = "Metric ingested successfully",
                content = @Content(schema = @Schema(implementation = IngestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    @Parameter(name = "X-API-Key", description = "API Key for authentication", required = true, in = ParameterIn.HEADER)
    public ResponseEntity<IngestResponse> ingestMetric(@Valid @RequestBody MetricRequest metric) {
        ingestionService.ingestMetric(metric);
        return ResponseEntity.ok(new IngestResponse("success", "Metric ingested successfully"));
    }
}
