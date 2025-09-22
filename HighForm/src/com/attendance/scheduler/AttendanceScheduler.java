package com.attendance.scheduler;

import com.attendance.service.AttendanceCodeService;

/**
 * 출석 코드 스케줄러 - 앱과 별도로 실행되는 독립 프로세스
 * 
 * 실행 방법:
 * 1. JAR 파일 빌드: mvn clean package
 * 2. 스케줄러 실행: java -cp target/attendance.jar com.attendance.scheduler.AttendanceScheduler
 * 3. 또는 IDE에서 이 클래스를 직접 실행
 */
public class AttendanceScheduler {
    
    public static void main(String[] args) {
        System.out.println("=== 출석 코드 스케줄러 시작 ===");
        System.out.println("매일 오전 7시에 출석 코드를 생성하고 이메일로 발송합니다.");
        System.out.println("종료하려면 Ctrl+C를 누르세요.");
        
        // 테스트 메일 전송
        com.attendance.service.MailTest.sendTestMail();
        
        try {
            AttendanceCodeService service = AttendanceCodeService.getInstance();
            
            // Redis 연결 확인
            if (!service.isRedisConnected()) {
                System.err.println("Redis 연결 실패! Redis 서버가 실행 중인지 확인하세요.");
                System.exit(1);
            }
            
            // 스케줄러 시작 (무한 루프로 실행)
            service.startDailyScheduler();
            
        } catch (Exception e) {
            System.err.println("스케줄러 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}