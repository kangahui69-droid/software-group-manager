package com.softwaregroup.monitor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/** Beijing wall-clock bounds for operation_log.created_at: [start, end). */
public final class LogTimeRange {
    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");
    private final LocalDateTime start;
    private final LocalDateTime end;

    private LogTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        this.start = start;
        this.end = end;
    }

    public static LogTimeRange explicit(String startTime, String endTime) {
        return new LogTimeRange(parseTime(startTime), parseTime(endTime));
    }

    public static LogTimeRange legacy(String dateRange) {
        if (dateRange == null || dateRange.isBlank()) return new LogTimeRange(null, null);
        String value = dateRange.trim();
        if (value.matches("[0-9]{1,4}")) {
            int days = Integer.parseInt(value);
            if (days < 1 || days > 3650) throw new IllegalArgumentException("Invalid dateRange days");
            return new LogTimeRange(LocalDateTime.now(BEIJING).minusDays(days), null);
        }
        try {
            LocalDate day = LocalDate.parse(value);
            return new LogTimeRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid dateRange", ex);
        }
    }

    private static LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Expected Beijing local time yyyy-MM-ddTHH:mm[:ss]", ex);
        }
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
