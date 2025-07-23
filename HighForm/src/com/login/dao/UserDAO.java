package com.login.dao;

import com.login.model.User;
import com.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Singleton 패턴
    private static UserDAO instance;

    private UserDAO() {
        // DBConnection에서 모든 DB 설정을 처리하므로 생성자는 비어있음
    }

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }


    public User login(String loginId, String password) throws SQLException {
        String sql = UserSQL.SELECT_USER_LOGIN;

        try (Connection conn = getConnection()) {
            System.out.println("[UserDAO] 현재 접속된 유저: " + conn.getMetaData().getUserName());
            System.out.println("[UserDAO] 실행할 SQL: " + sql);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 파라미터 바인딩 전 로깅
                System.out.println("=== 파라미터 바인딩 ===");
                System.out.println("loginId 원본: [" + loginId + "]");
                System.out.println("loginId 길이: " + (loginId != null ? loginId.length() : "null"));
                System.out.println("password 원본: [" + password + "]");
                System.out.println("password 길이: " + (password != null ? password.length() : "null"));

                pstmt.setString(1, loginId);
                pstmt.setString(2, password);

                System.out.println("=== SQL 실행 ===");
                ResultSet rs = pstmt.executeQuery();

                // 결과 확인을 위한 추가 디버깅
                boolean hasResult = false;
                if (rs.next()) {
                    hasResult = true;
                    System.out.println("=== 조회 성공 ===");
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setLoginId(rs.getString("login_id"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setRole(rs.getString("role"));
                    user.setPhone(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));

                    System.out.println("조회된 사용자: " + user.getName() + " (" + user.getLoginId() + ")");
                    return user;
                } else {
                    System.out.println("=== 조회 실패 - 결과 없음 ===");
                }

                // 실제 DB에 어떤 데이터가 있는지 확인해보자
                System.out.println("=== DB 데이터 확인 ===");
                String checkSql = "SELECT login_id, password, del_yn FROM user_info WHERE del_yn = 'N'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                     ResultSet checkRs = checkStmt.executeQuery()) {

                    int count = 0;
                    while (checkRs.next()) {
                        count++;
                        String dbLoginId = checkRs.getString("login_id");
                        String dbPassword = checkRs.getString("password");
                        String delYn = checkRs.getString("del_yn");

                        System.out.println("DB 사용자 " + count + ": [" + dbLoginId + "] / [" + dbPassword + "] / del_yn:[" + delYn + "]");

                        // 입력값과 DB값 비교
                        boolean loginIdMatch = dbLoginId != null && dbLoginId.equals(loginId);
                        boolean passwordMatch = dbPassword != null && dbPassword.equals(password);

                        System.out.println("  -> loginId 매치: " + loginIdMatch);
                        System.out.println("  -> password 매치: " + passwordMatch);
                    }

                    if (count == 0) {
                        System.out.println("DB에 del_yn='N'인 사용자가 없습니다!");
                    }
                }
            }
        }
        return null;
    }

    // 사용자 추가 (관리자용)
    public boolean insertUser(User user) throws SQLException {
        String sql = UserSQL.INSERT_USER;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getLoginId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getName());
            pstmt.setString(6, user.getRole());

            int result = pstmt.executeUpdate();
            return result > 0;
        }
    }

    // login_id 중복 체크
    public boolean isLoginIdExists(String loginId) throws SQLException {
        String sql = UserSQL.CHECK_LOGIN_ID_DUPLICATE;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        }
        return false;
    }

    // 모든 사용자 조회
    public List<User> getAllUsers() throws SQLException {
        String sql = UserSQL.SELECT_ALL_USERS;
        List<User> users = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setLoginId(rs.getString("login_id"));
                user.setName(rs.getString("name"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                users.add(user);
            }
        }
        return users;
    }
}