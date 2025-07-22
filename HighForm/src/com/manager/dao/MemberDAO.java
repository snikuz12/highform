package com.manager.dao;

import com.manager.model.Member;
import com.manager.service.MemberService;
import com.util.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
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

	// 유효한 회원만 조회
	public List<Member> getAvailableMembers() {
		List<Member> members = new ArrayList<>();
		String sql = "SELECT M.ID, M.LOGIN_ID, M.PHONE, M.EMAIL, M.NAME, C.COURSE_NAME, M.ROLE " + "FROM MEMBERS M "
				+ "LEFT JOIN ENROLLMENT E ON M.ID = E.MEMBER_ID " + "LEFT JOIN COURSE C ON E.COURSE_ID = C.COURSE_ID "
				+ "WHERE M.DEL_YN = 'N' " + "ORDER BY M.ID, C.COURSE_NAME";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Member member = new Member();

				member.setMemberId(rs.getInt("ID"));
				member.setMemberLoginId(rs.getString("LOGIN_ID"));
				member.setPhoneNumber(rs.getString("PHONE"));
				member.setEmail(rs.getString("EMAIL"));
				member.setMemberName(rs.getString("NAME"));
				member.setAffiliation(rs.getString("COURSE_NAME")); // 추가
				member.setPosition(rs.getString("ROLE"));

				members.add(member);
			}

			System.out.println("회원 목록 전체 조회 완료: " + members.size() + "건");

		} catch (SQLException e) {
			System.err.println("회원 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}

		return members;
	}

	// 모든 회원 조회
	public List<Member> getAllMembers() {
		List<Member> members = new ArrayList<>();
		
		String sql = "SELECT M.ID, M.LOGIN_ID, M.PHONE, M.EMAIL, M.NAME, C.COURSE_NAME, M.ROLE " + "FROM MEMBERS M "
				+ "LEFT JOIN ENROLLMENT E ON M.ID = E.MEMBER_ID " + "LEFT JOIN COURSE C ON E.COURSE_ID = C.COURSE_ID "
				+ "ORDER BY M.ID, C.COURSE_NAME";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Member member = new Member();

				member.setMemberId(rs.getInt("ID"));
				member.setMemberLoginId(rs.getString("LOGIN_ID"));
				member.setPhoneNumber(rs.getString("PHONE"));
				member.setEmail(rs.getString("EMAIL"));
				member.setMemberName(rs.getString("NAME"));
				member.setAffiliation(rs.getString("COURSE_NAME")); // 추가
				member.setPosition(rs.getString("ROLE"));

				members.add(member);
			}

			System.out.println("회원 목록 전체 조회 완료: " + members.size() + "건");

		} catch (SQLException e) {
			System.err.println("회원 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}

		return members;
	}

	// 시퀀스 번호 받아오기(회원 등록에서 사용)
	public long getNextMemberId() {
		String sql = "SELECT SYS.MEMBERS_SEQ.NEXTVAL FROM dual";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				return rs.getLong(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1; // 오류 시
	}

	// 회원 등록
	public int addMemberAndReturnId(Member member) {

		String password = "1234"; // 초기 비밀번호

		String sql = "INSERT INTO MEMBERS (ID, LOGIN_ID, PASSWORD, PHONE, EMAIL, NAME, ROLE, CREATED_AT, UPDATED_AT)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";
		String[] generatedColumns = { "ID" };

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, generatedColumns)) {

			pstmt.setLong(1, member.getMemberId()); // id
			pstmt.setString(2, member.getMemberLoginId()); // login_id
			pstmt.setString(3, password);
			pstmt.setString(4, member.getPhoneNumber());
			pstmt.setString(5, member.getEmail());
			pstmt.setString(6, member.getMemberName());
			pstmt.setString(7, member.getPosition());

			int result = pstmt.executeUpdate();

			if (result > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						return rs.getInt(1); // 생성된 ID 반환
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1; // 실패 시
	}

	// 회원 수정
	public boolean updateMember(Member member) {
		String sql = "UPDATE MEMBERS SET PHONE = ?, EMAIL = ?, NAME = ?, ROLE = ?, UPDATED_AT = SYSDATE "
				+ "WHERE ID = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, member.getPhoneNumber());
			pstmt.setString(2, member.getEmail());
			pstmt.setString(3, member.getMemberName());
			pstmt.setString(4, member.getPosition());
			pstmt.setLong(5, member.getMemberId());

			int result = pstmt.executeUpdate();
			System.out.println("회원 수정 결과: " + result + "건 수정");
			return result > 0;

		} catch (SQLException e) {
			System.err.println("회원 수정 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// 회원 삭제(실제 삭제가 아니라 del_yn='Y'로 처리)
	public boolean deleteMember(long memberId) {
		String sql = "UPDATE MEMBERS SET DEL_YN = 'Y' WHERE ID = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, memberId);
			int result = pstmt.executeUpdate();
			System.out.println("회원 탈퇴 처리 결과: " + result + "건 처리");
			return result > 0;

		} catch (SQLException e) {
			System.err.println("회원 탈퇴 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// 이름 또는 연락처로 회원 조회 (전화번호는 '-' 제거 후 비교)
	public List<Member> getMemberByKeyword(String keyword) {
		List<Member> members = new ArrayList<>();

		String sql = "SELECT ID, LOGIN_ID, PASSWORD, PHONE, EMAIL, NAME, ROLE, CREATED_AT, DEL_YN "
				+ "FROM MEMBERS WHERE DEL_YN = 'N' AND (NAME LIKE ? OR REPLACE(PHONE, '-', '') LIKE ?)";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, "%" + keyword + "%");
			pstmt.setString(2, "%" + keyword.replaceAll("-", "") + "%");

			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				Member member = new Member();
				member.setMemberId(rs.getInt("id"));
				member.setMemberLoginId(rs.getString("login_id"));
				member.setPassword(rs.getString("password"));
				member.setPhoneNumber(rs.getString("phone"));
				member.setEmail(rs.getString("email"));
				member.setMemberName(rs.getString("name"));
				member.setPosition(rs.getString("role"));

				members.add(member);
			}

		} catch (SQLException e) {
			System.err.println("회원 검색 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}

		return members;
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