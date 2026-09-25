package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.entity.embeddable.CloudinaryFile;
import com.careerlink.careerlink_backend.exception.FileUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // resourceType: "image" for photos/logos, "raw" for PDFs
    public CloudinaryFile uploadFile(MultipartFile file, String folder, String resourceType) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", resourceType
            ));

            CloudinaryFile cf = new CloudinaryFile();
            cf.setUrl((String) result.get("secure_url"));
            cf.setPublicId((String) result.get("public_id"));
            cf.setUploadedAt(LocalDateTime.now());
            return cf;

        } catch (Exception e) {
            throw new FileUploadException("File upload failed: " + e.getMessage());
        }
    }

    public void deleteFile(String publicId, String resourceType) {
        if (publicId == null) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (Exception e) {
            // Non-fatal: don't block the operation over a cleanup failure, just log it
            System.err.println("Failed to delete old file from Cloudinary: " + e.getMessage());
        }
    }
}
