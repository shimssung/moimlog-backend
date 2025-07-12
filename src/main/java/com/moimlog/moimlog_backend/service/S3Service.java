package com.moimlog.moimlog_backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null));
        return amazonS3.getUrl(bucket, fileName).toString();
    }
    
    /**
     * Base64 이미지를 S3에 업로드
     * @param base64Image Base64 인코딩된 이미지 문자열
     * @param fileExtension 파일 확장자 (예: .jpg, .png)
     * @return S3 URL
     */
    public String uploadBase64Image(String base64Image, String fileExtension) throws IOException {
        try {
            // Base64 데이터에서 헤더 제거
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            
            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            
            // 파일명 생성
            String fileName = "profile-images/" + UUID.randomUUID().toString() + fileExtension;
            
            // S3에 업로드
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, 
                    new ByteArrayInputStream(imageBytes), null));
            
            String s3Url = amazonS3.getUrl(bucket, fileName).toString();
            log.info("S3 업로드 완료: {}", s3Url);
            
            return s3Url;
            
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("이미지 업로드에 실패했습니다.", e);
        }
    }
} 