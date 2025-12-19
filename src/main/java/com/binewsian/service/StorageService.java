package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file) throws BiNewsianException;
    void deleteFile(String key) throws BiNewsianException;
    String getPresignedUrl(String key) throws BiNewsianException;
    String getPublicUrl(String key) throws BiNewsianException;
}
