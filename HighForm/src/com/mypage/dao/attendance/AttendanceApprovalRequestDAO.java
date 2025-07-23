package com.mypage.dao.attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mypage.Model.attendance.AttendanceApprovalRequest;
import com.util.DBConnection;

public class AttendanceApprovalRequestDAO {
    public void insert(AttendanceApprovalRequest req) throws SQLException {
        String sql = "INSERT INTO attendance_approval_request "
            + "(reason, proof_file, status, requested_at, start_date, end_date, user_id) "
            + "VALUES (?, ?, ?, SYSDATE, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, req.getReason());
            pstmt.setString(2, req.getProofFile());
            pstmt.setString(3, "progressing");  // 신청시 고정

            // LocalDate → java.sql.Date 변환 후 setDate
            pstmt.setDate(4, java.sql.Date.valueOf(req.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(req.getEndDate()));
            pstmt.setLong(6, req.getUserId());

            pstmt.executeUpdate();
        }
    }
}
