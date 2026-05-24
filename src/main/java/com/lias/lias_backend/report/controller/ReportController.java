package com.lias.lias_backend.report.controller;

import com.lias.lias_backend.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/report/annual?year=2025
     * Generate and download the full annual activity report.
     * ADMIN or DIRECTOR only.
     */
    @GetMapping("/annual")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<byte[]> generateAnnualReport(@RequestParam int year) throws Exception {
        byte[] pdf = reportService.generateAnnualReport(year);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=LIAS_Annual_Report_" + year + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * GET /api/report/monthly?year=2025&month=3
     * Generate and download a monthly activity report.
     * ADMIN or DIRECTOR only.
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<byte[]> generateMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) throws Exception {
        byte[] pdf = reportService.generateMonthlyReport(year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=LIAS_Report_" + year + "_" + month + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}