package com.board.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.board.model.Comment;
import com.util.DBConnection;

public class CommentDao {
    private static CommentDao instance;
    
    public static CommentDao getInstance() {
        if (instance == null) {
            instance = new CommentDao();
        }
        return instance;
    }
    
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
    
    // 댓글 작성
    public Long createComment(Comment comment) {
        String sql = CommentSQL.CREATE_COMMENT;
        try (Connection conn = getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql, new String[] { "ID" })) {
            
            psmt.setLong(1, comment.getBoardId());
            psmt.setLong(2, comment.getParentId());
            psmt.setString(3, comment.getAuthor());
            psmt.setString(4, comment.getContent());
            psmt.setLong(5, comment.getUserId());
            
            psmt.executeUpdate();
            
            ResultSet rs = psmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 게시글의 댓글 목록 조회
    public List<Comment> getCommentsByBoardId(Long boardId, String currentUser) {
        String sql = CommentSQL.GET_COMMENTS_BY_BOARD_ID;
        List<Comment> comments = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            
            psmt.setLong(1, boardId);
            
            ResultSet rs = psmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getLong("id"));
                comment.setBoardId(rs.getLong("board_id"));
                comment.setParentId(rs.getLong("parent_id"));
                comment.setAuthor(rs.getString("author"));
                comment.setContent(rs.getString("content"));
                comment.setUserId(rs.getLong("user_id"));
                comment.setCreatedAt(rs.getDate("created_at"));
                comment.setUpdatedAt(rs.getDate("updated_at"));
                comment.setDel_yn(rs.getString("del_yn").charAt(0));
                
                // 현재 사용자가 작성자인지 확인
                comment.setOwner(comment.getAuthor().equals(currentUser));
                
                comments.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comments;
    }
    
    // 댓글 수정
    public boolean updateComment(Long commentId, String content) {
        String sql = CommentSQL.UPDATE_COMMENT;
        try (Connection conn = getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            
            psmt.setString(1, content);
            psmt.setLong(2, commentId);
            
            return psmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 댓글 삭제 (논리 삭제)
    public boolean deleteComment(Long commentId) {
        String sql = CommentSQL.DELETE_COMMENT;
        try (Connection conn = getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            
            psmt.setLong(1, commentId);
            
            return psmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 대댓글도 함께 삭제
    public boolean deleteCommentWithReplies(Long commentId) {
        String sql = CommentSQL.DELETE_COMMENT_WITH_REPLIES;
        try (Connection conn = getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            
            psmt.setLong(1, commentId);
            psmt.setLong(2, commentId);
            
            return psmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}