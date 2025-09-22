package com.mypage.dao.attendance;

import com.mypage.Model.attendance.Attendance;
import com.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {
    // 싱글톤 패턴
    private static AttendanceDAO instance;

    private AttendanceDAO() {}

    public static AttendanceDAO getInstance() {
        if (instance == null) {
            instance = new AttendanceDAO();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // 출결 리스트 조회 (15개씩 페이징, PreparedStatement)
    public List<Attendance> getAttendanceList(Long userId, int offset, int limit) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql =
        		"SELECT * FROM ( " +
        		"  SELECT  a.id, a.user_id,                     " +
        		"          a.check_in, a.check_out, a.status,   " +
        		"          /* check_in이 NULL이면 테이블 컬럼,            */ " +
        		"          /* check_in이 존재하면 그 날짜를 사용         */ " +
        		"          COALESCE( a.attendance_date, TRUNC(a.check_in) ) AS ATTENDANCE_DATE," +
        		"          ROW_NUMBER() OVER (ORDER BY a.attendance_date DESC) rn " +  // ← 정렬도 날짜 기준
        		"  FROM    attendance a                       " +
        		"  WHERE   a.user_id = ?                      " +
        		") WHERE rn > ? AND rn <= ?";
        System.out.println("[DAO][PreparedStatement] SQL = " + sql);
        System.out.printf("[DAO][PreparedStatement] params: userId=%d, offset=%d, limit=%d%n", userId, offset, limit);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, offset + limit);
            ResultSet rs = pstmt.executeQuery();
            int rowCnt = 0;
            while (rs.next()) {
                rowCnt++;

                Attendance att = new Attendance();
                att.setId(rs.getLong("ID"));

                /* 날짜만 필요한 컬럼 → LocalDate */
                Date sqlDate = rs.getDate("ATTENDANCE_DATE");
                if (sqlDate != null) {
                    att.setAttendanceDate(sqlDate.toLocalDate());
                }

                /* 체크인/아웃 → LocalDateTime */
                Timestamp inTs = rs.getTimestamp("CHECK_IN");
                if (inTs != null) {
                    att.setCheckIn(inTs.toLocalDateTime());
                }

                Timestamp outTs = rs.getTimestamp("CHECK_OUT");
                if (outTs != null) {
                    att.setCheckOut(outTs.toLocalDateTime());   // ← 버그 수정
                }

                /* 상태 코드 → enum */
                String statusCd = rs.getString("STATUS");
                if (statusCd != null) {
                    att.setStatus(Attendance.AttendanceStatus.fromCode(statusCd));
                }

                att.setUserId(rs.getLong("USER_ID"));

                list.add(att);
            }

            System.out.println("[DAO][PreparedStatement] Attendance row count: " + rowCnt);
        }
        return list;
    }

    // 출결률 조회 (지각·조퇴 3번=결석 1번으로 환산)
    public double getAttendanceRate(Long userId) throws SQLException {
        String sql = "SELECT CASE WHEN COUNT(*) = 0 THEN 0 " +
        	    " ELSE ROUND(( " +
        	    "   SUM(CASE " +
        	    "     WHEN status IN ('PRESENT', 'EXCUSED') THEN 1 " +
        	    "     WHEN status IN ('LATE', 'EARLY_LEAVE') THEN 1.0/3 " +
        	    "     ELSE 0 " +
        	    "   END) / COUNT(*) " +
        	    " ) * 100, 2) END AS attendance_rate " +
        	    "FROM attendance WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }


    // Statement로 실행해서 직접 결과 비교 (문제 확인)
    public void testDirectStatement() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM ( " +
                "SELECT a.*, ROW_NUMBER() OVER (ORDER BY check_in DESC NULLS LAST) rn " +
                "FROM attendance a WHERE user_id = 1 ) WHERE rn > 0 AND rn <= 15";
            System.out.println("[DAO][Statement] SQL = " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            int cnt = 0;
            while (rs.next()) cnt++;
            System.out.println("[DAO][Statement] 직접 Statement로 실행 row count: " + cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 출결 전체 개수 조회
    public int getAttendanceCount(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
