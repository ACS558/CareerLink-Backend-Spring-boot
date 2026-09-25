package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.ReferralRequest;
import com.careerlink.careerlink_backend.dto.response.ReferralResponse;
import com.careerlink.careerlink_backend.entity.Alumni;
import com.careerlink.careerlink_backend.entity.Referral;
import com.careerlink.careerlink_backend.entity.enums.ApprovalStatus;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.entity.enums.VerificationStatus;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.repository.ReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final AlumniService alumniService;
    private final NotificationService notificationService;

    @Transactional
    public ReferralResponse submitReferral(Authentication auth, ReferralRequest req) {
        Alumni alumni = alumniService.getCurrentAlumni(auth);

        if (alumni.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new UnauthorizedException("Your alumni account is not yet approved by admin");
        }

        Referral referral = new Referral();
        referral.setAlumni(alumni);
        referral.setCompany(req.company());
        referral.setRole(req.role());
        referral.setLocation(req.location());
        referral.setReferralLink(req.referralLink());
        referral.setDescription(req.description());
        referral.setApprovalStatus(ApprovalStatus.PENDING);

        Referral saved = referralRepository.save(referral);

        notificationService.notifyAllAdmins(
                NotificationType.REFERRAL_PENDING,
                "New Referral Pending Approval",
                req.company() + " referral submitted by alumni",
                "/admin/referrals"
        );

        return toResponse(saved);
    }

    public List<ReferralResponse> getMyReferrals(Authentication auth) {
        Alumni alumni = alumniService.getCurrentAlumni(auth);
        return referralRepository.findByAlumniId(alumni.getId())
                .stream().map(this::toResponse).toList();
    }

    public Page<ReferralResponse> getApprovedReferrals(Pageable pageable) {
        return referralRepository.findByApprovalStatusAndIsActiveTrue(ApprovalStatus.APPROVED, pageable)
                .map(this::toResponse);
    }

    public Referral getReferralEntity(Long id) {
        return referralRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found"));
    }

    public ReferralResponse toResponse(Referral r) {
        String alumniName = r.getAlumni().getPersonalInfo() != null
                ? (r.getAlumni().getPersonalInfo().getFirstName() + " " + r.getAlumni().getPersonalInfo().getLastName())
                : r.getAlumni().getRegistrationNumber();

        return new ReferralResponse(
                r.getId(), r.getCompany(), r.getRole(), r.getLocation(),
                r.getReferralLink(), r.getDescription(), alumniName,
                r.getApprovalStatus().name(), r.getCreatedAt()
        );
    }

    @Transactional
    public void deleteReferral(Authentication auth, Long referralId) {
        Alumni alumni = alumniService.getCurrentAlumni(auth);
        Referral referral = getReferralEntity(referralId);

        if (!referral.getAlumni().getId().equals(alumni.getId())) {
            throw new UnauthorizedException("You can only delete your own referrals");
        }
        referralRepository.delete(referral);
    }
}
