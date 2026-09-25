package com.careerlink.careerlink_backend.mapper;

import com.careerlink.careerlink_backend.dto.request.*;
import com.careerlink.careerlink_backend.dto.response.StudentResponse;
import com.careerlink.careerlink_backend.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getRegistrationNumber(),
                s.getUser().getEmail(),
                s.getPersonalInfo() != null ? s.getPersonalInfo().getFirstName() : null,
                s.getPersonalInfo() != null ? s.getPersonalInfo().getLastName() : null,
                s.getPersonalInfo() != null ? s.getPersonalInfo().getPhoneNumber() : null,
                s.getPersonalInfo() != null ? s.getPersonalInfo().getDateOfBirth() : null,
                s.getPersonalInfo() != null ? s.getPersonalInfo().getGender() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getDepartment() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getBranch() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getSemester() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getCgpa() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getPercentage() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getBacklogs() : null,
                s.getAcademicInfo() != null ? s.getAcademicInfo().getGraduationYear() : null,
                s.getSkills(),
                s.getResume() != null ? s.getResume().getUrl() : null,
                s.getResume() != null ? s.getResume().getUploadedAt() : null,
                s.getPersonalInfo() != null ? s.getPersonalInfo().getProfilePictureUrl() : null,
                s.getSocialLinks() != null ? s.getSocialLinks().getLinkedin() : null,
                s.getSocialLinks() != null ? s.getSocialLinks().getGithub() : null,
                s.getSocialLinks() != null ? s.getSocialLinks().getPortfolio() : null,
                s.getPlacementStatus().name(),
                s.isProfileCompleted(),
                s.getAccountStatus().name(),
                toProjectDtos(s.getProjects()),
                toInternshipDtos(s.getInternships()),
                toCertificationDtos(s.getCertifications()),
                toExternalLinkDtos(s.getExternalLinks())
        );
    }

    private List<ProjectDto> toProjectDtos(List<Project> projects) {
        return projects.stream()
                .map(p -> new ProjectDto(p.getTitle(), p.getDescription(), p.getTechnologies(), p.getLiveLink(), p.getGithubLink()))
                .collect(Collectors.toList());
    }

    private List<InternshipDto> toInternshipDtos(List<Internship> internships) {
        return internships.stream()
                .map(i -> new InternshipDto(i.getCompanyName(), i.getRole(), i.getDuration(), i.getStartDate(), i.getEndDate(), i.getDescription()))
                .collect(Collectors.toList());
    }

    private List<CertificationDto> toCertificationDtos(List<Certification> certifications) {
        return certifications.stream()
                .map(c -> new CertificationDto(c.getName(), c.getIssuedBy(), c.getIssueDate(), c.getCredentialUrl()))
                .collect(Collectors.toList());
    }

    private List<ExternalLinkDto> toExternalLinkDtos(List<ExternalLink> links) {
        return links.stream()
                .map(l -> new ExternalLinkDto(l.getName(), l.getUrl()))
                .collect(Collectors.toList());
    }
}
