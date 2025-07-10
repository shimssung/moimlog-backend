package com.moimlog.moimlog_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 서비스
 * 이메일 발송 관련 기능을 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    /**
     * 인증 코드 이메일 발송
     * @param to 수신자 이메일
     * @param verificationCode 인증 코드
     */
    public void sendVerificationEmail(String to, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[모임로그] 이메일 인증 코드");
            message.setText(createVerificationEmailContent(verificationCode));
            
            mailSender.send(message);
            log.info("인증 이메일 발송 완료: {}", to);
        } catch (Exception e) {
            log.error("인증 이메일 발송 실패: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
    
    /**
     * 인증 이메일 내용 생성
     * @param verificationCode 인증 코드
     * @return 이메일 내용
     */
    private String createVerificationEmailContent(String verificationCode) {
        return String.format(
            "안녕하세요! 모임로그입니다.\n\n" +
            "회원가입을 위한 이메일 인증 코드입니다.\n\n" +
            "인증 코드: %s\n\n" +
            "이 코드는 10분 후에 만료됩니다.\n" +
            "본인이 요청하지 않은 경우 이 이메일을 무시하세요.\n\n" +
            "감사합니다.\n" +
            "모임로그 팀",
            verificationCode
        );
    }
} 