package com.manager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.manager.model.Course;
import com.util.DBConnection;

public class CourseDAO {

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

	// 모든 강의 조회
	public List<Course> getAllCourses() {
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT COURSE_ID, COURSE_NAME, START_DATE, END_DATE, INSTRUCTOR, MANAGER, NOTE FROM COURSE ORDER BY COURSE_ID";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Course course = new Course();

				course.setCourseId(rs.getInt("COURSE_ID"));
				course.setCourseName(rs.getString("COURSE_NAME"));
				course.setStartDate(rs.getString("START_DATE"));
				course.setEndDate(rs.getString("END_DATE"));
				course.setInstructor(rs.getString("INSTRUCTOR"));
				course.setManager(rs.getString("MANAGER"));
				course.setNote(rs.getString("NOTE"));
				courses.add(course);
			}

			System.out.println("강의 목록 조회 완료: " + courses.size() + "건");

		} catch (SQLException e) {
			System.err.println("강의 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}

		return courses;
	}

	// 강의 등록
	public boolean addCourse(Course course) {
		String sql = "INSERT INTO course (course_id, course_name, start_date, end_date, instructor, manager, note) "
				+ "VALUES (SYS.COURSE_SEQ.NEXTVAL, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, course.getCourseName());
			pstmt.setString(2, course.getStartDate());
			pstmt.setString(3, course.getEndDate());
			pstmt.setString(4, course.getInstructor());
			pstmt.setString(5, course.getManager());
			pstmt.setString(6, course.getNote());

			int result = pstmt.executeUpdate();
			return result > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 강의 수정
	public boolean updateCourse(Course course) {
		String sql = "UPDATE COURSE SET COURSE_NAME=?, START_DATE=?, END_DATE=?, INSTRUCTOR=?, MANAGER=?, NOTE=? WHERE COURSE_ID=?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, course.getCourseName());
			pstmt.setString(2, course.getStartDate());
			pstmt.setString(3, course.getEndDate());
			pstmt.setString(4, course.getInstructor());
			pstmt.setString(5, course.getManager());
			pstmt.setString(6, course.getNote());
			pstmt.setInt(7, course.getCourseId());

			int result = pstmt.executeUpdate();
			System.out.println("강의 수정 결과: " + result + "건 수정");
			return result > 0;

		} catch (SQLException e) {
			System.err.println("강의 수정 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// 강의 삭제
	public boolean deleteCourse(int courseId) {
		String sql = "DELETE FROM COURSE WHERE COURSE_ID = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, courseId);
			int result = pstmt.executeUpdate();
			System.out.println("강의 삭제 결과: " + result + "건 삭제");
			return result > 0;

		} catch (SQLException e) {
			System.err.println("강의 삭제 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// 특정 강의 조회 (필요시 사용)
	public int getCourseIdByName(String courseName) {
		String sql = "SELECT COURSE_ID FROM COURSE WHERE COURSE_NAME = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, courseName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("COURSE_ID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1; // 못 찾으면 -1 반환
	}

	public Course getCourseById(int courseId) {
		String sql = "SELECT COURSE_ID, COURSE_NAME, START_DATE, END_DATE, INSTRUCTOR, MANAGER, NOTE FROM COURSE WHERE COURSE_ID = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, courseId);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				Course course = new Course();
				course.setCourseId(rs.getInt("COURSE_ID"));
				course.setCourseName(rs.getString("COURSE_NAME"));
				course.setStartDate(rs.getString("START_DATE"));
				course.setEndDate(rs.getString("END_DATE"));
				course.setInstructor(rs.getString("INSTRUCTOR"));
				course.setManager(rs.getString("MANAGER"));
				course.setNote(rs.getString("NOTE"));
				return course;
			}

		} catch (SQLException e) {
			System.err.println("강의 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	// 멤버 등록시 강의 목록 드롭다운
	public List<String> getOnlyCourse() {
		List<String> course = new ArrayList<>();
		String sql = "SELECT COURSE_NAME FROM COURSE";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				course.add(rs.getString("COURSE_NAME"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return course;
	}

	// DB 연결 테스트 메서드
	public boolean testConnection() {
		try (Connection conn = getConnection()) {
			System.out.println("Oracle DB 연결 성공!");
			return true;
		} catch (SQLException e) {
			System.err.println("Oracle DB 연결 실패: " + e.getMessage());
			return false;
		}
	}
}