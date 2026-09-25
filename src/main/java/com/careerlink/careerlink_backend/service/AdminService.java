package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.AdminCreateRequest;
import com.careerlink.careerlink_backend.dto.request.AdminProfileUpdateRequest;
import com.careerlink.careerlink_backend.dto.request.ApplicationStatusUpdateRequest;
import com.careerlink.careerlink_backend.dto.request.BulkStatusUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.*;
import com.careerlink.careerlink_backend.entity.*;
import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.enums.*;
import com.careerlink.careerlink_backend.exception.DuplicateResourceException;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.mapper.ApplicationMapper;
import com.careerlink.careerlink_backend.mapper.JobMapper;
import com.careerlink.careerlink_backend.mapper.StudentMapper;
import com.careerlink.careerlink_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final RecruiterRepository recruiterRepository;
    private final AlumniRepository alumniRepository;
    private final JobRepository jobRepository;
    private final ReferralRepository referralRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecruiterService recruiterService;
    private final AlumniService alumniService;
    private final ReferralService referralService;
    private final JobMapper jobMapper;
    private final NotificationService notificationService;
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationService applicationService;
    private final StudentMapper studentMapper;

    // ---------- Admin profile ----------

    public AdminProfileResponse getProfile(Authentication auth) {
        Admin admin = getCurrentAdmin(auth);
        return toProfileResponse(admin);
    }

    @Transactional
    public AdminProfileResponse updateProfile(Authentication auth, AdminProfileUpdateRequest req) {
        Admin admin = getCurrentAdmin(auth);

        PersonalInfo personalInfo = admin.getPersonalInfo() != null ? admin.getPersonalInfo() : new PersonalInfo();
        if (req.firstName() != null) personalInfo.setFirstName(req.firstName());
        if (req.lastName() != null) personalInfo.setLastName(req.lastName());
        if (req.phoneNumber() != null) personalInfo.setPhoneNumber(req.phoneNumber());
        admin.setPersonalInfo(personalInfo);

        if (req.department() != null) admin.setDepartment(req.department());

        return toProfileResponse(adminRepository.save(admin));
    }

    private AdminProfileResponse toProfileResponse(Admin admin) {
        return new AdminProfileResponse(
                admin.getId(),
                admin.getUser().getEmail(),
                admin.getPersonalInfo() != null ? admin.getPersonalInfo().getFirstName() : null,
                admin.getPersonalInfo() != null ? admin.getPersonalInfo().getLastName() : null,
                admin.getDepartment(),
                admin.getPersonalInfo() != null ? admin.getPersonalInfo().getPhoneNumber() : null,
                admin.getRoleLevel().name()
        );
    }

    // ---------- List-all endpoints ----------

    public List<RecruiterResponse> listAllRecruiters(String search, String status) {
        VerificationStatus vs = (status != null && !status.isBlank()) ? VerificationStatus.valueOf(status.toUpperCase()) : null;
        String s = (search != null && !search.isBlank()) ? search : null;
        return recruiterRepository.searchRecruiters(s, vs).stream().map(recruiterService::toResponse).toList();
    }

    public List<AlumniResponse> listAllAlumni(String search, String status, String branch, Integer graduationYear) {
        VerificationStatus vs = (status != null && !status.isBlank()) ? VerificationStatus.valueOf(status.toUpperCase()) : null;
        String s = (search != null && !search.isBlank()) ? search : null;
        String b = (branch != null && !branch.isBlank()) ? branch : null;
        return alumniRepository.searchAlumni(s, vs, b, graduationYear).stream().map(alumniService::toResponse).toList();
    }

    public Page<StudentResponse> listAllStudents(String search, String branch, String placementStatus,
                                                 Integer graduationYear, Pageable pageable) {
        String s = (search != null && !search.isBlank()) ? search : null;
        String b = (branch != null && !branch.isBlank()) ? branch : null;
        PlacementStatus ps = (placementStatus != null && !placementStatus.isBlank())
                ? PlacementStatus.valueOf(placementStatus.toUpperCase()) : null;
        return studentRepository.searchStudents(s, b, ps, graduationYear, pageable).map(studentMapper::toResponse);
    }

    public List<JobResponse> listAllJobs(String search, String status) {
        ApprovalStatus as = (status != null && !status.isBlank()) ? ApprovalStatus.valueOf(status.toUpperCase()) : null;
        String s = (search != null && !search.isBlank()) ? search : null;
        return jobRepository.searchJobsForAdmin(s, as).stream().map(jobMapper::toResponse).toList();
    }

    public List<ReferralResponse> listAllReferrals() {
        return referralRepository.findAllByOrderByCreatedAtDesc().stream().map(referralService::toResponse).toList();
    }

    // ---------- Admin job detail ----------

    public AdminJobDetailResponse getJobDetail(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        List<Application> applications = applicationRepository.findByJobIdOrderByAtsScore_ScoreDesc(jobId);
        List<ApplicationResponse> appResponses = applications.stream().map(applicationMapper::toResponse).toList();

        long total = applications.size();
        long pending = applications.stream().filter(a -> a.getStatus() == com.careerlink.careerlink_backend.entity.enums.ApplicationStatus.PENDING).count();
        long shortlisted = applications.stream().filter(a -> a.getStatus() == com.careerlink.careerlink_backend.entity.enums.ApplicationStatus.SHORTLISTED).count();
        long selected = applications.stream().filter(a -> a.getStatus() == com.careerlink.careerlink_backend.entity.enums.ApplicationStatus.SELECTED).count();
        long rejected = applications.stream().filter(a -> a.getStatus() == com.careerlink.careerlink_backend.entity.enums.ApplicationStatus.REJECTED).count();

        return new AdminJobDetailResponse(
                jobMapper.toResponse(job),
                appResponses,
                new AdminJobDetailResponse.JobStats(total, pending, shortlisted, selected, rejected)
        );
    }

    // ---------- Admin application oversight (no ownership restriction) ----------

    @Transactional
    public ApplicationResponse adminUpdateApplicationStatus(Authentication auth, Long applicationId, ApplicationStatusUpdateRequest req) {
        return applicationService.updateStatusAsAdmin(applicationId, req);
    }

    @Transactional
    public int adminBulkUpdateApplications(BulkStatusUpdateRequest req) {
        return applicationService.bulkUpdateStatusAsAdmin(req);
    }

    // ---------- Dashboard stats ----------

    public AdminDashboardStatsResponse getDashboardStats() {
        long totalStudents = studentRepository.count();
        long placedStudents = studentRepository.countByPlacementStatus(PlacementStatus.PLACED);
        long unplacedStudents = studentRepository.countByPlacementStatus(PlacementStatus.UNPLACED);
        double placementPct = totalStudents == 0 ? 0.0 : Math.round((placedStudents * 10000.0 / totalStudents)) / 100.0;

        long totalRecruiters = recruiterRepository.count();
        long pendingRecruiters = recruiterRepository.findByVerificationStatus(VerificationStatus.PENDING).size();
        long approvedRecruiters = recruiterRepository.findByVerificationStatus(VerificationStatus.APPROVED).size();
        long rejectedRecruiters = recruiterRepository.findByVerificationStatus(VerificationStatus.REJECTED).size();

        long totalAlumni = alumniRepository.count();
        long pendingAlumni = alumniRepository.findByVerificationStatus(VerificationStatus.PENDING).size();
        long approvedAlumni = alumniRepository.findByVerificationStatus(VerificationStatus.APPROVED).size();
        long rejectedAlumni = alumniRepository.findByVerificationStatus(VerificationStatus.REJECTED).size();

        Double avgPkg = studentRepository.averagePlacedPackage();
        Double highPkg = studentRepository.highestPlacedPackage();
        Double lowPkg = studentRepository.lowestPlacedPackage();

        return new AdminDashboardStatsResponse(
                new AdminDashboardStatsResponse.StudentStats(totalStudents, placedStudents, unplacedStudents, placementPct),
                new AdminDashboardStatsResponse.RecruiterStats(totalRecruiters, pendingRecruiters, approvedRecruiters, rejectedRecruiters),
                new AdminDashboardStatsResponse.AlumniStats(totalAlumni, pendingAlumni, approvedAlumni, rejectedAlumni),
                new AdminDashboardStatsResponse.PackageStats(avgPkg, highPkg, lowPkg),
                pendingRecruiters + pendingAlumni
        );
    }

    public Admin getCurrentAdmin(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return adminRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }

    // ---------- Recruiter approvals ----------

    public List<RecruiterResponse> getPendingRecruiters() {
        return recruiterRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream().map(recruiterService::toResponse).toList();
    }

    @Transactional
    public RecruiterResponse approveRecruiter(Authentication auth, Long recruiterId) {
        Admin admin = getCurrentAdmin(auth);
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        recruiter.setVerificationStatus(VerificationStatus.APPROVED);
        recruiter.setVerifiedBy(admin);
        recruiter.setVerifiedAt(LocalDateTime.now());
        Recruiter saved = recruiterRepository.save(recruiter);

        notificationService.notifyUser(saved.getUser(), NotificationType.RECRUITER_APPROVED,
                "Account Approved", "Your recruiter account has been approved. You can now post jobs.",
                null, null, NotificationPriority.HIGH, "/recruiter/dashboard");

        return recruiterService.toResponse(saved);
    }

    @Transactional
    public RecruiterResponse rejectRecruiter(Authentication auth, Long recruiterId, String reason) {
        Admin admin = getCurrentAdmin(auth);
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        recruiter.setVerificationStatus(VerificationStatus.REJECTED);
        recruiter.setVerifiedBy(admin);
        recruiter.setVerifiedAt(LocalDateTime.now());
        recruiter.setRejectionReason(reason);
        Recruiter saved = recruiterRepository.save(recruiter);

        notificationService.notifyUser(
                saved.getUser(), NotificationType.RECRUITER_REJECTED,
                "Account Rejected",
                "Your recruiter account registration was rejected" + (reason != null ? ": " + reason : "."),
                null, null, NotificationPriority.HIGH, "/recruiter/profile"
        );

        return recruiterService.toResponse(saved);
    }

    // ---------- Alumni approvals ----------

    public List<AlumniResponse> getPendingAlumni() {
        return alumniRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream().map(alumniService::toResponse).toList();
    }

    @Transactional
    public AlumniResponse approveAlumni(Authentication auth, Long alumniId) {
        Admin admin = getCurrentAdmin(auth);
        Alumni alumni = alumniRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni not found"));

        alumni.setVerificationStatus(VerificationStatus.APPROVED);
        alumni.setVerifiedBy(admin);
        alumni.setVerifiedAt(LocalDateTime.now());
        Alumni saved = alumniRepository.save(alumni);

        notificationService.notifyUser(
                saved.getUser(), NotificationType.ALUMNI_APPROVED,
                "Account Approved",
                "Your alumni account has been approved. You can now post referrals and career guidance.",
                null, null, NotificationPriority.HIGH, "/alumni/dashboard"
        );

        return alumniService.toResponse(saved);
    }

    @Transactional
    public AlumniResponse rejectAlumni(Authentication auth, Long alumniId, String reason) {
        Admin admin = getCurrentAdmin(auth);
        Alumni alumni = alumniRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni not found"));

        alumni.setVerificationStatus(VerificationStatus.REJECTED);
        alumni.setVerifiedBy(admin);
        alumni.setVerifiedAt(LocalDateTime.now());
        alumni.setRejectionReason(reason);
        Alumni saved = alumniRepository.save(alumni);

        notificationService.notifyUser(
                saved.getUser(), NotificationType.ALUMNI_REJECTED,
                "Account Rejected",
                "Your alumni account registration was rejected" + (reason != null ? ": " + reason : "."),
                null, null, NotificationPriority.HIGH, "/alumni/profile"
        );

        return alumniService.toResponse(saved);
    }

    // ---------- Job approvals ----------

    public List<JobResponse> getPendingJobs() {
        return jobRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream().map(jobMapper::toResponse).toList();
    }

    @Transactional
    public JobResponse approveJob(Authentication auth, Long jobId) {
        Admin admin = getCurrentAdmin(auth);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        job.setApprovalStatus(ApprovalStatus.APPROVED);
        job.setApprovedBy(admin);
        job.setApprovedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);

        notificationService.notifyUser(
                saved.getRecruiter().getUser(), NotificationType.JOB_APPROVED,
                "Job Posting Approved",
                "Your job posting \"" + saved.getTitle() + "\" has been approved and is now visible to students.",
                saved.getId(), null, NotificationPriority.HIGH, "/recruiter/jobs/" + saved.getId()
        );

        return jobMapper.toResponse(saved);
    }

    @Transactional
    public JobResponse rejectJob(Authentication auth, Long jobId, String reason) {
        Admin admin = getCurrentAdmin(auth);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        job.setApprovalStatus(ApprovalStatus.REJECTED);
        job.setApprovedBy(admin);
        job.setApprovedAt(LocalDateTime.now());
        job.setRejectionReason(reason);
        Job saved = jobRepository.save(job);

        notificationService.notifyUser(
                saved.getRecruiter().getUser(), NotificationType.JOB_REJECTED,
                "Job Posting Rejected",
                "Your job posting \"" + saved.getTitle() + "\" was rejected" + (reason != null ? ": " + reason : "."),
                saved.getId(), null, NotificationPriority.HIGH, "/recruiter/jobs"
        );

        return jobMapper.toResponse(saved);
    }

    // ---------- Referral approvals ----------

    public List<ReferralResponse> getPendingReferrals() {
        return referralRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream().map(referralService::toResponse).toList();
    }

    @Transactional
    public ReferralResponse approveReferral(Authentication auth, Long referralId) {
        Admin admin = getCurrentAdmin(auth);
        Referral referral = referralService.getReferralEntity(referralId);

        referral.setApprovalStatus(ApprovalStatus.APPROVED);
        referral.setApprovedBy(admin);
        referral.setApprovedAt(LocalDateTime.now());
        Referral saved = referralRepository.save(referral);

        notificationService.notifyUser(
                saved.getAlumni().getUser(), NotificationType.REFERRAL_APPROVED,
                "Referral Approved",
                "Your referral for " + saved.getCompany() + " has been approved and published.",
                null, null, NotificationPriority.MEDIUM, "/alumni/referrals"
        );

        return referralService.toResponse(saved);
    }

    @Transactional
    public ReferralResponse rejectReferral(Authentication auth, Long referralId, String reason) {
        Admin admin = getCurrentAdmin(auth);
        Referral referral = referralService.getReferralEntity(referralId);

        referral.setApprovalStatus(ApprovalStatus.REJECTED);
        referral.setApprovedBy(admin);
        referral.setApprovedAt(LocalDateTime.now());
        referral.setRejectionReason(reason);
        Referral saved = referralRepository.save(referral);

        notificationService.notifyUser(
                saved.getAlumni().getUser(), NotificationType.REFERRAL_REJECTED,
                "Referral Rejected",
                "Your referral for " + saved.getCompany() + " was rejected" + (reason != null ? ": " + reason : "."),
                null, null, NotificationPriority.MEDIUM, "/alumni/referrals"
        );

        return referralService.toResponse(saved);
    }

    // ---------- Student account management ----------

    @Transactional
    public void extendStudentAccount(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setAccountStatus(AccountStatus.ACTIVE);
        student.setExpiryDate(LocalDateTime.now().plusDays(365));
        student.setDeletionScheduledAt(LocalDateTime.now().plusDays(455));
        studentRepository.save(student);
        notificationService.notifyUser(
                student.getUser(), NotificationType.EXTENSION_APPROVED,
                "Account Extended",
                "Your CareerLink account access has been extended by one year.",
                null, null, NotificationPriority.MEDIUM, "/student/dashboard"
        );
    }

    // ---------- Super admin: manage admin accounts ----------

    @Transactional
    public void createAdmin(Authentication auth, AdminCreateRequest req) {
        Admin currentAdmin = getCurrentAdmin(auth);
        if (currentAdmin.getRoleLevel() != RoleLevel.SUPER_ADMIN) {
            throw new UnauthorizedException("Only a Super Admin can create new admin accounts");
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.ADMIN);
        user.setVerified(true);
        userRepository.save(user);

        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName(req.name());

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setRoleLevel(RoleLevel.ADMIN);
        admin.setPersonalInfo(personalInfo);
        adminRepository.save(admin);
    }

    public List<AdminListResponse> listAllAdmins() {
        return adminRepository.findAll().stream()
                .map(a -> new AdminListResponse(
                        a.getId(), a.getUser().getEmail(),
                        a.getPersonalInfo() != null ? a.getPersonalInfo().getFirstName() : null,
                        a.getPersonalInfo() != null ? a.getPersonalInfo().getLastName() : null,
                        a.getDepartment(), a.getRoleLevel().name()
                )).toList();
    }

    @Transactional
    public void deleteAdmin(Authentication auth, Long adminId) {
        Admin currentAdmin = getCurrentAdmin(auth);
        if (currentAdmin.getRoleLevel() != RoleLevel.SUPER_ADMIN) {
            throw new UnauthorizedException("Only a Super Admin can delete admin accounts");
        }

        Admin target = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (target.getRoleLevel() == RoleLevel.SUPER_ADMIN && adminRepository.countByRoleLevel(RoleLevel.SUPER_ADMIN) <= 1) {
            throw new UnauthorizedException("Cannot delete the last Super Admin");
        }

        User targetUser = target.getUser();
        adminRepository.delete(target);
        userRepository.delete(targetUser);
    }

    public List<ApplicationResponse> listAllApplications(String status, Long jobId, Long studentId) {
        ApplicationStatus as = (status != null && !status.isBlank()) ? ApplicationStatus.valueOf(status.toUpperCase()) : null;
        return applicationRepository.findAllFiltered(as, jobId, studentId).stream()
                .map(applicationMapper::toResponse)
                .toList();
    }
}
