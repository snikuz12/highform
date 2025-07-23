package com.board.model;

import com.util.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends BaseEntity {
    private Long id;
    private Long boardId;
    private Long parentId;  // 0이면 최상위 댓글, 아니면 대댓글
    private String author;
    private String content;
    private Long userId;
    private boolean isOwner;  // 현재 사용자가 작성자인지 여부 (UI용)
}