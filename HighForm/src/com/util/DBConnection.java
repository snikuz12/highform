package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DBConnection {
    private static String url;
    private static String user;
    private static String password;
    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    static {
        try {
            // 드라이버 등록 (최신 JDBC는 생략 가능)
            Class.forName(DRIVER);

            // db.properties 읽기 (src/main/resources 등 classpath 기준)
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getResourceAsStream("db.properties");
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(reader);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
                
                System.out.println(url);
                
            }
        } catch (Exception e) {
            throw new RuntimeException("DB 접속정보 로딩 실패: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        // 연결 정보 로그 (한 번만 출력, 또는 주석처리 가능)
        System.out.println("[DB] Connected to: " + conn.getMetaData().getURL());
        System.out.println("[DB] User: " + conn.getMetaData().getUserName());
        return conn;
    }

}
