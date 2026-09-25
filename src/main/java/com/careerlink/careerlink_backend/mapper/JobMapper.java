package com.careerlink.careerlink_backend.mapper;

import com.careerlink.careerlink_backend.dto.response.JobResponse;
import com.careerlink.careerlink_backend.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getRecruiter().getId(),
                job.getRecruiter().getUser() != null ? job.getRecruiter().getUser().getEmail() : null,
                job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getCompanyName() : null,
                job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getCompanyLogoUrl() : null,
                // JobMapper.java — add matching values:
                job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getIndustry() : null,
                job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getCompanySize() : null,
                job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getWebsite() : null,
                job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getDescription() : null,
                job.getTitle(),
                job.getDescription(),
                job.getJobType().name(),
                job.getWorkMode().name(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryType().name(),
                job.getEligibleBranches(),
                job.getMinCgpa(),
                job.getMaxBacklogs(),
                job.getGraduationYears(),
                job.getSkillsRequired(),
                job.getNumberOfOpenings(),
                job.getApplicationDeadline(),
                job.isActive(),
                job.getApprovalStatus().name(),
                job.getCreatedAt()
        );
    }
}
