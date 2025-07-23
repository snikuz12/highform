package com.mypage.dao.assignment;

import lombok.Data;

/* ==========================================================
 *  ▶ 수강 중 모든 과제 + 제출 여부 DTO
 * ======================================================== */
// === DTO BEGIN
@Data
public class CourseAssignmentDTO {
    private Long assignmentId;
    private String assignmentTitle;
    private java.time.LocalDateTime endDate;  
    private boolean submitted;
}
