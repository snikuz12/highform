package com.mypage.Model.attendance;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceApprovalRequest {
    private Long id;
    private String reason;
    private String proofFile;
    private String status;
    private LocalDate requestedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
}
