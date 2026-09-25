package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.entity.Application;
import com.careerlink.careerlink_backend.entity.Job;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.enums.PlacementStatus;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.repository.ApplicationRepository;
import com.careerlink.careerlink_backend.repository.JobRepository;
import com.careerlink.careerlink_backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final StudentRepository studentRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public byte[] exportStudentsReport(String branch, String placementStatus) {
        List<Student> students = studentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            CellStyle headerStyle = boldHeaderStyle(workbook);

            String[] headers = {
                    "Registration Number", "First Name", "Last Name", "Email",
                    "Branch", "CGPA", "Backlogs", "Graduation Year",
                    "Placement Status", "Placed Company", "Placed Package (LPA)"
            };
            writeHeaderRow(sheet, headers, headerStyle);

            int rowNum = 1;
            for (Student s : students) {
                if (branch != null && !branch.isBlank()
                        && (s.getAcademicInfo() == null || !branch.equalsIgnoreCase(s.getAcademicInfo().getBranch()))) {
                    continue;
                }
                if (placementStatus != null && !placementStatus.isBlank()
                        && s.getPlacementStatus() != PlacementStatus.valueOf(placementStatus)) {
                    continue;
                }

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(s.getRegistrationNumber());
                row.createCell(1).setCellValue(safe(s.getPersonalInfo() != null ? s.getPersonalInfo().getFirstName() : null));
                row.createCell(2).setCellValue(safe(s.getPersonalInfo() != null ? s.getPersonalInfo().getLastName() : null));
                row.createCell(3).setCellValue(s.getUser() != null ? s.getUser().getEmail() : "");
                row.createCell(4).setCellValue(safe(s.getAcademicInfo() != null ? s.getAcademicInfo().getBranch() : null));
                row.createCell(5).setCellValue(s.getAcademicInfo() != null && s.getAcademicInfo().getCgpa() != null ? s.getAcademicInfo().getCgpa() : 0.0);
                row.createCell(6).setCellValue(s.getAcademicInfo() != null && s.getAcademicInfo().getBacklogs() != null ? s.getAcademicInfo().getBacklogs() : 0);
                row.createCell(7).setCellValue(s.getAcademicInfo() != null && s.getAcademicInfo().getGraduationYear() != null ? s.getAcademicInfo().getGraduationYear() : 0);
                row.createCell(8).setCellValue(s.getPlacementStatus().name());
                row.createCell(9).setCellValue(safe(s.getPlacedCompany()));
                row.createCell(10).setCellValue(s.getPlacedPackage() != null ? s.getPlacedPackage() : 0.0);
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate students report: " + e.getMessage(), e);
        }
    }

    public byte[] exportApplicantsForJob(Long jobId, String statusFilter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        List<Application> applications = applicationRepository.findByJobIdOrderByAtsScore_ScoreDesc(jobId);

        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
            var filterStatus = com.careerlink.careerlink_backend.entity.enums.ApplicationStatus.valueOf(statusFilter.toUpperCase());
            applications = applications.stream().filter(a -> a.getStatus() == filterStatus).toList();
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(safeSheetName(job.getTitle()));
            CellStyle headerStyle = boldHeaderStyle(workbook);

            String[] headers = {"Registration Number", "Name", "Branch", "CGPA", "ATS Score", "Status", "Applied At"};
            writeHeaderRow(sheet, headers, headerStyle);

            int rowNum = 1;
            for (Application app : applications) {
                Student s = app.getStudent();
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(s.getRegistrationNumber());
                row.createCell(1).setCellValue(
                        s.getPersonalInfo() != null
                                ? safe(s.getPersonalInfo().getFirstName()) + " " + safe(s.getPersonalInfo().getLastName())
                                : ""
                );
                row.createCell(2).setCellValue(safe(s.getAcademicInfo() != null ? s.getAcademicInfo().getBranch() : null));
                row.createCell(3).setCellValue(s.getAcademicInfo() != null && s.getAcademicInfo().getCgpa() != null ? s.getAcademicInfo().getCgpa() : 0.0);
                row.createCell(4).setCellValue(app.getAtsScore() != null && app.getAtsScore().getScore() != null ? app.getAtsScore().getScore() : 0.0);
                row.createCell(5).setCellValue(app.getStatus().name());
                row.createCell(6).setCellValue(app.getAppliedAt() != null ? app.getAppliedAt().toString() : "");
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate applicants report: " + e.getMessage(), e);
        }
    }

    private CellStyle boldHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void writeHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) sheet.autoSizeColumn(i);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeSheetName(String name) {
        // Excel sheet names can't exceed 31 chars or contain certain characters
        String cleaned = name.replaceAll("[\\\\/*\\[\\]:?]", "");
        return cleaned.length() > 28 ? cleaned.substring(0, 28) : cleaned;
    }
}