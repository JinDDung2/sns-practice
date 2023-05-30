package com.example.fasns.domain.post.dto;

import com.example.fasns.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class PostDto {
    private Long id;

    private Long memberId;

    private String title;

    private String contents;

    private Long likeCount;

    private LocalDate createdDate;

    private LocalDateTime createdAt;

    public PostDto() {}

    @Builder
    public PostDto(Long id, Long memberId, String title, String contents, Long likeCount, LocalDate createdDate, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.contents = contents;
        this.likeCount = likeCount;
        this.createdDate = createdDate;
        this.createdAt = createdAt;
    }

    public Post toEntity() {
        return Post.builder()
                .memberId(memberId)
                .title(title)
                .contents(contents)
                .createdAt(createdAt)
                .createdDate(createdDate)
                .build();
    }
}
