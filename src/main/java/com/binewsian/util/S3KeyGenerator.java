package com.binewsian.util;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class S3KeyGenerator {
    
    public String generateKey(MultipartFile file) {
        String uniqueId = generateUniqueId();
        String extension = getFileExtension(file.getOriginalFilename());
        return uniqueId + "." + extension;
    }

    private String generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        String random = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + random;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ""; 
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
