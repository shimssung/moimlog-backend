package com.moimlog.moimlog_backend.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 댓글 작성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
    
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
    
    @Builder.Default
    private Boolean isAnonymous = false;
    
    private Long parentId; // 대댓글인 경우 부모 댓글 ID
}
