package com.mypage.Model.assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentStatus {
    private int no;
    private String curriculumName;
    private String assignmentTitle;
    private String status; // "제출" 또는 "미제출"
    private LocalDate deadline;

}