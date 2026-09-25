package com.careerlink.careerlink_backend.mapper;

import com.careerlink.careerlink_backend.dto.response.ApplicationResponse;
import com.careerlink.careerlink_backend.dto.response.AtsScoreResponse;
import com.careerlink.careerlink_backend.entity.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(Application app) {
        AtsScoreResponse atsResponse = null;
        if (app.getAtsScore() != null && app.getAtsScore().getScore() != null) {
            atsResponse = new AtsScoreResponse(
                    app.getAtsScore().getScore(),
                    app.getAtsScore().getStrengths(),
                    app.getAtsScore().getWeaknesses(),
                    app.getAtsScore().getRecommendation(),
                    app.getAtsScore().getOverallSummary()
            );
        }

        return new ApplicationResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getJob().getRecruiter().getCompanyInfo() != null
                        ? app.getJob().getRecruiter().getCompanyInfo().getCompanyName() : null,
                app.getStudent().getId(),
                app.getStudent().getPersonalInfo() != null
                        ? app.getStudent().getPersonalInfo().getFirstName() + " " + app.getStudent().getPersonalInfo().getLastName()
                        : null,
                app.getStudent().getRegistrationNumber(),
                app.getStatus().name(),
                app.getAppliedAt(),
                atsResponse,
                app.getJob().getLocation(),
                app.getJob().getJobType().name(),
                app.getJob().getWorkMode().name(),
                app.getJob().getSalaryMin(),
                app.getJob().getSalaryMax(),
                app.getJob().getSalaryType().name(),
                app.getShortlistedAt(),
                app.getSelectedAt(),
                app.getRejectionReason(),
                app.getRecruiterNotes(),
                app.getStudent().getAcademicInfo() != null ? app.getStudent().getAcademicInfo().getBranch() : null,
                app.getStudent().getAcademicInfo() != null ? app.getStudent().getAcademicInfo().getCgpa() : null,
                app.getStudent().getAcademicInfo() != null ? app.getStudent().getAcademicInfo().getGraduationYear() : null,
                app.getStudent().getResume() != null ? app.getStudent().getResume().getUrl() : null
        );
    }
}
