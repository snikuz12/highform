package com.login.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String loginId;
    private String password;
    private String phone;
    private String email;
    private String name;
    private String role; // STUDENT, PROFESSOR, MANAGER
    private Timestamp createdAt;
    private String delYn;
    
    // 로그인용 생성자
    public User(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }
    
    // 관리자가 유저 추가할 때 사용할 생성자
    public User(String phone, String name, String role) {
        this.loginId = phone; // phone이 login_id가 됨
        this.password = "1234"; // 기본 패스워드
        this.phone = phone;
        this.name = name;
        this.role = role;
        this.delYn = "N";
    }
}