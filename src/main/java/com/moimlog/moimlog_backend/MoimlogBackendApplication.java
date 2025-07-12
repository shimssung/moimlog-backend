package com.moimlog.moimlog_backend;

import com.moimlog.moimlog_backend.config.AwsS3Config;
import com.moimlog.moimlog_backend.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtConfig.class, AwsS3Config.class})
public class MoimlogBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoimlogBackendApplication.class, args);
	}

}
