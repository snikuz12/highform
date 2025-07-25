package com.mypage.dao.attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mypage.Model.attendance.AttendanceApprovalRequest;
import com.util.DBConnection;

public class AttendanceApprovalRequestDAO {
    public void insert(AttendanceApprovalRequest req) throws SQLException {
        // status 값은 필요시 상수로 관리
        final String STATUS_PROGRESSING = "progressing";
        
        String sql = "INSERT INTO attendance_approval_request "
            + "(id, reason, proof_file, status, requested_at, start_date, end_date, user_id) "
            + "VALUES (SEQ_ATT_APP_REQ.NEXTVAL, ?, ?, ?, SYSDATE, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, req.getReason());
            pstmt.setString(2, req.getProofFile());   // null 허용 컬럼 확인 필요
            pstmt.setString(3, STATUS_PROGRESSING);
            pstmt.setDate(4, java.sql.Date.valueOf(req.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(req.getEndDate()));
            pstmt.setLong(6, req.getUserId());

            pstmt.executeUpdate();
        }
    }
}
