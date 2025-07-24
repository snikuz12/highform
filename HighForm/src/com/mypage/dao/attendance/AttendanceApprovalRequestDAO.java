package com.mypage.dao.attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mypage.Model.attendance.AttendanceApprovalRequest;
import com.util.DBConnection;

public class AttendanceApprovalRequestDAO {
    public void insert(AttendanceApprovalRequest req) throws SQLException {
        // 1) id 컬럼에 시퀀스를 직접 호출하도록 SQL 수정
        String sql = "INSERT INTO attendance_approval_request "
            + "(id, reason, proof_file, status, requested_at, start_date, end_date, user_id) "
            + "VALUES (SEQ_ATT_APP_REQ.NEXTVAL, ?, ?, ?, SYSDATE, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            // 2) 파라미터 인덱스 변경: ? 순서에 맞게 세팅
            pstmt.setString(1, req.getReason());
            pstmt.setString(2, req.getProofFile());
            pstmt.setString(3, "progressing");  // 신청 시 고정
            pstmt.setDate(4, java.sql.Date.valueOf(req.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(req.getEndDate()));
            pstmt.setLong(6, req.getUserId());

            pstmt.executeUpdate();
        }
    }
}
