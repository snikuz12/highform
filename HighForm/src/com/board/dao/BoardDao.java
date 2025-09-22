package com.board.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.board.model.Board;
import com.board.model.BoardCategory;
import com.board.model.dto.BoardDto;
import com.util.DBConnection;

public class BoardDao {

   // 1. Singleton & DB connection
   private static BoardDao instance;
  
   

   public static BoardDao getInstance() {
      if (instance == null) {
         instance = new BoardDao();
      }
      return instance;
   }
  
   
   private Connection getConnection() throws SQLException {
       return DBConnection.getConnection();
   }
   
   // 테이블 생성
   public void createTable() throws SQLException{
	   String createTableSQL = BoardSQL.CREATE_TABLE;
	   try(Connection conn = getConnection();
			   CallableStatement psmt = conn.prepareCall(createTableSQL)){
		   psmt.executeUpdate();
	   }catch(Exception e) {
		   e.getStackTrace();
	   }
   }
   
   // 게시물 작성
   public Long createBoard(Board board) {
	   
	   String createBoardSQL = BoardSQL.CREATE_BOARD;
	   try(Connection conn = getConnection();
			   PreparedStatement psmt = conn.prepareStatement(createBoardSQL, new String[] { "ID" })){
		   
		   psmt.setString(1, board.getTitle());
		   psmt.setString(2, board.getContent());
		   psmt.setString(3,  board.getType().name());
		   psmt.setLong(4, board.getUserId());
		   psmt.setLong(5, board.getFileId());
		   psmt.setString(6, board.getAuthor());
		   
		   psmt.executeUpdate();
		   
		   // 2. 생성된 ID 가져오기
		    
	        ResultSet rs = psmt.getGeneratedKeys();
	        long generatedId = 0;

	        if (rs.next()) {
	            generatedId = rs.getLong(1);  // 이제 안전하게 동작 가능
//	          또는 rs.getLong("ID");
	        }
		   
		   return generatedId;
	        
	        
	   }catch(Exception e) {
		   e.getStackTrace();
		   e.printStackTrace();
	   }
	   return null;
   }
   
   
   //게시물 호출
   public Board getBoard(Long boardId) {
	   String getBoardSQL = BoardSQL.GET_BOARD;
	   try(Connection conn = getConnection();
			PreparedStatement psmt = conn.prepareStatement(getBoardSQL)){
		   
		   psmt.setLong(1, boardId);
		   
		    ResultSet selectRs = psmt.executeQuery();
		    Board entity = null;
			
		    if (selectRs.next()) {
		        entity = new Board();
		        entity.setBoardId(boardId);
		        entity.setAuthor(selectRs.getString("author"));
		        entity.setTitle(selectRs.getString("title"));
		        entity.setContent(selectRs.getString("content"));
		        entity.setType(selectRs.getString("type").equals(BoardCategory.BOARD.name())  ? BoardCategory.BOARD : 
		        	selectRs.getString("type").equals(BoardCategory.DATA_ROOM.name()) ? BoardCategory.DATA_ROOM :  BoardCategory.NOTICE );
		        entity.setFileId(selectRs.getLong("file_id"));
		        entity.setUserId(selectRs.getLong("user_id"));
		        entity.setCreatedAt(selectRs.getDate("created_at"));
		    }
		   
		   
		   psmt.executeUpdate();
		   
		   return entity;
	   }catch(Exception e) {
		   e.getStackTrace();
		   e.printStackTrace();
	   }
	   return null;
   }
	
   
   
   //게시물 리스트 호출
   public List<BoardDto> getBoardList(BoardCategory type) {
	    String getBoardSQL = BoardSQL.GET_BOARD_FROM_TYPE; // 순번 포함 쿼리
	    List<BoardDto> boardList = new ArrayList<>();
	    
	    try {
			Connection conn2 = getConnection();
			System.out.println(conn2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("오류");
		}
	    
	    
	    
	   try(Connection conn = getConnection();
			PreparedStatement psmt = conn.prepareStatement(getBoardSQL)){
		   
	        // 바인딩: type (enum이거나 String 형태로 처리)
	        psmt.setString(1, type.name());  // 또는 type.toString()
	        
		    ResultSet rs = psmt.executeQuery();
		    
		    while(rs.next()) {
		    	BoardDto dto = new BoardDto(
		    			rs.getInt("no"),
		    			rs.getString("title"),
		    			rs.getString("author"),
		    			rs.getDate("created_at"),
		        		rs.getString("type").equals(BoardCategory.BOARD.name()) ? BoardCategory.BOARD : rs.getString("type").equals(BoardCategory.DATA_ROOM.name()) ? BoardCategory.DATA_ROOM :  BoardCategory.NOTICE,
		    			rs.getString("content"),
		    			rs.getLong("id"),
		    			rs.getLong("user_id")
		    			);

	            boardList.add(dto);
		    }
		
		   
		   psmt.executeUpdate();
		   
		   return boardList;
	   }catch(Exception e) {
		   e.getStackTrace();
		   e.printStackTrace();
	   }
	   return null;
   }
   
   // 게시물 수정 
   public boolean updateBoard(Long boardId, Board board) {
	    String sql = BoardSQL.UPDATE_BOARD;
	        
	        try (Connection conn = getConnection();
	             PreparedStatement psmt = conn.prepareStatement(sql)) {
	            
	            psmt.setString(1, board.getTitle());
	            psmt.setString(2, board.getContent());
	            psmt.setObject(3, board.getFileId());
	            psmt.setLong(4, boardId);
	            
	            // UPDATE 문에서는 getGeneratedKeys() 호출하지 않음
	            int affectedRows = psmt.executeUpdate();
	            return affectedRows > 0;  // 업데이트된 행이 있으면 true 반환
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
   }

   // 게시물 삭제 
   public void deleteBoard(Long boardId) {
	    String sql = BoardSQL.DELETE_BOARD;
	        
	        try (Connection conn = getConnection();
	             PreparedStatement psmt = conn.prepareStatement(sql)) {
	            
	            psmt.setLong(1, boardId);
	           
	 		   psmt.executeUpdate();
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
   }
}
