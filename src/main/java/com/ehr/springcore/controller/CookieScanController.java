package com.ehr.springcore.controller;

import com.ehr.springcore.model.CookieRequest;
import com.ehr.springcore.model.CookieScanResponse;
import com.ehr.springcore.service.CookieScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cookies")
@Tag(name = "Cookie Scanner", description = "Website cookie scanning endpoints for news websites")
@SecurityRequirement(name = "X-API-Key")
public class CookieScanController {

    private final CookieScanService cookieScanService;

    public CookieScanController(CookieScanService cookieScanService) {
        this.cookieScanService = cookieScanService;
    }

    @PostMapping("/scan")
    @Operation(
        summary = "Scan a website for cookies",
        description = "Scans a website (typically news sites) and extracts all cookies set by the site. " +
                     "This is useful for analyzing cookie usage for customer websites.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Scan completed (check success field for actual result)",
                content = @Content(schema = @Schema(implementation = CookieScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    @Parameter(name = "X-API-Key", description = "API Key for authentication", required = true, in = ParameterIn.HEADER)
    public ResponseEntity<CookieScanResponse> scanWebsite(@Valid @RequestBody CookieRequest request) {
        CookieScanResponse response = cookieScanService.scanWebsiteForCookies(request);
        return ResponseEntity.ok(response);
    }
}
