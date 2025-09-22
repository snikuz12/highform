package com.login.dao;

public class UserSQL {

	// 로그인 쿼리 - login_id와 password로 사용자 조회
	public static final String SELECT_USER_LOGIN = """
			SELECT id, login_id, password, name, role, phone, email
			FROM user_info
			WHERE login_id = ? AND password = ? AND del_yn = 'N'
			""";

	// 사용자 추가 쿼리 (관리자가 유저 추가)
	public static final String INSERT_USER = """
			INSERT INTO user_info (login_id, password, phone, email, name, role)
			VALUES (?, ?, ?, ?, ?, ?)
			""";

	// 모든 사용자 조회 (삭제되지 않은)
	public static final String SELECT_ALL_USERS = """
			SELECT id, login_id, name, role, phone, email, created_at
			FROM user_info
			WHERE del_yn = 'N'
			ORDER BY created_at DESC
			""";

	// login_id 중복 체크
	public static final String CHECK_LOGIN_ID_DUPLICATE = """
			SELECT COUNT(*) as cnt
			FROM user_info
			WHERE login_id = ? AND del_yn = 'N'
			""";

	// 사용자 정보 수정
	public static final String UPDATE_USER = """
		    UPDATE user_info 
		    SET password = ?, phone = ?, email = ?, name = ?
		    WHERE id = ? AND del_yn = 'N'
		    """;

	// 사용자 삭제 (논리삭제)
	public static final String DELETE_USER = """
		    UPDATE user_info 
		    SET del_yn = 'Y'
		    WHERE id = ?
		    """;
}
