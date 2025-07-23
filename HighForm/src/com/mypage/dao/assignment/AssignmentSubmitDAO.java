package com.mypage.dao.assignment;

import com.mypage.Model.assignment.AssignmentSubmit;
import com.util.DBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AssignmentSubmitDAO
 *  - 과제 제출/조회 DAO
 *  - 싱글턴 패턴
 */
public class AssignmentSubmitDAO {

    /* ==========================================================
     * 1) 과제 콤보 옵션용 DTO
     * ======================================================== */
    public static class AssignmentOption {
        private Long id;
        private String title;
        private String curriculumName;

        public AssignmentOption(Long id, String title, String curriculumName) {
            this.id = id;
            this.title = title;
            this.curriculumName = curriculumName;
        }
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getCurriculumName() { return curriculumName; }

        @Override
        public String toString() {
            return title + (curriculumName != null ? " (" + curriculumName + ")" : "");
        }
    }


    /* ==========================================================
     * 3) 싱글턴 설정 & 커넥션 헬퍼
     * ======================================================== */
    private static AssignmentSubmitDAO instance;
    private AssignmentSubmitDAO() {}
    public static AssignmentSubmitDAO getInstance() {
        if (instance == null) instance = new AssignmentSubmitDAO();
        return instance;
    }
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /* ==========================================================
     * 4) 내가 제출한 과제 목록 
     * ======================================================== */
    public List<AssignmentSubmit> getSubmitList(Long userId, int offset, int limit) throws SQLException {
    	String sql =
    		    "SELECT * FROM ( " +
    		    "  SELECT s.*,                      " +
    		    "         a.title AS assignment_title, " +  // ★ 추가
    		    "         c.name  AS curriculum_name,  " +
    		    "         ROW_NUMBER() OVER (ORDER BY s.submitted_at DESC) rn " +
    		    "    FROM assignment_submit s " +
    		    "    JOIN assignment a ON s.assignment_id = a.id " +
    		    "    JOIN curriculum c ON a.curriculum_id = c.id " +
    		    "   WHERE s.user_id = ? " +
    		    ") WHERE rn > ? AND rn <= ?";

        List<AssignmentSubmit> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, offset);
            ps.setInt(3, offset + limit - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                	AssignmentSubmit sub = new AssignmentSubmit();
                	sub.setUserId(rs.getLong("user_id"));
                	sub.setAssignmentId(rs.getLong("assignment_id"));

                	sub.setSubmitTitle(rs.getString("title"));          // 제출 제목
                	sub.setContent(rs.getString("content"));
                	Timestamp ts = rs.getTimestamp("submitted_at");
                	if (ts != null) sub.setSubmittedAt(ts.toLocalDateTime());

                	sub.setAssignmentTitle(rs.getString("assignment_title")); 
                	sub.setCurriculumName(rs.getString("curriculum_name"));

                    list.add(sub);
                }
            }
        }
        return list;
    }

    /* ==========================================================
     * 5) 과제 제출 INSERT
     * ======================================================== */
    public void insert(AssignmentSubmit submit) throws SQLException {
        String sql =
            "INSERT INTO assignment_submit " +
            "      (user_id, assignment_id, title, content, submitted_at) " +
            "VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, submit.getUserId());
            ps.setLong(2, submit.getAssignmentId());
            ps.setString(3, submit.getSubmitTitle());
            ps.setString(4, submit.getContent());
            ps.setTimestamp(5,
                    submit.getSubmittedAt() != null ? Timestamp.valueOf(submit.getSubmittedAt()) : null);
            ps.executeUpdate();
        }
    }

    /* ==========================================================
     * 6) 내가 제출한 과제 전체 개수
     * ======================================================== */
    public int getSubmitCount(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assignment_submit WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ==========================================================
     * 7) (제출 폼) 수강 중 과제 콤보
     * ======================================================== */
    public List<AssignmentOption> getAvailableAssignmentsForUser(Long userId) throws SQLException {
        String sql =
            "SELECT a.id, a.title, c.name AS curriculum_name " +
            "  FROM assignment a " +
            "  JOIN curriculum c ON a.curriculum_id = c.id " +
            "  JOIN enrollment e ON c.id = e.curriculum_id " +
            " WHERE e.user_id = ?";
        List<AssignmentOption> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AssignmentOption(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("curriculum_name")));
                }
            }
        }
        return list;
    }

    /* ==========================================================
     * 8) 수강 중 모든 과제 + 제출 여부(LEFT JOIN)
     * ======================================================== */
    public List<CourseAssignmentDTO> getCourseAssignmentsWithStatus(
            Long userId, int offset, int limit) throws SQLException {

    	String sql =
    		    "SELECT * FROM ( " +
    		    "  SELECT a.id   AS assignment_id, " +
    		    "         a.title AS assignment_title, " +
    		    "         a.end_date AS end_date, " +
    		    /* ---------- 존재 여부만 체크 (EXISTS) ---------- */
    		    "         CASE WHEN EXISTS (SELECT 1 " +
    		    "                           FROM assignment_submit s " +
    		    "                          WHERE s.assignment_id = a.id " +
    		    "                            AND s.user_id      = ?) " +
    		    "              THEN 1 ELSE 0 END AS submitted, " +
    		    "         ROW_NUMBER() OVER (ORDER BY a.end_date) rn " +
    		    "    FROM assignment a " +
    		    "    JOIN enrollment e " +
    		    "      ON a.curriculum_id = e.curriculum_id " +
    		    "     AND e.user_id       = ? " + 
    		    ") WHERE rn > ? AND rn <= ?";


        List<CourseAssignmentDTO> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);            // enrollment 조건
            ps.setLong(2, userId);            // 제출 여부 조건
            ps.setInt (3, offset);            // rn >  offset
            ps.setInt (4, offset + limit - 1);// rn <= offset+limit-1

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseAssignmentDTO dto = new CourseAssignmentDTO();
                    dto.setAssignmentId   (rs.getLong ("assignment_id"));
                    dto.setAssignmentTitle(rs.getString("assignment_title"));
                    /* curriculum_name 컬럼을 선택하지 않았으므로 주석 또는 제거
                       dto.setCurriculumName(rs.getString("curriculum_name"));
                    */

                    /* -------- 변경: end_date → LocalDateTime 매핑 -------- */
                    Timestamp ts = rs.getTimestamp("end_date");
                    dto.setEndDate(ts != null ? ts.toLocalDateTime() : null);

                    dto.setSubmitted(rs.getInt("submitted") == 1);
                    list.add(dto);
                }
            }
        }
        return list;
    }


    /* ==========================================================
     * 9) 모든 과제 개수 (수강 과정 범위)
     * ======================================================== */
    public int getCourseAssignmentCount(Long userId) throws SQLException {
        String sql =
            "SELECT COUNT(*) " +
            "  FROM assignment a " +
            "  JOIN curriculum c ON a.curriculum_id = c.id " +
            "  JOIN enrollment e ON c.id = e.curriculum_id " +
            " WHERE e.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    
    public String getIdTitle(Long assignmentId) {
        final String sql = "SELECT title FROM assignment WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, assignmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("title");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();          
        }
        return null;                       
    }

    /* ====================================================================
     *  과제 제출 + 첨부파일 INSERT 
     * ==================================================================== */

    public void insertWithFile(AssignmentSubmit submit, File file) throws Exception {

        try (Connection conn = getConnection()) {

            conn.setAutoCommit(false);

            /* ── 1) 시퀀스에서 새 PK(id) 확보 ───────────────────────────── */
            long submitId;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT assignment_submit_seq.NEXTVAL FROM dual")) {
                rs.next();
                submitId = rs.getLong(1);
            }

            /* ── 2) assignment_submit INSERT  ▶ 컬럼명 id 로 통일 ───────── */
            final String sqlSubmit =
                "INSERT INTO assignment_submit " +
                "(id, user_id, assignment_id, title, content, submitted_at) " +
                "VALUES (?, ?, ?, ?, ?, SYSDATE)";

            try (PreparedStatement ps = conn.prepareStatement(sqlSubmit)) {
                ps.setLong  (1, submitId);
                ps.setLong  (2, submit.getUserId());
                ps.setLong  (3, submit.getAssignmentId());
                ps.setString(4, submit.getSubmitTitle());
                ps.setString(5, submit.getContent());
                ps.executeUpdate();
            }

            /* ── 3) 첨부파일이 있을 때 file_location INSERT ───────────── */
            if (file != null) {

                Path saved = saveFileToServer(file);   // 서버 디렉터리로 복사

                final String sqlFile =
                    "INSERT INTO file_location " +
                    "(id, file_path, file_type, file_size, uploaded_at, submit_id, user_id) " +
                    "VALUES (file_location_seq.NEXTVAL, ?, ?, ?, SYSDATE, ?, ?)";

                try (PreparedStatement psFile = conn.prepareStatement(sqlFile)) {
                    psFile.setString(1, saved.toString());
                    psFile.setString(2, Files.probeContentType(saved));
                    psFile.setLong  (3, Files.size(saved));
                    psFile.setLong  (4, submitId);           // FK → assignment_submit.id
                    psFile.setLong  (5, submit.getUserId());
                    psFile.executeUpdate();
                }
            }

            conn.commit();

        } catch (Exception ex) {
            /* 여기서 ROLLBACK 은 try-with-resources 밖에서 다시 연결을 열 필요 없음 */
            throw ex;  // 컨트롤러에서 catch 후 Alert 으로 표시
        }
    }


    /* 1) 업로드 루트 디렉터리 지정 → 홈 디렉터리 아래로 */
    private static final Path UPLOAD_ROOT =
            Paths.get(System.getProperty("user.home"), "uploads");   // ~/uploads

    /* 2) 파일 저장 메서드 수정 */
    private Path saveFileToServer(File src) throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path destDir   = UPLOAD_ROOT.resolve(dateDir);
        Files.createDirectories(destDir);           // 권한 문제 없음

        Path dest = destDir.resolve(UUID.randomUUID() + "_" + src.getName());
        Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest;
    }


    
}
