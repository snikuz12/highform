package com.mypage.Model.assignment;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileLocation {
    private Long id;                // 파일 PK
    private String filePath;        // 파일경로
    private String fileType;        // 파일유형
    private Integer fileSize;       // 파일크기 (byte 단위 추천)
    private LocalDateTime uploadedAt;   // 업로드 시간
    private Long assignmentId;      // 과제ID
    private Long userId;            // 회원ID
}
