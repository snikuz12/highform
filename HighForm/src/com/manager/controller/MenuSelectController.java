package com.manager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuSelectController {
    
    @FXML
    private Button courseManagementButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button paymentApprovalButton;
    
    @FXML
    private Button attendanceListButton;
    
    @FXML
    private Button memberManagementButton;
    
    @FXML
    private void initialize() {
        // 초기화 로직이 필요한 경우 여기에 작성
        System.out.println("MenuSelectController 초기화 완료");
    }
    
    // 강의 관리
    @FXML
    private void handleCourseManagement(ActionEvent event) {
    	System.out.println("강의관리 클릭");
    	
    	try {
            // courseManagement.fxml 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/manager/courseManagement.fxml"));
            Parent courseManagementRoot = loader.load();
            
            // 새로운 Scene 생성
            Scene courseManagementScene = new Scene(courseManagementRoot);
            
            // 현재 Stage 가져오기
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Scene 전환
            window.setScene(courseManagementScene);
            window.show();
            
            System.out.println("강의 관리 화면으로 전환 완료");
            
        } catch (Exception e) {
            System.err.println("화면 전환 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 회원관리
    @FXML
    private void handleMemberManagement(ActionEvent event) {
        System.out.println("회원관리 클릭");
        
        try {
            // memberManagement.fxml 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/manager/memberManagement.fxml"));
            Parent memberManagementRoot = loader.load();
            
            // 새로운 Scene 생성
            Scene memberManagementScene = new Scene(memberManagementRoot);
            
            // 현재 Stage 가져오기
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Scene 전환
            window.setScene(memberManagementScene);
            window.show();
            
            System.out.println("회원 관리 화면으로 전환 완료");
            
        } catch (Exception e) {
            System.err.println("화면 전환 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSettings(ActionEvent event) {
        System.out.println("설정 버튼 클릭됨");
        // 설정 화면으로 전환하는 로직을 추후 구현
    }
    
    @FXML
    private void handlePaymentApproval(ActionEvent event) {
        System.out.println("결재승인 버튼 클릭됨");
        // 결재승인 화면으로 전환하는 로직을 추후 구현
    }
    
    @FXML
    private void handleAttendanceList(ActionEvent event) {
        System.out.println("출결리스트 버튼 클릭됨");
        // 출결리스트 화면으로 전환하는 로직을 추후 구현
    }    
    
}