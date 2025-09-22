package com.attendance.service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import redis.clients.jedis.Jedis;

/**
 * 출석 코드 관리 서비스 - 앱에서는 조회만, 스케줄러는 별도 실행
 */
public class AttendanceCodeService {

    private final Jedis jedis = new Jedis("localhost", 6379);
    private final Random random = new Random();
    private static final AttendanceCodeService instance = new AttendanceCodeService();

    private AttendanceCodeService() {}
    public static AttendanceCodeService getInstance() {
        return instance;
    }

    /**
     * Redis에서 오늘의 출석 코드 조회만 (생성하지 않음)
     * @return 출석 코드 (없으면 null)
     */
    public String getTodayCode() {
        LocalDate today = LocalDate.now();
        String todayCode = getCodeFromRedis(today);
        
        if (todayCode == null) {
            System.out.println("[앱] 오늘의 출석 코드가 Redis에 없습니다. 스케줄러가 실행되었는지 확인하세요.");
        } else {
            System.out.println("[앱] 오늘의 출석 코드 조회: " + todayCode);
        }
        
        return todayCode;
    }

    /**
     * 오늘 입력된 코드가 맞는지 확인 (Redis에서만 비교)
     */
    public boolean validateTodayCode(String inputCode) {
        if (inputCode == null || inputCode.trim().isEmpty()) {
            System.out.println("[출석검증] 입력 코드가 비어있습니다.");
            return false;
        }
        
        String todayCode = getCodeFromRedis(LocalDate.now());
        if (todayCode == null) {
            System.out.println("[출석검증] 오늘의 출석 코드가 Redis에 없습니다.");
            return false;
        }
        
        boolean isValid = inputCode.trim().equals(todayCode);
        System.out.println("[출석검증] 입력코드: " + inputCode + ", 실제코드: " + todayCode + ", 결과: " + isValid);
        return isValid;
    }

    /**
     * 특정 날짜의 출석 코드 조회
     */
    public String getCodeByDate(LocalDate date) {
        return getCodeFromRedis(date);
    }

    /**
     * Redis에서 코드 조회
     */
    private String getCodeFromRedis(LocalDate date) {
        return jedis.get(date.toString());
    }

    /**
     * Redis 연결 상태 확인
     */
    public boolean isRedisConnected() {
        try {
            String response = jedis.ping();
            System.out.println("[Redis] 연결 상태: " + response);
            return "PONG".equals(response);
        } catch (Exception e) {
            System.err.println("[Redis] 연결 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * Redis 연결 종료
     */
    public void closeRedisConnection() {
        if (jedis != null) {
            jedis.close();
        }
    }

    // ========== 아래는 스케줄러 전용 메서드들 (앱에서 사용 금지) ==========

    /**
     * 출석 코드 생성 (4자리 숫자) - 스케줄러 전용
     */
    private String generateDailyCode(LocalDate date) {
        String code = String.format("%04d", random.nextInt(10000));
        return code;
    }

    /**
     * Redis에 코드 저장 (24시간 TTL) - 스케줄러 전용
     */
    private void storeCodeInRedis(LocalDate date, String code) {
        jedis.set(date.toString(), code);
        jedis.expire(date.toString(), 86400); // 24시간 유지
    }

    /**
     * 출석 코드 이메일 전송 - 스케줄러 전용
     */
    private void sendAttendanceCodeEmail(String recipientEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("sana2d2v@gmail.com", "mdez ynqs eqrf nqxl"); 
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sana2d2v@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🔐 오늘의 출석 코드입니다");
            message.setText("오늘의 출석 코드는 [" + code + "] 입니다.\n출석 시 정확히 입력해주세요.");

            Transport.send(message);
            System.out.println("[스케줄러] 메일 발송 완료: " + code);

        } catch (MessagingException e) {
            System.err.println("[스케줄러] 메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 매일 오전 7시 스케줄러 - 별도 프로세스에서만 실행
     * ⚠️ 주의: 앱에서 이 메서드를 호출하지 마세요!
     */
    public void startDailyScheduler() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    LocalDate today = LocalDate.now();
                    String code = generateDailyCode(today);
                    storeCodeInRedis(today, code);
                    sendAttendanceCodeEmail("vsana2d2v@gmail.com", code);
                    System.out.println("[스케줄러] " + today + " 출석 코드 생성 및 발송 완료: " + code);
                } catch (Exception e) {
                    System.err.println("[스케줄러] 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, getNext7amTime(), 1000 * 60 * 60 * 24); // 매일 24시간마다 반복
        
        System.out.println("[스케줄러] 매일 오전 7시 출석 코드 자동 발송 시작");
        
        // 프로그램 종료 방지를 위한 무한 대기
        while (true) {
            try {
                Thread.sleep(1000 * 60 * 60); // 1시간마다 체크
            } catch (InterruptedException e) {
                System.out.println("[스케줄러] 종료됨");
                break;
            }
        }
    }

    /**
     * 다음 오전 7시 시각을 반환
     */
    private Date getNext7amTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        if (cal.getTime().before(new Date())) {
            cal.add(Calendar.DAY_OF_MONTH, 1); // 이미 지났으면 다음 날로
        }
        
        return cal.getTime();
    }

    /**
     * 테스트용 - 즉시 코드 생성 및 발송 (개발/디버그 용도만)
     */
    public void testImmediateSend() {
        LocalDate today = LocalDate.now();
        String code = generateDailyCode(today);
        storeCodeInRedis(today, code);
        sendAttendanceCodeEmail("vsana2d2v@gmail.com", code);
        System.out.println("[테스트] 즉시 발송 완료: " + code);
    }
}