package com.manager.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.util.DBConnection;

public class EnrollmentDAO {

	private Connection getConnection() throws SQLException {
		return DBConnection.getConnection();
	}

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Oracle JDBC 드라이버를 찾을 수 없습니다.", e);
		}
	}

	// 회원-강의 등록 (enrollment 테이블 insert)
	public boolean insertEnrollment(int memberId, int courseId) {
		String sql = "INSERT INTO enrollment (member_id, course_id) VALUES (?, ?)";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, memberId);
			pstmt.setInt(2, courseId);

			int result = pstmt.executeUpdate();
			System.out.println("Enrollment 등록 결과: " + result + "건 등록됨");
			return result > 0;

		} catch (SQLException e) {
			System.err.println("Enrollment 등록 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// DB 연결 테스트 메서드
	public boolean testConnection() {
		try (Connection conn = getConnection()) {
			System.out.println("Oracle DB 연결 성공 (EnrollmentDAO)");
			return true;
		} catch (SQLException e) {
			System.err.println("Oracle DB 연결 실패 (EnrollmentDAO): " + e.getMessage());
			return false;
		}
	}

	// 회원id를 통해 소속된 수강과정을 조회
	public List<String> getCourseNamesByMemberId(int memberId) {
		List<String> courseNames = new ArrayList<>();
		String sql = "SELECT c.course_name " + "FROM enrollment e " + "JOIN course c ON e.course_id = c.course_id "
				+ "WHERE e.member_id = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, memberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					courseNames.add(rs.getString("course_name"));
				}
			}

		} catch (SQLException e) {
			System.err.println("수강 과정 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}

		return courseNames;
	}
}
