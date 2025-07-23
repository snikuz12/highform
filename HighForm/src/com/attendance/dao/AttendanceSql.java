package com.attendance.dao;

public class AttendanceSql {
   // ============ 출석 관련 SQL ============
    
    /**
     * 출석 기록 삽입
     */
    public static final String INSERT_ATTENDANCE = 
        "INSERT INTO attendance (id, attendance_date, check_in, status, user_id, created_at, updated_at) " +
        "VALUES (seq_attendance.NEXTVAL, ?, ?, ?, ?, SYSDATE, SYSDATE)";
    
    /**
     * 당일 출석 기록 조회
     */
    public static final String SELECT_TODAY_ATTENDANCE = 
        "SELECT id, attendance_date, check_in, check_out, status, user_id, created_at, updated_at " +
        "FROM attendance " +
        "WHERE user_id = ? AND attendance_date = ?";
    
    /**
     * 퇴실 시간 및 상태 업데이트
     */
    public static final String UPDATE_CHECK_OUT = 
        "UPDATE attendance " +
        "SET check_out = ?, status = ?, updated_at = SYSDATE " +
        "WHERE user_id = ? AND attendance_date = ?";
    
    /**
     * 사용자별 출석 기록 조회 (기간별)
     */
    public static final String SELECT_ATTENDANCE_BY_PERIOD = 
        "SELECT id, attendance_date, check_in, check_out, status, user_id, created_at, updated_at " +
        "FROM attendance " +
        "WHERE user_id = ? AND attendance_date BETWEEN ? AND ? " +
        "ORDER BY attendance_date DESC";
    
    /**
     * 당일 퇴실하지 않은 출석자 조회 (배치용)
     */
    public static final String SELECT_UNPROCESSED_ATTENDANCE = 
        "SELECT id, attendance_date, check_in, check_out, status, user_id, created_at, updated_at " +
        "FROM attendance " +
        "WHERE attendance_date = ? AND check_out IS NULL AND status != 'EXCUSED'";
    
    /**
     * 자동 퇴실 처리 (배치용)
     */
    public static final String UPDATE_AUTO_ABSENT = 
        "UPDATE attendance " +
        "SET check_out = ?, status = 'ABSENT', updated_at = SYSDATE " +
        "WHERE id = ?";
    
    /**
     * 중복 출석 확인
     */
    public static final String COUNT_TODAY_ATTENDANCE = 
        "SELECT COUNT(*) FROM attendance WHERE user_id = ? AND attendance_date = ?";
    
    // ============ 승인 요청 관련 SQL ============
    
    /**
     * 승인 요청 삽입
     */
    public static final String INSERT_APPROVAL_REQUEST = 
        "INSERT INTO attendance_approval_request " +
        "(id, reason, proof_file, status, requested_at, start_date, end_date, user_id) " +
        "VALUES (seq_approval_request.NEXTVAL, ?, ?, 'progressing', SYSDATE, ?, ?, ?)";
    
    /**
     * 당일 승인된 휴가/병가 확인
     */
    public static final String SELECT_APPROVED_REQUEST_BY_DATE = 
        "SELECT id, reason, proof_file, status, requested_at, decision_at, start_date, end_date, user_id, approver_id " +
        "FROM attendance_approval_request " +
        "WHERE user_id = ? AND ? BETWEEN start_date AND end_date AND status = 'approve'";
    
    /**
     * 승인 요청 상태 업데이트
     */
    public static final String UPDATE_APPROVAL_STATUS = 
        "UPDATE attendance_approval_request " +
        "SET status = ?, decision_at = SYSDATE, approver_id = ? " +
        "WHERE id = ?";
    
    /**
     * 사용자별 승인 요청 목록 조회
     */
//    public static final String SELECT_APPROVAL_REQUESTS_BY_USER = 
//        "SELECT id, reason, proof_file, status, requested_at, decision_at, start_date, end_date, user_id, approver_id " +
//        "FROM attendance_approval_request " +
//        "WHERE user_id = ? " +
//        "ORDER BY requested_at DESC";
    
    /**
     * 대기중인 승인 요청 목록 조회 (관리자용)
     */
//    public static final String SELECT_PENDING_APPROVAL_REQUESTS = 
//        "SELECT aar.id, aar.reason, aar.proof_file, aar.status, aar.requested_at, aar.decision_at, " +
//        "       aar.start_date, aar.end_date, aar.user_id, aar.approver_id, ui.name as user_name " +
//        "FROM attendance_approval_request aar " +
//        "JOIN user_info ui ON aar.user_id = ui.id " +
//        "WHERE aar.status = 'progressing' " +
//        "ORDER BY aar.requested_at ASC";
    
    /**
     * 특정 승인 요청 조회
     */
//    public static final String SELECT_APPROVAL_REQUEST_BY_ID = 
//        "SELECT id, reason, proof_file, status, requested_at, decision_at, start_date, end_date, user_id, approver_id " +
//        "FROM attendance_approval_request " +
//        "WHERE id = ?";
}
