package com.careerlink.careerlink_backend.config;

import com.careerlink.careerlink_backend.entity.Admin;
import com.careerlink.careerlink_backend.entity.User;
import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.enums.Role;
import com.careerlink.careerlink_backend.entity.enums.RoleLevel;
import com.careerlink.careerlink_backend.repository.AdminRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.existsByRoleLevel(RoleLevel.SUPER_ADMIN)) {
            return; // already bootstrapped, skip
        }

        User user = new User();
        user.setEmail("superadmin@careerlink.com");
        user.setPassword(passwordEncoder.encode("SuperAdmin@123"));
        user.setRole(Role.ADMIN);
        user.setVerified(true);
        userRepository.save(user);

        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName("Super");
        personalInfo.setLastName("Admin");

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setRoleLevel(RoleLevel.SUPER_ADMIN);
        admin.setPersonalInfo(personalInfo);
        adminRepository.save(admin);

        System.out.println("==== Bootstrapped default Super Admin: superadmin@careerlink.com / SuperAdmin@123 ====");
    }
}
