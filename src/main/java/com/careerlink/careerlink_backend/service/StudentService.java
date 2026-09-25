package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.*;
import com.careerlink.careerlink_backend.dto.response.ProfileCompletionResponse;
import com.careerlink.careerlink_backend.dto.response.StudentDashboardResponse;
import com.careerlink.careerlink_backend.dto.response.StudentPlacementResponse;
import com.careerlink.careerlink_backend.dto.response.StudentResponse;
import com.careerlink.careerlink_backend.entity.*;
import com.careerlink.careerlink_backend.entity.embeddable.AcademicInfo;
import com.careerlink.careerlink_backend.entity.embeddable.CloudinaryFile;
import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.embeddable.SocialLinks;
import com.careerlink.careerlink_backend.entity.enums.ExtensionStatus;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.exception.FileUploadException;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.mapper.StudentMapper;
import com.careerlink.careerlink_backend.mapper.StudentPlacementMapper;
import com.careerlink.careerlink_backend.repository.ExtensionRequestRepository;
import com.careerlink.careerlink_backend.repository.StudentPlacementRepository;
import com.careerlink.careerlink_backend.repository.StudentRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final CloudinaryService cloudinaryService;
    private final ResumeParsingService resumeParsingService;
    private final ExtensionRequestRepository extensionRequestRepository;
    private final NotificationService notificationService;
    private final StudentPlacementRepository studentPlacementRepository;
    private final StudentPlacementMapper studentPlacementMapper;

    public Student getCurrentStudent(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    public StudentResponse getProfile(Authentication auth) {
        return studentMapper.toResponse(getCurrentStudent(auth));
    }

    @Transactional
    public void deleteResume(Authentication auth) {
        Student student = getCurrentStudent(auth);
        if (student.getResume() == null || student.getResume().getPublicId() == null) {
            throw new ResourceNotFoundException("No resume to delete");
        }
        cloudinaryService.deleteFile(student.getResume().getPublicId(), "raw");
        student.setResume(null);
        student.setResumeText(null);
        student.setProfileCompleted(isProfileComplete(student));
        studentRepository.save(student);
    }

    @Transactional
    public void deleteProfilePhoto(Authentication auth) {
        Student student = getCurrentStudent(auth);
        PersonalInfo info = student.getPersonalInfo();
        if (info == null || info.getProfilePicturePublicId() == null) {
            throw new ResourceNotFoundException("No photo to delete");
        }
        cloudinaryService.deleteFile(info.getProfilePicturePublicId(), "image");
        info.setProfilePictureUrl(null);
        info.setProfilePicturePublicId(null);
        studentRepository.save(student);
    }

    @Transactional
    public StudentResponse updateProfile(Authentication auth, StudentProfileUpdateRequest req) {
        Student student = getCurrentStudent(auth);

        PersonalInfo personalInfo = student.getPersonalInfo() != null ? student.getPersonalInfo() : new PersonalInfo();
        personalInfo.setFirstName(req.firstName());
        personalInfo.setLastName(req.lastName());
        personalInfo.setPhoneNumber(req.phoneNumber());
        personalInfo.setDateOfBirth(req.dateOfBirth());
        personalInfo.setGender(req.gender());
        student.setPersonalInfo(personalInfo);

        AcademicInfo academicInfo = student.getAcademicInfo() != null ? student.getAcademicInfo() : new AcademicInfo();
        academicInfo.setDepartment(req.department());
        academicInfo.setBranch(req.branch());
        academicInfo.setSemester(req.semester());
        academicInfo.setCgpa(req.cgpa());
        academicInfo.setPercentage(req.percentage());
        academicInfo.setBacklogs(req.backlogs() != null ? req.backlogs() : 0);
        academicInfo.setGraduationYear(req.graduationYear());
        student.setAcademicInfo(academicInfo);

        if (req.skills() != null) {
            student.setSkills(req.skills());
        }

        SocialLinks socialLinks = student.getSocialLinks() != null ? student.getSocialLinks() : new SocialLinks();
        socialLinks.setLinkedin(req.linkedin());
        socialLinks.setGithub(req.github());
        socialLinks.setPortfolio(req.portfolio());
        student.setSocialLinks(socialLinks);

        replaceProjects(student, req.projects());
        replaceInternships(student, req.internships());
        replaceCertifications(student, req.certifications());
        replaceExternalLinks(student, req.externalLinks());

        student.setProfileCompleted(isProfileComplete(student));

        Student saved = studentRepository.save(student);
        return studentMapper.toResponse(saved);
    }

    @Transactional
    public StudentResponse uploadResume(Authentication auth, MultipartFile file) {
        Student student = getCurrentStudent(auth);

        if (file.isEmpty()) {
            throw new FileUploadException("No file provided");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new FileUploadException("Resume must be a PDF file");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileUploadException("Resume must not exceed 5MB");
        }

        String oldPublicId = student.getResume() != null ? student.getResume().getPublicId() : null;

        // 1. Upload new resume first
        CloudinaryFile uploaded = cloudinaryService.uploadFile(file, "careerlink/resumes", "raw");

        // 2. Extract text — if this fails, we still keep the newly uploaded file reference
        //    (better than losing both old and new), and surface the error to the caller.
        String extractedText;
        try {
            extractedText = resumeParsingService.extractText(file.getBytes());
        } catch (Exception e) {
            throw new FileUploadException("Uploaded, but failed to extract resume text: " + e.getMessage());
        }

        // 3. Update DB with the new resume + extracted text
        student.setResume(uploaded);
        student.setResumeText(extractedText);
        student.setProfileCompleted(isProfileComplete(student));

        Student saved = studentRepository.save(student);

        // 4. Delete old resume only after successful upload and DB update
        if (oldPublicId != null) {
            try {
                cloudinaryService.deleteFile(oldPublicId, "raw");
            } catch (Exception e) {
                log.warn("Failed to delete old resume for student {}: {}", student.getId(), e.getMessage(), e);
            }
        }

        return studentMapper.toResponse(saved);
    }
    @Transactional
    public StudentResponse uploadProfilePhoto(Authentication auth, MultipartFile file) {
        Student student = getCurrentStudent(auth);

        if (file.isEmpty()) {
            throw new FileUploadException("No file provided");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new FileUploadException("Profile photo must be an image file");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileUploadException("Profile photo must not exceed 5MB");
        }

        PersonalInfo personalInfo = student.getPersonalInfo() != null
                ? student.getPersonalInfo()
                : new PersonalInfo();

        String oldPublicId = personalInfo.getProfilePicturePublicId();

        // 1. Upload new photo first
        CloudinaryFile uploaded = cloudinaryService.uploadFile(
                file,
                "careerlink/student-photos",
                "image"
        );

        // 2. Update database with new photo
        personalInfo.setProfilePictureUrl(uploaded.getUrl());
        personalInfo.setProfilePicturePublicId(uploaded.getPublicId());
        student.setPersonalInfo(personalInfo);

        Student saved = studentRepository.save(student);

        // 3. Delete old photo only after successful upload and DB update
        if (oldPublicId != null) {
            try {
                cloudinaryService.deleteFile(oldPublicId, "image");
            } catch (Exception e) {
                log.warn("Failed to delete old profile photo for student {}: {}",
                        student.getId(), e.getMessage(), e);
            }
        }

        return studentMapper.toResponse(saved);
    }


    private void replaceProjects(Student student, List<ProjectDto> dtos) {
        student.getProjects().clear();
        if (dtos == null) return;
        for (ProjectDto dto : dtos) {
            Project p = new Project();
            p.setStudent(student);
            p.setTitle(dto.title());
            p.setDescription(dto.description());
            p.setTechnologies(dto.technologies() != null ? dto.technologies() : new ArrayList<>());
            p.setLiveLink(dto.liveLink());
            p.setGithubLink(dto.githubLink());
            student.getProjects().add(p);
        }
    }

    private void replaceInternships(Student student, List<InternshipDto> dtos) {
        student.getInternships().clear();
        if (dtos == null) return;
        for (InternshipDto dto : dtos) {
            Internship i = new Internship();
            i.setStudent(student);
            i.setCompanyName(dto.companyName());
            i.setRole(dto.role());
            i.setDuration(dto.duration());
            i.setStartDate(dto.startDate());
            i.setEndDate(dto.endDate());
            i.setDescription(dto.description());
            student.getInternships().add(i);
        }
    }

    private void replaceCertifications(Student student, List<CertificationDto> dtos) {
        student.getCertifications().clear();
        if (dtos == null) return;
        for (CertificationDto dto : dtos) {
            Certification c = new Certification();
            c.setStudent(student);
            c.setName(dto.name());
            c.setIssuedBy(dto.issuedBy());
            c.setIssueDate(dto.issueDate());
            c.setCredentialUrl(dto.credentialUrl());
            student.getCertifications().add(c);
        }
    }

    private void replaceExternalLinks(Student student, List<ExternalLinkDto> dtos) {
        student.getExternalLinks().clear();
        if (dtos == null) return;
        for (ExternalLinkDto dto : dtos) {
            ExternalLink l = new ExternalLink();
            l.setStudent(student);
            l.setName(dto.name());
            l.setUrl(dto.url());
            student.getExternalLinks().add(l);
        }
    }

    private boolean isProfileComplete(Student s) {
        return s.getPersonalInfo() != null
                && s.getPersonalInfo().getFirstName() != null
                && s.getAcademicInfo() != null
                && s.getAcademicInfo().getBranch() != null
                && s.getAcademicInfo().getCgpa() != null
                && s.getResume() != null;
    }

    @Transactional
    public void requestExtension(Authentication auth, ExtensionRequestSubmitRequest req) {
        Student student = getCurrentStudent(auth);

        if (extensionRequestRepository.existsByStudentIdAndStatus(student.getId(), ExtensionStatus.PENDING)) {
            throw new UnauthorizedException("You already have a pending extension request");
        }

        ExtensionRequest request = new ExtensionRequest();
        request.setStudent(student);
        request.setReason(req.reason().trim());
        request.setStatus(ExtensionStatus.PENDING);
        extensionRequestRepository.save(request);

        String studentName = student.getPersonalInfo() != null
                ? (student.getPersonalInfo().getFirstName() + " " + student.getPersonalInfo().getLastName())
                : student.getRegistrationNumber();

        notificationService.notifyAllAdmins(
                NotificationType.EXTENSION_REQUESTED,
                "New Extension Request",
                studentName + " (" + student.getRegistrationNumber() + ") has requested an account extension.",
                "/admin/extension-requests"
        );
    }

    public StudentDashboardResponse getDashboard(Authentication auth) {
        Student student = getCurrentStudent(auth);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expiry = student.getExpiryDate();
        long daysUntilExpiry = expiry != null ? Math.max(0, ChronoUnit.DAYS.between(now, expiry)) : 0;

        LocalDateTime guidanceStart = student.getCareerGuidanceStartDate();
        boolean inGuidanceMode = guidanceStart != null && expiry != null
                && !now.isBefore(guidanceStart) && now.isBefore(expiry);

        List<StudentPlacementResponse> placements = studentPlacementRepository.findByStudentId(student.getId())
                .stream()
                .sorted((a, b) -> {
                    if (a.isPrimary() != b.isPrimary()) return a.isPrimary() ? -1 : 1;
                    if (a.getOfferDate() == null || b.getOfferDate() == null) return 0;
                    return b.getOfferDate().compareTo(a.getOfferDate());
                })
                .map(studentPlacementMapper::toResponse)
                .toList();

        String name = student.getPersonalInfo() != null
                ? (safe(student.getPersonalInfo().getFirstName()) + " " + safe(student.getPersonalInfo().getLastName())).trim()
                : student.getRegistrationNumber();

        var info = new StudentDashboardResponse.StudentInfo(
                name, student.getUser().getEmail(), student.getRegistrationNumber(),
                student.getPlacedCompany(), student.getPlacedPackage(), placements, placements.size()
        );

        return new StudentDashboardResponse(
                inGuidanceMode ? "career_guidance" : "normal",
                daysUntilExpiry, student.getAccountStatus().name(),
                student.getPlacementStatus().name(), info
        );
    }

    public List<StudentPlacementResponse> getMyPlacements(Authentication auth) {
        Student student = getCurrentStudent(auth);
        return studentPlacementRepository.findByStudentId(student.getId()).stream()
                .sorted((a, b) -> {
                    if (a.isPrimary() != b.isPrimary()) return a.isPrimary() ? -1 : 1;
                    if (a.getOfferDate() == null || b.getOfferDate() == null) return 0;
                    return b.getOfferDate().compareTo(a.getOfferDate());
                })
                .map(studentPlacementMapper::toResponse)
                .toList();
    }

    public ProfileCompletionResponse getProfileCompletion(Authentication auth) {
        Student student = getCurrentStudent(auth);

        Map<String, ProfileCompletionResponse.SectionStatus> breakdown = new LinkedHashMap<>();

        boolean personalOk = student.getPersonalInfo() != null
                && student.getPersonalInfo().getFirstName() != null
                && student.getPersonalInfo().getLastName() != null
                && student.getPersonalInfo().getPhoneNumber() != null
                && student.getPersonalInfo().getDateOfBirth() != null
                && student.getPersonalInfo().getGender() != null;
        breakdown.put("personalInfo", new ProfileCompletionResponse.SectionStatus(personalOk, 20));

        boolean academicOk = student.getAcademicInfo() != null
                && student.getAcademicInfo().getBranch() != null
                && student.getAcademicInfo().getSemester() != null
                && student.getAcademicInfo().getCgpa() != null
                && student.getAcademicInfo().getPercentage() != null
                && student.getAcademicInfo().getGraduationYear() != null;
        breakdown.put("academicInfo", new ProfileCompletionResponse.SectionStatus(academicOk, 25));

        breakdown.put("skills", new ProfileCompletionResponse.SectionStatus(
                student.getSkills() != null && !student.getSkills().isEmpty(), 15));
        breakdown.put("projects", new ProfileCompletionResponse.SectionStatus(
                !student.getProjects().isEmpty(), 15));
        breakdown.put("internships", new ProfileCompletionResponse.SectionStatus(
                !student.getInternships().isEmpty(), 10));
        breakdown.put("certifications", new ProfileCompletionResponse.SectionStatus(
                !student.getCertifications().isEmpty(), 10));

        boolean socialOk = student.getSocialLinks() != null
                && (student.getSocialLinks().getLinkedin() != null || student.getSocialLinks().getGithub() != null);
        breakdown.put("socialLinks", new ProfileCompletionResponse.SectionStatus(socialOk, 5));

        int completion = breakdown.values().stream()
                .filter(ProfileCompletionResponse.SectionStatus::completed)
                .mapToInt(ProfileCompletionResponse.SectionStatus::weight)
                .sum();

        String message = completion >= 80
                ? "Profile is complete!"
                : "Profile is " + completion + "% complete. Complete it to access all features.";

        return new ProfileCompletionResponse(completion, student.isProfileCompleted(), breakdown, message);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
