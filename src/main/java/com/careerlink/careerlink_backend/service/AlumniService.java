package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.AlumniProfileUpdateRequest;
import com.careerlink.careerlink_backend.dto.request.ExternalLinkDto;
import com.careerlink.careerlink_backend.dto.response.AlumniResponse;
import com.careerlink.careerlink_backend.entity.Alumni;
import com.careerlink.careerlink_backend.entity.ExternalLink;
import com.careerlink.careerlink_backend.entity.User;
import com.careerlink.careerlink_backend.entity.embeddable.CloudinaryFile;
import com.careerlink.careerlink_backend.entity.embeddable.CurrentRole;
import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.embeddable.SocialLinks;
import com.careerlink.careerlink_backend.exception.FileUploadException;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.repository.AlumniRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlumniService {

    private final AlumniRepository alumniRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public Alumni getCurrentAlumni(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return alumniRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Alumni profile not found"));
    }

    public AlumniResponse getProfile(Authentication auth) {
        return toResponse(getCurrentAlumni(auth));
    }

    @Transactional
    public AlumniResponse updateProfile(Authentication auth, AlumniProfileUpdateRequest req) {
        Alumni alumni = getCurrentAlumni(auth);

        PersonalInfo personalInfo = alumni.getPersonalInfo() != null ? alumni.getPersonalInfo() : new PersonalInfo();
        personalInfo.setFirstName(req.firstName());
        personalInfo.setLastName(req.lastName());
        personalInfo.setPhoneNumber(req.phoneNumber());
        alumni.setPersonalInfo(personalInfo);

        CurrentRole currentRole = alumni.getCurrentRole() != null ? alumni.getCurrentRole() : new CurrentRole();
        currentRole.setCompany(req.currentCompany());
        currentRole.setDesignation(req.currentDesignation());
        currentRole.setLocation(req.currentLocation());
        currentRole.setExperience(req.experience() != null ? req.experience() : 0);
        currentRole.setStartDate(req.startDate());
        alumni.setCurrentRole(currentRole);

        SocialLinks socialLinks = alumni.getSocialLinks() != null ? alumni.getSocialLinks() : new SocialLinks();
        socialLinks.setLinkedin(req.linkedin());
        socialLinks.setGithub(req.github());
        socialLinks.setPortfolio(req.portfolio());
        socialLinks.setTwitter(req.twitter());
        alumni.setSocialLinks(socialLinks);

        replaceExternalLinks(alumni, req.externalLinks());

        Alumni saved = alumniRepository.save(alumni);
        return toResponse(saved);
    }
    private void replaceExternalLinks(Alumni alumni, List<ExternalLinkDto> dtos) {
        alumni.getExternalLinks().clear();
        if (dtos == null) return;
        for (ExternalLinkDto dto : dtos) {
            ExternalLink l = new ExternalLink();
            l.setAlumni(alumni);
            l.setName(dto.name());
            l.setUrl(dto.url());
            alumni.getExternalLinks().add(l);
        }
    }
    public AlumniResponse toResponse(Alumni a) {
        return new AlumniResponse(
                a.getId(),
                a.getRegistrationNumber(),
                a.getUser().getEmail(),
                a.getPersonalInfo() != null ? a.getPersonalInfo().getFirstName() : null,
                a.getPersonalInfo() != null ? a.getPersonalInfo().getLastName() : null,
                a.getPersonalInfo() != null ? a.getPersonalInfo().getPhoneNumber() : null,
                a.getAcademicInfo() != null ? a.getAcademicInfo().getBranch() : null,
                a.getAcademicInfo() != null ? a.getAcademicInfo().getGraduationYear() : null,
                a.getCurrentRole() != null ? a.getCurrentRole().getCompany() : null,
                a.getCurrentRole() != null ? a.getCurrentRole().getDesignation() : null,
                a.getCurrentRole() != null ? a.getCurrentRole().getLocation() : null,
                a.getCurrentRole() != null ? a.getCurrentRole().getExperience() : null,
                a.getSocialLinks() != null ? a.getSocialLinks().getLinkedin() : null,
                a.getSocialLinks() != null ? a.getSocialLinks().getGithub() : null,
                a.getSocialLinks() != null ? a.getSocialLinks().getPortfolio() : null,
                a.getVerificationStatus().name(),
                a.getPersonalInfo() != null ? a.getPersonalInfo().getProfilePictureUrl() : null,
                a.getSocialLinks() != null ? a.getSocialLinks().getTwitter() : null,
                a.getCurrentRole() != null ? a.getCurrentRole().getStartDate() : null,
                a.getExternalLinks().stream().map(l -> new ExternalLinkDto(l.getName(), l.getUrl())).toList()
        );
    }

    @Transactional
    public AlumniResponse uploadProfilePhoto(Authentication auth, MultipartFile file) {
        Alumni alumni = getCurrentAlumni(auth);

        if (file.isEmpty()) throw new FileUploadException("No file provided");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new FileUploadException("Profile photo must be an image file");
        }
        if (file.getSize() > 5 * 1024 * 1024) throw new FileUploadException("Profile photo must not exceed 5MB");

        CloudinaryFile uploaded = cloudinaryService.uploadFile(file, "careerlink/alumni-photos", "image");

        PersonalInfo personalInfo = alumni.getPersonalInfo() != null ? alumni.getPersonalInfo() : new PersonalInfo();
        personalInfo.setProfilePictureUrl(uploaded.getUrl());
        alumni.setPersonalInfo(personalInfo);

        return toResponse(alumniRepository.save(alumni));
    }
}
