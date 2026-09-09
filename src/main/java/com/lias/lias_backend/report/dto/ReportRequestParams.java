package com.lias.lias_backend.report.dto;

import lombok.Data;

@Data
public class ReportRequestParams {
    private int year;
    private Integer month; // null = annual report
}