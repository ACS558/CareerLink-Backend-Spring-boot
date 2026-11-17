package com.careerlink.careerlink_backend.dto.response;

public record AcademicYearSummary(Integer academicYear, String label, long studentCount) {
    public static AcademicYearSummary of(Integer year, long count) {
        return new AcademicYearSummary(year, "A.Y. " + year + "-" + (year + 1), count);
    }
}
