//package com.board.util;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.time.Duration;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.util.DBConnection;
//
//public class GeminiApiClient {
//    private static final String API_KEY; // 실제 API 키로 교체
//    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
//    private static final HttpClient httpClient = HttpClient.newBuilder()
//            .connectTimeout(Duration.ofSeconds(10))
//            .build();
//    private static final Gson gson = new Gson();
//    
//    
//    
//    private String getKey() throws SQLException {
//        return DBConnection.getKey();
//    }
//    
//    
//    public static String generateComment(String postContent, String postTitle) {
//        try {
//        	API_KEY = getKey();
//        	
//            // 프롬프트 구성
//            String prompt = String.format(
//                "다음 게시글에 대한 유용하고 도움이 되는 댓글을 작성해주세요. " +
//                "댓글은 50자 이상 200자 이하로 작성하고, 게시글 내용을 잘 이해한 후 " +
//                "구체적이고 실용적인 조언이나 견해를 담아주세요.\n\n" +
//                "제목: %s\n" +
//                "내용: %s",
//                postTitle, postContent
//            );
//            
//            // 요청 본문 구성
//            JsonObject requestBody = new JsonObject();
//            JsonObject content = new JsonObject();
//            JsonObject part = new JsonObject();
//            
//            part.addProperty("text", prompt);
//            content.add("parts", gson.toJsonTree(new JsonObject[]{part}));
//            requestBody.add("contents", gson.toJsonTree(new JsonObject[]{content}));
//            
//            // HTTP 요청 생성
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(API_URL + "?key=" + API_KEY))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
//                    .build();
//            
//            // API 호출
//            HttpResponse<String> response = httpClient.send(request, 
//                    HttpResponse.BodyHandlers.ofString());
//            
//            // 응답 파싱
//            if (response.statusCode() == 200) {
//                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
//                JsonObject candidates = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
//                JsonObject contentResponse = candidates.getAsJsonObject("content");
//                JsonObject partResponse = contentResponse.getAsJsonArray("parts").get(0).getAsJsonObject();
//                
//                return partResponse.get("text").getAsString().trim();
//            } else {
//                throw new RuntimeException("API 호출 실패: " + response.statusCode());
//            }
//            
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return "AI 댓글 생성 중 오류가 발생했습니다.";
//        }
//    }
//}