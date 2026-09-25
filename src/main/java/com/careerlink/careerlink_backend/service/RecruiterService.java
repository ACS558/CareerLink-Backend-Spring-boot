package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.RecruiterProfileUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.RecruiterResponse;
import com.careerlink.careerlink_backend.entity.Recruiter;
import com.careerlink.careerlink_backend.entity.User;
import com.careerlink.careerlink_backend.entity.embeddable.CloudinaryFile;
import com.careerlink.careerlink_backend.entity.embeddable.CompanyInfo;
import com.careerlink.careerlink_backend.entity.embeddable.ContactPerson;
import com.careerlink.careerlink_backend.exception.FileUploadException;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.repository.RecruiterRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public Recruiter getCurrentRecruiter(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
    }

    public RecruiterResponse getProfile(Authentication auth) {
        return toResponse(getCurrentRecruiter(auth));
    }

    @Transactional
    public RecruiterResponse updateProfile(Authentication auth, RecruiterProfileUpdateRequest req) {
        Recruiter recruiter = getCurrentRecruiter(auth);

        CompanyInfo companyInfo = recruiter.getCompanyInfo() != null ? recruiter.getCompanyInfo() : new CompanyInfo();
        companyInfo.setCompanyName(req.companyName());
        companyInfo.setIndustry(req.industry());
        companyInfo.setLocation(req.location());
        companyInfo.setWebsite(req.website());
        companyInfo.setCompanySize(req.companySize());
        companyInfo.setDescription(req.description());
        recruiter.setCompanyInfo(companyInfo);

        ContactPerson contactPerson = recruiter.getContactPerson() != null ? recruiter.getContactPerson() : new ContactPerson();
        contactPerson.setName(req.contactName());
        contactPerson.setDesignation(req.contactDesignation());
        contactPerson.setPhoneNumber(req.contactPhone());
        contactPerson.setEmail(req.contactEmail());
        recruiter.setContactPerson(contactPerson);

        Recruiter saved = recruiterRepository.save(recruiter);
        return toResponse(saved);
    }

    public RecruiterResponse toResponse(Recruiter r) {
        return new RecruiterResponse(
                r.getId(),
                r.getUser().getEmail(),
                r.getCompanyInfo() != null ? r.getCompanyInfo().getCompanyName() : null,
                r.getCompanyInfo() != null ? r.getCompanyInfo().getIndustry() : null,
                r.getCompanyInfo() != null ? r.getCompanyInfo().getLocation() : null,
                r.getCompanyInfo() != null ? r.getCompanyInfo().getCompanyLogoUrl() : null,
                r.getCompanyInfo() != null ? r.getCompanyInfo().getWebsite() : null,
                r.getCompanyInfo() != null ? r.getCompanyInfo().getCompanySize() : null,
                r.getCompanyInfo() != null ? r.getCompanyInfo().getDescription() : null,
                r.getContactPerson() != null ? r.getContactPerson().getName() : null,
                r.getContactPerson() != null ? r.getContactPerson().getDesignation() : null,
                r.getContactPerson() != null ? r.getContactPerson().getPhoneNumber() : null,
                r.getContactPerson() != null ? r.getContactPerson().getEmail() : null,
                r.getVerificationStatus().name()
        );
    }

    @Transactional
    public RecruiterResponse uploadLogo(Authentication auth, MultipartFile file) {
        Recruiter recruiter = getCurrentRecruiter(auth);

        if (file.isEmpty()) throw new FileUploadException("No file provided");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new FileUploadException("Logo must be an image file");
        }
        if (file.getSize() > 5 * 1024 * 1024) throw new FileUploadException("Logo must not exceed 5MB");

        CloudinaryFile uploaded = cloudinaryService.uploadFile(file, "careerlink/company-logos", "image");

        CompanyInfo companyInfo = recruiter.getCompanyInfo() != null ? recruiter.getCompanyInfo() : new CompanyInfo();
        companyInfo.setCompanyLogoUrl(uploaded.getUrl());
        recruiter.setCompanyInfo(companyInfo);

        return toResponse(recruiterRepository.save(recruiter));
    }
}