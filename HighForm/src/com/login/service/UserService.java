package com.login.service;

import com.login.dao.UserDAO;
import com.login.model.User;
import java.util.List;

public class UserService {
    private static final UserDAO userDAO = UserDAO.getInstance();
    private static UserService instance;
    
    private UserService() {}
    
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    // 로그인 검증
    public User login(String loginId, String password) {
        try {
            // 입력값 검증
            if (loginId == null || loginId.trim().isEmpty()) {
                throw new RuntimeException("사용자 이름을 입력해주세요.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new RuntimeException("비밀번호를 입력해주세요.");
            }
            
            // DB에서 사용자 조회
            User user = userDAO.login(loginId.trim(), password.trim());
            if (user == null) {
                throw new RuntimeException("사용자 이름 또는 비밀번호가 일치하지 않습니다.");
            }
            
            return user;
            
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    // 사용자 추가 (관리자용)
    public boolean addUser(String phone, String name, String role, String email) {
        try {
            // 입력값 검증
            if (phone == null || phone.trim().isEmpty()) {
                throw new RuntimeException("전화번호를 입력해주세요.");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("이름을 입력해주세요.");
            }
            if (role == null || role.trim().isEmpty()) {
                throw new RuntimeException("역할을 선택해주세요.");
            }
            
            // 중복 체크 (전화번호가 login_id가 되므로)
            if (userDAO.isLoginIdExists(phone.trim())) {
                throw new RuntimeException("이미 등록된 전화번호입니다.");
            }
            
            // 사용자 생성 및 저장
            User user = new User(phone.trim(), name.trim(), role.trim());
            if (email != null && !email.trim().isEmpty()) {
                user.setEmail(email.trim());
            }
            
            return userDAO.insertUser(user);
            
        } catch (Exception e) {
            System.out.println("사용자 추가 실패: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    // 모든 사용자 조회
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (Exception e) {
            System.out.println("사용자 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("사용자 목록을 불러올 수 없습니다.");
        }
    }
    
    // 입력값 유효성 검사 메소드들
    public void validateLoginInput(String loginId, String password) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new RuntimeException("User name을 입력해주세요.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password를 입력해주세요.");
        }
    }
}