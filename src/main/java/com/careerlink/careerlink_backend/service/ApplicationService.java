package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.ApplicationRequest;
import com.careerlink.careerlink_backend.dto.request.ApplicationStatusUpdateRequest;
import com.careerlink.careerlink_backend.dto.request.AutoShortlistRequest;
import com.careerlink.careerlink_backend.dto.request.BulkStatusUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.ApplicationResponse;
import com.careerlink.careerlink_backend.entity.Application;
import com.careerlink.careerlink_backend.entity.Job;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.StudentPlacement;
import com.careerlink.careerlink_backend.entity.enums.ApplicationStatus;
import com.careerlink.careerlink_backend.entity.enums.NotificationPriority;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.entity.enums.PlacementStatus;
import com.careerlink.careerlink_backend.exception.DuplicateResourceException;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.mapper.ApplicationMapper;
import com.careerlink.careerlink_backend.repository.ApplicationRepository;
import com.careerlink.careerlink_backend.repository.StudentPlacementRepository;
import com.careerlink.careerlink_backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final StudentService studentService;
    private final AtsService atsService;
    private final ApplicationMapper applicationMapper;
    private final NotificationService notificationService;
    private final StudentPlacementRepository studentPlacementRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public ApplicationResponse applyForJob(Authentication auth, Long jobId, ApplicationRequest req) {
        Student student = studentService.getCurrentStudent(auth);
        Job job = jobService.getJobEntity(jobId);

        if (!jobService.isStudentEligible(job, student)) {
            throw new UnauthorizedException("You are not eligible for this job posting");
        }

        if (applicationRepository.existsByJobIdAndStudentId(jobId, student.getId())) {
            throw new DuplicateResourceException("You have already applied for this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setStudent(student);
        application.setRecruiter(job.getRecruiter());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setCoverLetter(req != null ? req.coverLetter() : null);

        // ATS scoring — placeholder now, real Gemini call in Phase 7
        var atsScore = atsService.calculateScore(
                student.getResumeText(),
                job.getDescription(),
                job.getSkillsRequired()
        );
        application.setAtsScore(atsScore);

        Application saved = applicationRepository.save(application);

        notificationService.notifyUser(
                student.getUser(), NotificationType.APPLICATION_RECEIVED,
                "Application Submitted",
                "Your application for " + job.getTitle() + " at " +
                        (job.getRecruiter().getCompanyInfo() != null ? job.getRecruiter().getCompanyInfo().getCompanyName() : "the company") +
                        " was submitted successfully.",
                job.getId(), saved.getId(), NotificationPriority.LOW, "/student/applications"
        );

        return applicationMapper.toResponse(saved);
    }

    public List<ApplicationResponse> getMyApplications(Authentication auth) {
        Student student = studentService.getCurrentStudent(auth);
        return applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId())
                .stream().map(applicationMapper::toResponse).toList();
    }

    public List<ApplicationResponse> getApplicationsForJob(Authentication auth, Long jobId) {
        var recruiter = jobService.getCurrentRecruiter(auth);
        Job job = jobService.getJobEntity(jobId);

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedException("This job does not belong to you");
        }

        return applicationRepository.findByJobIdOrderByAtsScore_ScoreDesc(jobId)
                .stream().map(applicationMapper::toResponse).toList();
    }

    public Application getApplicationEntity(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    @Transactional
    public ApplicationResponse updateStatus(Authentication auth, Long applicationId, ApplicationStatusUpdateRequest req) {
        var recruiter = jobService.getCurrentRecruiter(auth);
        Application application = getApplicationEntity(applicationId);

        if (!application.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedException("This application does not belong to your job postings");
        }

        ApplicationStatus newStatus = ApplicationStatus.valueOf(req.status());
        applyStatusTransition(application, newStatus, req.rejectionReason(), req.recruiterNotes());

        Application saved = applicationRepository.save(application);
        notifyStudentOfStatusChange(saved);
        createPlacementIfSelected(saved);
        return applicationMapper.toResponse(saved);
    }

    @Transactional
    public int bulkUpdateStatus(Authentication auth, BulkStatusUpdateRequest req) {
        var recruiter = jobService.getCurrentRecruiter(auth);
        ApplicationStatus newStatus = ApplicationStatus.valueOf(req.status());

        List<Application> applications = applicationRepository.findAllById(req.applicationIds());
        for (Application app : applications) {
            if (!app.getRecruiter().getId().equals(recruiter.getId())) {
                throw new UnauthorizedException("One or more applications do not belong to your job postings");
            }
        }

        int updated = applicationRepository.bulkUpdateStatus(req.applicationIds(), newStatus);

        // Re-fetch to get updated state for notifications (bulkUpdateStatus is a direct JPQL update, bypasses entity state)
        List<Application> refreshed = applicationRepository.findAllById(req.applicationIds());
        for (Application app : refreshed) {
            notifyStudentOfStatusChange(app);
            createPlacementIfSelected(app);
        }

        return updated;
    }

    @Transactional
    public int autoShortlist(Authentication auth, Long jobId, AutoShortlistRequest req) {
        var recruiter = jobService.getCurrentRecruiter(auth);
        Job job = jobService.getJobEntity(jobId);

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedException("This job does not belong to you");
        }

        List<Application> eligible = applicationRepository.findByJobIdAndAtsScore_ScoreGreaterThanEqual(jobId, req.threshold());
        List<Long> ids = eligible.stream().map(Application::getId).toList();

        if (ids.isEmpty()) return 0;

        applicationRepository.bulkUpdateStatus(ids, ApplicationStatus.SHORTLISTED);

        List<Application> refreshed = applicationRepository.findAllById(ids);
        for (Application app : refreshed) {
            notifyStudentOfStatusChange(app);
        }

        return ids.size();
    }

    @Transactional
    public ApplicationResponse updateStatusAsAdmin(Long applicationId, ApplicationStatusUpdateRequest req) {
        Application application = getApplicationEntity(applicationId);
        ApplicationStatus newStatus = ApplicationStatus.valueOf(req.status());
        applyStatusTransition(application, newStatus, req.rejectionReason(), req.recruiterNotes());

        Application saved = applicationRepository.save(application);
        notifyStudentOfStatusChange(saved);
        createPlacementIfSelected(saved);
        return applicationMapper.toResponse(saved);
    }

    @Transactional
    public int bulkUpdateStatusAsAdmin(BulkStatusUpdateRequest req) {
        ApplicationStatus newStatus = ApplicationStatus.valueOf(req.status());
        int updated = applicationRepository.bulkUpdateStatus(req.applicationIds(), newStatus);

        List<Application> refreshed = applicationRepository.findAllById(req.applicationIds());
        for (Application app : refreshed) {
            notifyStudentOfStatusChange(app);
            createPlacementIfSelected(app);
        }
        return updated;
    }

    private void applyStatusTransition(Application app, ApplicationStatus newStatus, String rejectionReason, String recruiterNotes) {
        app.setStatus(newStatus);
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case SHORTLISTED -> app.setShortlistedAt(now);
            case REJECTED -> { app.setRejectedAt(now); app.setRejectionReason(rejectionReason); }
            case SELECTED -> app.setSelectedAt(now);
            default -> {}
        }
        if (recruiterNotes != null) app.setRecruiterNotes(recruiterNotes);
    }

    private void notifyStudentOfStatusChange(Application app) {
        NotificationType type = switch (app.getStatus()) {
            case SHORTLISTED -> NotificationType.APPLICATION_SHORTLISTED;
            case REJECTED -> NotificationType.APPLICATION_REJECTED;
            case SELECTED -> NotificationType.APPLICATION_SELECTED;
            default -> NotificationType.APPLICATION_PENDING;
        };

        String message = "Your application for " + app.getJob().getTitle() + " is now: " + app.getStatus().name();

        notificationService.notifyUser(
                app.getStudent().getUser(), type, "Application Status Updated", message,
                app.getJob().getId(), app.getId(), NotificationPriority.HIGH, "/student/applications"
        );
    }

    private void createPlacementIfSelected(Application app) {
        if (app.getStatus() != ApplicationStatus.SELECTED) return;
        if (studentPlacementRepository.existsByRelatedApplicationId(app.getId())) return;

        Student student = app.getStudent();
        Job job = app.getJob();
        String companyName = job.getRecruiter().getCompanyInfo() != null
                ? job.getRecruiter().getCompanyInfo().getCompanyName() : "Company";
        Double pkg = job.getSalaryMax() != null ? job.getSalaryMax() : job.getSalaryMin();

        StudentPlacement placement = new StudentPlacement();
        placement.setStudent(student);
        placement.setCompany(companyName);
        placement.setJobTitle(job.getTitle());
        placement.setPackageOffered(pkg);
        placement.setOfferDate(LocalDate.now());
        placement.setPrimary(student.getPlacements().isEmpty());
        placement.setRelatedJobId(job.getId());
        placement.setRelatedApplicationId(app.getId());
        studentPlacementRepository.save(placement);

        student.setPlacementStatus(PlacementStatus.PLACED);
        student.setPlacedCompany(companyName);
        student.setPlacedPackage(pkg);
        student.setPlacedAt(LocalDateTime.now());
        studentRepository.save(student);

        notificationService.notifyUser(
                student.getUser(), NotificationType.APPLICATION_SELECTED,
                "New Job Offer!",
                "Congratulations! You've been selected for " + job.getTitle() + " at " + companyName + ".",
                job.getId(), app.getId(), NotificationPriority.HIGH, "/student/placements"
        );
    }

    public ApplicationResponse getApplicationById(Authentication auth, Long applicationId) {
        Student student = studentService.getCurrentStudent(auth);
        Application application = getApplicationEntity(applicationId);

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("Not authorized to view this application");
        }
        return applicationMapper.toResponse(application);
    }

    @Transactional
    public void withdrawApplication(Authentication auth, Long applicationId) {
        Student student = studentService.getCurrentStudent(auth);
        Application application = getApplicationEntity(applicationId);

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("Not authorized to withdraw this application");
        }
        if (application.getStatus() == ApplicationStatus.SHORTLISTED || application.getStatus() == ApplicationStatus.SELECTED) {
            throw new UnauthorizedException("Cannot withdraw an application that is " + application.getStatus().name().toLowerCase());
        }

        applicationRepository.delete(application);
    }

    @Transactional
    public int recalculateAtsScores(Authentication auth, Long jobId) {
        var recruiter = jobService.getCurrentRecruiter(auth);
        Job job = jobService.getJobEntity(jobId);

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedException("Not authorized");
        }

        List<Application> applications = applicationRepository.findByJobIdOrderByAtsScore_ScoreDesc(jobId);
        int processed = 0;
        for (Application app : applications) {
            var atsScore = atsService.calculateScore(app.getStudent().getResumeText(), job.getDescription(), job.getSkillsRequired());
            app.setAtsScore(atsScore);
            applicationRepository.save(app);
            processed++;
        }
        return processed;
    }
}
