package com.careerlink.careerlink_backend.mapper;

import com.careerlink.careerlink_backend.dto.response.JobResponse;
import com.careerlink.careerlink_backend.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobResponse toResponse(Job job) {
        // 1. Safeguard against null recruiter
        var recruiter = job.getRecruiter();

        Long recruiterId = (recruiter != null) ? recruiter.getId() : null;

        String email = (recruiter != null && recruiter.getUser() != null)
                ? recruiter.getUser().getEmail() : null;

        String companyName = (recruiter != null && recruiter.getCompanyInfo() != null)
                ? recruiter.getCompanyInfo().getCompanyName() : null;

        String logoUrl = (recruiter != null && recruiter.getCompanyInfo() != null)
                ? recruiter.getCompanyInfo().getCompanyLogoUrl() : null;

        String industry = (recruiter != null && recruiter.getCompanyInfo() != null)
                ? recruiter.getCompanyInfo().getIndustry() : null;

        String companySize = (recruiter != null && recruiter.getCompanyInfo() != null)
                ? recruiter.getCompanyInfo().getCompanySize() : null;

        String website = (recruiter != null && recruiter.getCompanyInfo() != null)
                ? recruiter.getCompanyInfo().getWebsite() : null;

        String description = (recruiter != null && recruiter.getCompanyInfo() != null)
                ? recruiter.getCompanyInfo().getDescription() : null;

        // 2. Return the new response with the safe variables
        return new JobResponse(
                job.getId(),
                recruiterId,
                email,
                companyName,
                logoUrl,
                industry,
                companySize,
                website,
                description,
                job.getTitle(),
                job.getDescription(),
                job.getJobType() != null ? job.getJobType().name() : null,
                job.getWorkMode() != null ? job.getWorkMode().name() : null,
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryType() != null ? job.getSalaryType().name() : null,
                job.getEligibleBranches(),
                job.getMinCgpa(),
                job.getMaxBacklogs(),
                job.getGraduationYears(),
                job.getSkillsRequired(),
                job.getNumberOfOpenings(),
                job.getApplicationDeadline(),
                job.isActive(),
                job.getApprovalStatus() != null ? job.getApprovalStatus().name() : null,
                job.getCreatedAt()
        );
    }
}
