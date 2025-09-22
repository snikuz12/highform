package com.attendance.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class MailTest {
    public static void sendTestMail() {
        String username = "sana2d2v@gmail.com";
        String password = "mdez ynqs eqrf nqxl";
        String to = "vsana2d2v@gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // 🔍 SMTP 로그 출력

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject("[출석 테스트] 이메일 발송 확인");
            message.setText("이 메일은 출석 스케줄러의 테스트 메시지입니다.\n정상 작동을 확인하세요!");

            Transport.send(message);
            System.out.println("✅ 테스트 메일 전송 완료");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
