package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.AlumniRegisterRequest;
import com.careerlink.careerlink_backend.dto.request.LoginRequest;
import com.careerlink.careerlink_backend.dto.request.RecruiterRegisterRequest;
import com.careerlink.careerlink_backend.dto.request.StudentRegisterRequest;
import com.careerlink.careerlink_backend.dto.response.AuthResponse;
import com.careerlink.careerlink_backend.entity.*;
import com.careerlink.careerlink_backend.entity.embeddable.AcademicInfo;
import com.careerlink.careerlink_backend.entity.embeddable.CompanyInfo;
import com.careerlink.careerlink_backend.entity.embeddable.ContactPerson;
import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.entity.enums.Role;
import com.careerlink.careerlink_backend.exception.DuplicateResourceException;
import com.careerlink.careerlink_backend.exception.InvalidCredentialsException;
import com.careerlink.careerlink_backend.repository.*;
import com.careerlink.careerlink_backend.security.CustomUserDetailsService;
import com.careerlink.careerlink_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RecruiterRepository recruiterRepository;
    private final AlumniRepository alumniRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final NotificationService notificationService;
    private final AdminRepository adminRepository;


    @Transactional
    public AuthResponse registerStudent(StudentRegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (studentRepository.existsByRegistrationNumber(req.registrationNumber().toUpperCase())) {
            throw new DuplicateResourceException("Registration number already registered");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.STUDENT);
        user.setVerified(true);
        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setRegistrationNumber(req.registrationNumber().toUpperCase());
        student.setPersonalInfo(new PersonalInfo());
        student.setAcademicInfo(new AcademicInfo());
        student.setRegistrationDate(LocalDateTime.now());
        studentRepository.save(student);

        return buildAuthResponse(user, req.registrationNumber());
    }

    @Transactional
    public AuthResponse registerRecruiter(RecruiterRegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.RECRUITER);
        user.setVerified(true);
        userRepository.save(user);

        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setCompanyName(req.companyName());
        companyInfo.setIndustry(req.industry());
        companyInfo.setLocation(req.location());

        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setName(req.contactName());
        contactPerson.setDesignation(req.contactDesignation());
        contactPerson.setPhoneNumber(req.contactPhone());
        contactPerson.setEmail(req.contactEmail());

        Recruiter recruiter = new Recruiter();
        recruiter.setUser(user);
        recruiter.setCompanyInfo(companyInfo);
        recruiter.setContactPerson(contactPerson);
        recruiterRepository.save(recruiter);

        notificationService.notifyAllAdmins(
                NotificationType.RECRUITER_PENDING,
                "New Recruiter Registration",
                req.companyName() + " has registered and is awaiting approval",
                "/admin/recruiters"
        );

        return buildAuthResponse(user, req.companyName());
    }

    @Transactional
    public AuthResponse registerAlumni(AlumniRegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (alumniRepository.existsByRegistrationNumber(req.registrationNumber().toUpperCase())) {
            throw new DuplicateResourceException("Registration number already registered");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.ALUMNI);
        user.setVerified(true);
        userRepository.save(user);

        AcademicInfo academicInfo = new AcademicInfo();
        academicInfo.setBranch(req.branch());
        academicInfo.setGraduationYear(req.graduationYear());

        Alumni alumni = new Alumni();
        alumni.setUser(user);
        alumni.setRegistrationNumber(req.registrationNumber().toUpperCase());
        alumni.setAcademicInfo(academicInfo);
        alumni.setPersonalInfo(new PersonalInfo());
        alumniRepository.save(alumni);

        notificationService.notifyAllAdmins(
                NotificationType.ALUMNI_PENDING,
                "New Alumni Registration",
                req.registrationNumber() + " has registered and is awaiting approval",
                "/admin/alumni"
        );


        return buildAuthResponse(user, req.registrationNumber());
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = resolveEmail(req.identifier());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        LocalDateTime previousLastLogin = user.getLastLogin();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user, email, previousLastLogin);
    }

    // Students/alumni can log in with registration number OR email;
    // if the identifier isn't a valid email shape, treat it as a reg. number and resolve to email.
    private String resolveEmail(String identifier) {
        if (identifier.contains("@")) {
            return identifier;
        }
        String regNo = identifier.toUpperCase();

        return studentRepository.findByRegistrationNumber(regNo)
                .map(s -> s.getUser().getEmail())
                .or(() -> alumniRepository.findByRegistrationNumber(regNo).map(a -> a.getUser().getEmail()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    private AuthResponse buildAuthResponse(User user, String displayName) {
        return buildAuthResponse(user, displayName, null);
    }

    private AuthResponse buildAuthResponse(User user, String displayName, LocalDateTime previousLastLogin) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getId(), user.getRole().name());

        String roleLevel = null;
        if (user.getRole() == Role.ADMIN) {
            roleLevel = adminRepository.findByUserId(user.getId())
                    .map(a -> a.getRoleLevel().name())
                    .orElse(null);
        }


        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name(), displayName, previousLastLogin, roleLevel);
    }
}