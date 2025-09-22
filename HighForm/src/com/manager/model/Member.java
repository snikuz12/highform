package com.manager.model;

import lombok.Getter;

public class Member {
	private int memberId;
	private String memberLoginId;
	private String password;
	private String memberName;
	private String email;
	private String phoneNumber;
	private String affiliation; // 소속
	private String position; // 직급
	//private String createdAt;
	//private String state;

	// 기본 생성자
	public Member() {
	}

	// 매개변수 생성자
	public Member(int memberId, String memberLoginId, String password, String memberName, String email,
			String phoneNumber, String affiliation, String position){
		this.memberId = memberId;
		this.memberLoginId = memberLoginId;
		this.password = password;
		this.memberName = memberName;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.affiliation = affiliation;
		this.position = position;
	}

	public Member(int memberId, String memberName, String email, String phoneNumber) {
		this.memberId = memberId;
		this.memberName = memberName;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	// Getters and Setters
	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getMemberLoginId() {
		return memberLoginId;
	}

	public void setMemberLoginId(String memberLoginId) {
		this.memberLoginId = memberLoginId;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
}