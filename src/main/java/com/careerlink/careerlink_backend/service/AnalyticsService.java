package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.response.analytics.*;
import com.careerlink.careerlink_backend.entity.Application;
import com.careerlink.careerlink_backend.entity.Job;
import com.careerlink.careerlink_backend.entity.Recruiter;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.enums.ApplicationStatus;
import com.careerlink.careerlink_backend.entity.enums.ApprovalStatus;
import com.careerlink.careerlink_backend.entity.enums.PlacementStatus;
import com.careerlink.careerlink_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final RecruiterRepository recruiterRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final JobService jobService;
    private final RecruiterService recruiterService;

    // ---------- Student ----------

    public StudentAnalyticsResponse getStudentAnalytics(Authentication auth) {
        Student student = studentService.getCurrentStudent(auth);
        List<Application> apps = applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId());

        long total = apps.size();
        long applied = countStatus(apps, ApplicationStatus.APPLIED);
        long shortlisted = countStatus(apps, ApplicationStatus.SHORTLISTED);
        long rejected = countStatus(apps, ApplicationStatus.REJECTED);
        long selected = countStatus(apps, ApplicationStatus.SELECTED);

        double avgAts = apps.stream()
                .filter(a -> a.getAtsScore() != null && a.getAtsScore().getScore() != null)
                .mapToDouble(a -> a.getAtsScore().getScore())
                .average().orElse(0.0);

        var completion = studentService.getProfileCompletion(auth);
        int profileCompletion = student.isProfileCompleted() ? 100 : completion.completionPercentage();

        List<StudentAnalyticsResponse.RecentApplication> recent = apps.stream()
                .sorted(Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(a -> new StudentAnalyticsResponse.RecentApplication(
                        a.getJob().getTitle(),
                        a.getJob().getRecruiter().getCompanyInfo() != null ? a.getJob().getRecruiter().getCompanyInfo().getCompanyName() : "N/A",
                        a.getStatus().name(), a.getAppliedAt(),
                        a.getAtsScore() != null && a.getAtsScore().getScore() != null ? a.getAtsScore().getScore() : 0.0
                )).toList();

        List<TrendPoint> trend = buildTrend(apps.stream().map(Application::getAppliedAt).toList());

        return new StudentAnalyticsResponse(total, applied, shortlisted, rejected, selected,
                Math.round(avgAts * 100.0) / 100.0, profileCompletion, recent, trend);
    }

    // ---------- Recruiter ----------

    public RecruiterAnalyticsResponse getRecruiterAnalytics(Authentication auth) {
        Recruiter recruiter = jobService.getCurrentRecruiter(auth);
        List<Job> jobs = jobRepository.findByRecruiterId(recruiter.getId());
        List<Long> jobIds = jobs.stream().map(Job::getId).toList();
        List<Application> apps = jobIds.isEmpty() ? List.of() : applicationRepository.findByJobIdIn(jobIds);

        long totalJobs = jobs.size();
        long activeJobs = jobs.stream().filter(j -> j.getApprovalStatus() == ApprovalStatus.APPROVED && j.isActive()).count();
        long pendingJobs = jobs.stream().filter(j -> j.getApprovalStatus() == ApprovalStatus.PENDING).count();
        long rejectedJobs = jobs.stream().filter(j -> j.getApprovalStatus() == ApprovalStatus.REJECTED).count();

        long totalApps = apps.size();
        long newApps = countStatus(apps, ApplicationStatus.APPLIED);
        long shortlistedApps = countStatus(apps, ApplicationStatus.SHORTLISTED);
        long selectedApps = countStatus(apps, ApplicationStatus.SELECTED);
        long rejectedApps = countStatus(apps, ApplicationStatus.REJECTED);
        long pendingApps = countStatus(apps, ApplicationStatus.PENDING);

        Map<Long, Long> countsByJob = apps.stream()
                .collect(Collectors.groupingBy(a -> a.getJob().getId(), Collectors.counting()));

        List<RecruiterAnalyticsResponse.TopJob> topJobs = jobs.stream()
                .map(j -> new RecruiterAnalyticsResponse.TopJob(j.getTitle(), countsByJob.getOrDefault(j.getId(), 0L), j.getApprovalStatus().name()))
                .sorted(Comparator.comparingLong(RecruiterAnalyticsResponse.TopJob::applications).reversed())
                .limit(5)
                .toList();

        List<RecruiterAnalyticsResponse.StatusCount> statusDist = List.of(
                new RecruiterAnalyticsResponse.StatusCount("Applied", newApps),
                new RecruiterAnalyticsResponse.StatusCount("Shortlisted", shortlistedApps),
                new RecruiterAnalyticsResponse.StatusCount("Rejected", rejectedApps),
                new RecruiterAnalyticsResponse.StatusCount("Selected", selectedApps),
                new RecruiterAnalyticsResponse.StatusCount("On Hold", pendingApps)
        );

        List<RecruiterAnalyticsResponse.RecentApplication> recent = apps.stream()
                .sorted(Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(a -> new RecruiterAnalyticsResponse.RecentApplication(
                        studentName(a.getStudent()), a.getJob().getTitle(),
                        a.getAtsScore() != null && a.getAtsScore().getScore() != null ? a.getAtsScore().getScore() : 0.0,
                        a.getStatus().name(), a.getAppliedAt()
                )).toList();

        List<TrendPoint> trend = buildTrend(apps.stream().map(Application::getAppliedAt).toList());

        return new RecruiterAnalyticsResponse(totalJobs, activeJobs, pendingJobs, rejectedJobs,
                totalApps, newApps, shortlistedApps, selectedApps, topJobs, trend, statusDist, recent);
    }

    // ---------- Admin ----------

    public AdminAnalyticsResponse getAdminAnalytics() {
        long totalUsers = userRepository.count();
        long totalStudents = studentRepository.count();
        long totalRecruiters = recruiterRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.isActive()).count();

        List<Job> allJobs = jobRepository.findAll();
        long totalJobs = allJobs.size();
        long approvedJobs = allJobs.stream().filter(j -> j.getApprovalStatus() == ApprovalStatus.APPROVED).count();
        long pendingJobs = allJobs.stream().filter(j -> j.getApprovalStatus() == ApprovalStatus.PENDING).count();
        long activeJobs = allJobs.stream().filter(j -> j.getApprovalStatus() == ApprovalStatus.APPROVED && j.isActive()).count();

        List<Application> allApps = applicationRepository.findAll();
        long totalApps = allApps.size();
        long applied = countStatus(allApps, ApplicationStatus.APPLIED);
        long shortlisted = countStatus(allApps, ApplicationStatus.SHORTLISTED);
        long selected = countStatus(allApps, ApplicationStatus.SELECTED);
        long rejected = countStatus(allApps, ApplicationStatus.REJECTED);
        long pending = countStatus(allApps, ApplicationStatus.PENDING);

        long placedStudents = studentRepository.countByPlacementStatus(PlacementStatus.PLACED);
        double placementPct = totalStudents == 0 ? 0.0 : Math.round((placedStudents * 10000.0 / totalStudents)) / 100.0;
        Double avgPkg = studentRepository.averagePlacedPackage();
        Double highPkg = studentRepository.highestPlacedPackage();

        List<AdminAnalyticsResponse.RecentJob> recentJobs = allJobs.stream()
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(j -> new AdminAnalyticsResponse.RecentJob(
                        j.getTitle(),
                        j.getRecruiter().getCompanyInfo() != null ? j.getRecruiter().getCompanyInfo().getCompanyName() : "N/A",
                        j.getApprovalStatus().name(), j.getCreatedAt()
                )).toList();

        List<AdminAnalyticsResponse.RecentApplication> recentApps = allApps.stream()
                .sorted(Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(a -> new AdminAnalyticsResponse.RecentApplication(
                        studentName(a.getStudent()), a.getJob().getTitle(), a.getStatus().name(), a.getAppliedAt()
                )).toList();

        List<TrendPoint> trend = buildTrend(allApps.stream().map(Application::getAppliedAt).toList());

        List<AdminAnalyticsResponse.StatusCount> statusDist = List.of(
                new AdminAnalyticsResponse.StatusCount("Applied", applied),
                new AdminAnalyticsResponse.StatusCount("Shortlisted", shortlisted),
                new AdminAnalyticsResponse.StatusCount("Selected", selected),
                new AdminAnalyticsResponse.StatusCount("Rejected", rejected),
                new AdminAnalyticsResponse.StatusCount("On Hold", pending)
        );

        Map<String, Long> branchCounts = allApps.stream()
                .filter(a -> a.getStudent().getAcademicInfo() != null && a.getStudent().getAcademicInfo().getBranch() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getAcademicInfo().getBranch(), Collectors.counting()));

        List<AdminAnalyticsResponse.BranchCount> branchDist = branchCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new AdminAnalyticsResponse.BranchCount(e.getKey(), e.getValue()))
                .toList();

        return new AdminAnalyticsResponse(
                new AdminAnalyticsResponse.UserStats(totalUsers, totalStudents, totalRecruiters, activeUsers),
                new AdminAnalyticsResponse.JobStats(totalJobs, approvedJobs, pendingJobs, activeJobs),
                new AdminAnalyticsResponse.ApplicationStats(totalApps, applied, shortlisted, selected, rejected),
                new AdminAnalyticsResponse.PlacementStats(placedStudents, placementPct,
                        avgPkg != null ? avgPkg : 0.0, highPkg != null ? highPkg : 0.0),
                new AdminAnalyticsResponse.RecentActivities(recentJobs, recentApps),
                new AdminAnalyticsResponse.Charts(trend, statusDist, branchDist)
        );
    }

    // ---------- Advanced Analytics ----------

    public AdvancedAnalyticsResponse getAdvancedAnalytics(LocalDate startDate, LocalDate endDate, String branch, String company) {
        List<Application> apps = applicationRepository.findAll();

        if (startDate != null && endDate != null) {
            apps = apps.stream()
                    .filter(a -> a.getAppliedAt() != null
                            && !a.getAppliedAt().toLocalDate().isBefore(startDate)
                            && !a.getAppliedAt().toLocalDate().isAfter(endDate))
                    .toList();
        }
        if (branch != null && !branch.isBlank()) {
            apps = apps.stream()
                    .filter(a -> a.getStudent().getAcademicInfo() != null && branch.equalsIgnoreCase(a.getStudent().getAcademicInfo().getBranch()))
                    .toList();
        }
        if (company != null && !company.isBlank()) {
            apps = apps.stream()
                    .filter(a -> a.getJob().getRecruiter().getCompanyInfo() != null
                            && company.equalsIgnoreCase(a.getJob().getRecruiter().getCompanyInfo().getCompanyName()))
                    .toList();
        }

        long totalApps = apps.size();
        long totalSelected = countStatus(apps, ApplicationStatus.SELECTED);
        double successRate = totalApps == 0 ? 0.0 : Math.round((totalSelected * 10000.0 / totalApps)) / 100.0;

        // Branch-wise
        Map<String, List<Application>> byBranch = apps.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getAcademicInfo() != null && a.getStudent().getAcademicInfo().getBranch() != null
                        ? a.getStudent().getAcademicInfo().getBranch() : "Unknown"));

        List<AdvancedAnalyticsResponse.BranchAnalytics> branchWise = byBranch.entrySet().stream()
                .map(e -> {
                    List<Application> list = e.getValue();
                    long sel = countStatus(list, ApplicationStatus.SELECTED);
                    double rate = list.isEmpty() ? 0.0 : Math.round((sel * 10000.0 / list.size())) / 100.0;
                    double avgAts = list.stream()
                            .filter(a -> a.getAtsScore() != null && a.getAtsScore().getScore() != null)
                            .mapToDouble(a -> a.getAtsScore().getScore()).average().orElse(0.0);
                    return new AdvancedAnalyticsResponse.BranchAnalytics(e.getKey(), list.size(), sel, rate, Math.round(avgAts * 100.0) / 100.0);
                })
                .sorted(Comparator.comparingLong(AdvancedAnalyticsResponse.BranchAnalytics::totalApplications).reversed())
                .toList();

        // Company-wise
        Map<String, List<Application>> byCompany = apps.stream()
                .collect(Collectors.groupingBy(a -> a.getJob().getRecruiter().getCompanyInfo() != null
                        ? a.getJob().getRecruiter().getCompanyInfo().getCompanyName() : "Unknown"));

        List<AdvancedAnalyticsResponse.CompanyAnalytics> companyWise = byCompany.entrySet().stream()
                .map(e -> {
                    List<Application> list = e.getValue();
                    long hired = countStatus(list, ApplicationStatus.SELECTED);
                    long jobsPosted = list.stream().map(a -> a.getJob().getId()).distinct().count();
                    return new AdvancedAnalyticsResponse.CompanyAnalytics(e.getKey(), list.size(), hired, jobsPosted);
                })
                .sorted(Comparator.comparingLong(AdvancedAnalyticsResponse.CompanyAnalytics::hired).reversed())
                .toList();

        // CGPA brackets
        double[][] ranges = {{9, 10}, {8, 9}, {7, 8}, {6, 7}, {0, 6}};
        String[] labels = {"9.0-10.0", "8.0-8.9", "7.0-7.9", "6.0-6.9", "Below 6.0"};
        List<AdvancedAnalyticsResponse.CgpaAnalytics> cgpaAnalysis = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            double min = ranges[i][0], max = ranges[i][1];
            List<Application> inRange = apps.stream()
                    .filter(a -> {
                        Double cgpa = a.getStudent().getAcademicInfo() != null ? a.getStudent().getAcademicInfo().getCgpa() : null;
                        double c = cgpa != null ? cgpa : 0.0;
                        return c >= min && c < max;
                    }).toList();
            long sel = countStatus(inRange, ApplicationStatus.SELECTED);
            double rate = inRange.isEmpty() ? 0.0 : Math.round((sel * 10000.0 / inRange.size())) / 100.0;
            cgpaAnalysis.add(new AdvancedAnalyticsResponse.CgpaAnalytics(labels[i], inRange.size(), sel, rate));
        }

        // Skills demand
        List<Job> approvedJobs = jobRepository.findByApprovalStatus(ApprovalStatus.APPROVED);
        Map<String, Long> skillCounts = approvedJobs.stream()
                .flatMap(j -> j.getSkillsRequired().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        List<AdvancedAnalyticsResponse.SkillDemand> topSkills = skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new AdvancedAnalyticsResponse.SkillDemand(e.getKey(), e.getValue()))
                .toList();

        return new AdvancedAnalyticsResponse(
                new AdvancedAnalyticsResponse.Overview(totalApps, totalSelected, successRate),
                branchWise, companyWise, cgpaAnalysis, topSkills
        );
    }

    // ---------- Export ----------

    public List<Map<String, Object>> exportData(String type, String branch, String company) {
        List<Map<String, Object>> data = new ArrayList<>();

        if ("students".equalsIgnoreCase(type)) {
            List<Student> students = studentRepository.findAll();
            for (Student s : students) {
                if (branch != null && !branch.isBlank()
                        && (s.getAcademicInfo() == null || !branch.equalsIgnoreCase(s.getAcademicInfo().getBranch()))) continue;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Registration Number", s.getRegistrationNumber());
                row.put("Name", studentName(s));
                row.put("Email", s.getUser().getEmail());
                row.put("Branch", s.getAcademicInfo() != null ? s.getAcademicInfo().getBranch() : "");
                row.put("CGPA", s.getAcademicInfo() != null ? s.getAcademicInfo().getCgpa() : "");
                row.put("Placement Status", s.getPlacementStatus().name());
                row.put("Package (LPA)", s.getPlacedPackage() != null ? s.getPlacedPackage() : "");
                data.add(row);
            }
        } else if ("applications".equalsIgnoreCase(type)) {
            List<Application> apps = applicationRepository.findAll();
            for (Application a : apps) {
                if (branch != null && !branch.isBlank()
                        && (a.getStudent().getAcademicInfo() == null || !branch.equalsIgnoreCase(a.getStudent().getAcademicInfo().getBranch()))) continue;
                if (company != null && !company.isBlank()
                        && (a.getJob().getRecruiter().getCompanyInfo() == null
                        || !company.equalsIgnoreCase(a.getJob().getRecruiter().getCompanyInfo().getCompanyName()))) continue;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Student Reg No", a.getStudent().getRegistrationNumber());
                row.put("Student Name", studentName(a.getStudent()));
                row.put("Branch", a.getStudent().getAcademicInfo() != null ? a.getStudent().getAcademicInfo().getBranch() : "");
                row.put("CGPA", a.getStudent().getAcademicInfo() != null ? a.getStudent().getAcademicInfo().getCgpa() : "");
                row.put("Job Title", a.getJob().getTitle());
                row.put("Company", a.getJob().getRecruiter().getCompanyInfo() != null ? a.getJob().getRecruiter().getCompanyInfo().getCompanyName() : "");
                row.put("Status", a.getStatus().name());
                row.put("ATS Score", a.getAtsScore() != null ? a.getAtsScore().getScore() : "");
                row.put("Applied Date", a.getAppliedAt() != null ? a.getAppliedAt().toLocalDate().toString() : "");
                data.add(row);
            }
        } else if ("jobs".equalsIgnoreCase(type)) {
            List<Job> jobs = jobRepository.findAll();
            for (Job j : jobs) {
                if (company != null && !company.isBlank()
                        && (j.getRecruiter().getCompanyInfo() == null
                        || !company.equalsIgnoreCase(j.getRecruiter().getCompanyInfo().getCompanyName()))) continue;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Job Title", j.getTitle());
                row.put("Company", j.getRecruiter().getCompanyInfo() != null ? j.getRecruiter().getCompanyInfo().getCompanyName() : "");
                row.put("Location", j.getLocation());
                row.put("Type", j.getJobType().name());
                row.put("Salary Range", (j.getSalaryMin() != null ? j.getSalaryMin() : "") + "-" + (j.getSalaryMax() != null ? j.getSalaryMax() : "") + " " + j.getSalaryType().name());
                row.put("Status", j.getApprovalStatus().name());
                row.put("Posted Date", j.getCreatedAt() != null ? j.getCreatedAt().toLocalDate().toString() : "");
                data.add(row);
            }
        }
        return data;
    }

    // ---------- Helpers ----------

    private long countStatus(List<Application> apps, ApplicationStatus status) {
        return apps.stream().filter(a -> a.getStatus() == status).count();
    }

    private String studentName(Student s) {
        return s.getPersonalInfo() != null
                ? (safe(s.getPersonalInfo().getFirstName()) + " " + safe(s.getPersonalInfo().getLastName())).trim()
                : s.getRegistrationNumber();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private List<TrendPoint> buildTrend(List<LocalDateTime> dates) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Map<LocalDate, Long> counts = dates.stream()
                .filter(d -> d != null && !d.toLocalDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TrendPoint(e.getKey().toString(), e.getValue()))
                .toList();
    }
}