/* com.mypage.dao.ScheduleDAO */
package com.mypage.dao;


import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;

import com.mypage.Model.Schedule;

public interface ScheduleDAO {

    /** 해당 달 일정 조회 */
    List<Schedule> findByUserAndMonth(Long userId, YearMonth ym) throws SQLException;
    List<Schedule> findByUser(Long userId) throws SQLException;

    long save   (Schedule s) throws SQLException;
    void update (Schedule s) throws SQLException;
    void delete (long schedId) throws SQLException;
}
