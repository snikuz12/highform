package com.mypage.dao;


import com.mypage.Model.Schedule;
import com.util.DBConnection;         

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDaoImpl implements ScheduleDAO {

    /* DataSource 의존성 제거 → 기본 생성자만 유지 */
    public ScheduleDaoImpl() {}

    /* ----------나의 일정 조회 ---------- */

    @Override
    public List<Schedule> findByUser(Long userId) throws SQLException {
        String sql = """
            SELECT SCHED_ID, TITLE, MEMO, START_DATE, END_DATE
            FROM   CAL_SCHEDULE
            WHERE  USER_ID = ?
            ORDER  BY START_DATE
        """;

        List<Schedule> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);           // USER_ID가 VARCHAR2라도 setLong 가능 (숫자 ↔ 문자 변환)
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Schedule(
                            rs.getLong("SCHED_ID"),
                            userId,
                            rs.getString("TITLE"),
                            rs.getString("MEMO"),
                            rs.getDate("START_DATE").toLocalDate(),
                            rs.getDate("END_DATE").toLocalDate()
                    ));
                }
            }
        }
        list.forEach(System.out::println);
        return list;
    }

    
    /* ---------- 달력 조회 ---------- */
    @Override
    public List<Schedule> findByUserAndMonth(Long userId, YearMonth ym) throws SQLException {

        LocalDate first = ym.atDay(1);
        LocalDate last  = ym.atEndOfMonth();

        String sql = """
            SELECT SCHED_ID, TITLE, MEMO, START_DATE, END_DATE
            FROM   CAL_SCHEDULE
            WHERE  USER_ID    = ?
              AND  START_DATE <= ?
              AND  END_DATE   >= ?
            ORDER  BY START_DATE
        """;

        List<Schedule> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);                      
            ps.setDate(2, Date.valueOf(last));
            ps.setDate(3, Date.valueOf(first));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Schedule(
                            rs.getLong("SCHED_ID"),
                            userId,                                 
                            rs.getString("TITLE"),
                            rs.getString("MEMO"),
                            rs.getDate("START_DATE").toLocalDate(),
                            rs.getDate("END_DATE").toLocalDate()
                    ));
                }
            }
        }
        return list;
    }


    /* ---------- 저장 ---------- */
    @Override
    public long save(Schedule s) throws SQLException {
        String sql = """
            INSERT INTO CAL_SCHEDULE
            (SCHED_ID, USER_ID, TITLE, MEMO, START_DATE, END_DATE)
            VALUES (SEQ_CAL_SCHEDULE.NEXTVAL, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();      // ★ 변경
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "SCHED_ID" })) {

            ps.setLong(1, s.userId());
            ps.setString(2, s.title());
            ps.setString(3, s.memo());
            ps.setDate  (4, Date.valueOf(s.startDate()));
            ps.setDate  (5, Date.valueOf(s.endDate()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("PK retrieval failed");
    }

    /* ---------- 수정 ---------- */
    @Override
    public void update(Schedule s) throws SQLException {
        String sql = """
            UPDATE CAL_SCHEDULE
            SET   TITLE      = ?,
                  MEMO       = ?,
                  START_DATE = ?,
                  END_DATE   = ?,
                  UPT_DTTM   = SYSDATE
            WHERE SCHED_ID   = ?
              AND USER_ID    = ?
        """;

        try (Connection con = DBConnection.getConnection();      // ★ 변경
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.title());
            ps.setString(2, s.memo());
            ps.setDate  (3, Date.valueOf(s.startDate()));
            ps.setDate  (4, Date.valueOf(s.endDate()));
            ps.setLong  (5, s.id());
            ps.setLong(6, s.userId());
            ps.executeUpdate();
        }
    }

    /* ---------- 삭제 ---------- */
    @Override
    public void delete(long schedId) throws SQLException {
        String sql = "DELETE FROM CAL_SCHEDULE WHERE SCHED_ID = ?";

        try (Connection con = DBConnection.getConnection();      // ★ 변경
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, schedId);
            ps.executeUpdate();
        }
    }
}
