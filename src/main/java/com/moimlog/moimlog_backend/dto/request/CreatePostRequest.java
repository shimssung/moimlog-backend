package com.moimlog.moimlog_backend.dto.request;

import com.moimlog.moimlog_backend.entity.Post;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 게시글 작성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    
    @NotNull(message = "게시글 타입은 필수입니다.")
    private Post.PostType type;
    
    @Builder.Default
    private Boolean isAnonymous = false;
    
    private List<PostImageRequest> images;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostImageRequest {
        @NotBlank(message = "이미지 URL은 필수입니다.")
        @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
        private String imageUrl;
        
        @Builder.Default
        private Integer imageOrder = 0;
    }
}
