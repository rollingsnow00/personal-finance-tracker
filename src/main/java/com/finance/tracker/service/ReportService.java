package com.finance.tracker.service;

import com.finance.tracker.dto.response.MonthlyReportResponse;

import java.time.YearMonth;

public interface ReportService {
    MonthlyReportResponse monthly(String username, YearMonth month, String currency);
}
