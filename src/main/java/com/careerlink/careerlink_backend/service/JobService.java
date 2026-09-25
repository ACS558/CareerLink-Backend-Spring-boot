package com.careerlink.careerlink_backend.service;


import com.careerlink.careerlink_backend.dto.request.JobCreateRequest;
import com.careerlink.careerlink_backend.dto.request.JobUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.JobResponse;
import com.careerlink.careerlink_backend.entity.Job;
import com.careerlink.careerlink_backend.entity.Recruiter;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.User;
import com.careerlink.careerlink_backend.entity.enums.*;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.mapper.JobMapper;
import com.careerlink.careerlink_backend.repository.JobRepository;
import com.careerlink.careerlink_backend.repository.RecruiterRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final RecruiterRepository recruiterRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;
    private final NotificationService notificationService;

    public Recruiter getCurrentRecruiter(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
    }

    @Transactional
    public JobResponse createJob(Authentication auth, JobCreateRequest req) {
        Recruiter recruiter = getCurrentRecruiter(auth);

        if (recruiter.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new UnauthorizedException("Your recruiter account is not yet approved by admin");
        }

        Job job = new Job();
        job.setRecruiter(recruiter);
        job.setTitle(req.title());
        job.setDescription(req.description());
        job.setJobType(req.jobType() != null ? JobType.valueOf(req.jobType()) : JobType.FULL_TIME);
        job.setWorkMode(req.workMode() != null ? WorkMode.valueOf(req.workMode()) : WorkMode.ON_SITE);
        job.setLocation(req.location());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        job.setSalaryType(req.salaryType() != null ? SalaryType.valueOf(req.salaryType()) : SalaryType.LPA);
        job.setEligibleBranches(req.eligibleBranches() != null ? req.eligibleBranches() : new ArrayList<>());
        job.setMinCgpa(req.minCgpa());
        job.setMaxBacklogs(req.maxBacklogs() != null ? req.maxBacklogs() : 0);
        job.setGraduationYears(req.graduationYears() != null ? req.graduationYears() : new ArrayList<>());
        job.setSkillsRequired(req.skillsRequired() != null ? req.skillsRequired() : new ArrayList<>());
        job.setNumberOfOpenings(req.numberOfOpenings() != null ? req.numberOfOpenings() : 1);
        job.setApplicationDeadline(req.applicationDeadline());
        job.setApprovalStatus(ApprovalStatus.PENDING);

        Job saved = jobRepository.save(job);

        notificationService.notifyAllAdmins(
                NotificationType.JOB_POSTED,
                "New Job Posting Pending Approval",
                saved.getTitle() + " at " + recruiter.getCompanyInfo().getCompanyName(),
                "/admin/jobs"
        );

        return jobMapper.toResponse(saved);
    }

    public Page<JobResponse> getEligibleJobsForStudent(Student student, Pageable pageable) {
        String branch = student.getAcademicInfo() != null ? student.getAcademicInfo().getBranch() : null;
        Double cgpa = student.getAcademicInfo() != null ? student.getAcademicInfo().getCgpa() : 0.0;
        Integer backlogs = student.getAcademicInfo() != null ? student.getAcademicInfo().getBacklogs() : 0;
        Integer gradYear = student.getAcademicInfo() != null ? student.getAcademicInfo().getGraduationYear() : null;

        return jobRepository.findEligibleJobsForStudent(branch, cgpa, backlogs, gradYear, pageable)
                .map(jobMapper::toResponse);
    }

    public List<JobResponse> getJobsByRecruiter(Authentication auth) {
        Recruiter recruiter = getCurrentRecruiter(auth);
        return jobRepository.findByRecruiterId(recruiter.getId())
                .stream().map(jobMapper::toResponse).toList();
    }

    public Job getJobEntity(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    public boolean isStudentEligible(Job job, Student student) {
        if (job.getApprovalStatus() != ApprovalStatus.APPROVED || !job.isActive()) return false;

        String branch = student.getAcademicInfo() != null ? student.getAcademicInfo().getBranch() : null;
        Double cgpa = student.getAcademicInfo() != null ? student.getAcademicInfo().getCgpa() : null;
        Integer backlogs = student.getAcademicInfo() != null ? student.getAcademicInfo().getBacklogs() : 0;
        Integer gradYear = student.getAcademicInfo() != null ? student.getAcademicInfo().getGraduationYear() : null;

        boolean branchOk = job.getEligibleBranches().isEmpty() || job.getEligibleBranches().contains(branch);
        boolean cgpaOk = job.getMinCgpa() == null || (cgpa != null && cgpa >= job.getMinCgpa());
        boolean backlogOk = backlogs != null && backlogs <= job.getMaxBacklogs();
        boolean gradYearOk = job.getGraduationYears().isEmpty() || job.getGraduationYears().contains(gradYear);

        return branchOk && cgpaOk && backlogOk && gradYearOk;
    }

    public JobResponse getJobById(Long jobId) {
        Job job = getJobEntity(jobId);
        return jobMapper.toResponse(job);
    }

    @Transactional
    public JobResponse updateJob(Authentication auth, Long jobId, JobUpdateRequest req) {
        Recruiter recruiter = getCurrentRecruiter(auth);
        Job job = getJobEntity(jobId);

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedException("Not authorized to update this job");
        }
        if (job.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new UnauthorizedException("Cannot edit an approved job. Please contact admin.");
        }

        if (req.title() != null) job.setTitle(req.title());
        if (req.description() != null) job.setDescription(req.description());
        if (req.jobType() != null) job.setJobType(JobType.valueOf(req.jobType()));
        if (req.workMode() != null) job.setWorkMode(WorkMode.valueOf(req.workMode()));
        if (req.location() != null) job.setLocation(req.location());
        if (req.salaryMin() != null) job.setSalaryMin(req.salaryMin());
        if (req.salaryMax() != null) job.setSalaryMax(req.salaryMax());
        if (req.salaryType() != null) job.setSalaryType(SalaryType.valueOf(req.salaryType()));
        if (req.eligibleBranches() != null) job.setEligibleBranches(req.eligibleBranches());
        if (req.minCgpa() != null) job.setMinCgpa(req.minCgpa());
        if (req.maxBacklogs() != null) job.setMaxBacklogs(req.maxBacklogs());
        if (req.graduationYears() != null) job.setGraduationYears(req.graduationYears());
        if (req.skillsRequired() != null) job.setSkillsRequired(req.skillsRequired());
        if (req.numberOfOpenings() != null) job.setNumberOfOpenings(req.numberOfOpenings());
        if (req.applicationDeadline() != null) job.setApplicationDeadline(req.applicationDeadline());

        // Editing resets approval — matches original behavior: any edit re-enters the review queue
        job.setApprovalStatus(ApprovalStatus.PENDING);

        Job saved = jobRepository.save(job);

        notificationService.notifyAllAdmins(
                NotificationType.JOB_POSTED,
                "Job Posting Updated — Pending Re-Approval",
                saved.getTitle() + " was edited by " + recruiter.getCompanyInfo().getCompanyName(),
                "/admin/jobs"
        );

        return jobMapper.toResponse(saved);
    }

    @Transactional
    public void deleteJob(Authentication auth, Long jobId) {
        Recruiter recruiter = getCurrentRecruiter(auth);
        Job job = getJobEntity(jobId);

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedException("Not authorized to delete this job");
        }

        jobRepository.delete(job);
    }
}
