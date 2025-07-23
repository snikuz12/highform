package com.mypage.Model.assignment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmit {

    private Long userId;              // 회원 ID
    private Long assignmentId;        // 과제 ID

    private String submitTitle;       // 제출 제목  (기존 title → submitTitle 로 의미 분리)
    private String content;           // 제출 내용
    private LocalDateTime submittedAt;

    private String assignmentTitle;   // ★ 과제 테이블의 제목
    private String curriculumName;    // (필요 시 유지)
    private Long fileLocationId;
}
