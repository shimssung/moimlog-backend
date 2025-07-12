package com.moimlog.moimlog_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS S3 설정을 위한 ConfigurationProperties 클래스
 */
@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Config {
    
    private boolean enabled = false;
    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private String bucket;
} 