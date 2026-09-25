package com.careerlink.careerlink_backend.mapper;

import com.careerlink.careerlink_backend.dto.response.StudentPlacementResponse;
import com.careerlink.careerlink_backend.entity.StudentPlacement;
import org.springframework.stereotype.Component;

@Component
public class StudentPlacementMapper {
    public StudentPlacementResponse toResponse(StudentPlacement p) {
        return new StudentPlacementResponse(
                p.getId(), p.getRelatedApplicationId(),  p.getCompany(), p.getJobTitle(), p.getPackageOffered(),
                p.getOfferDate(), p.getJoiningDate(), p.getStatus().name(), p.isPrimary()
        );
    }
}
