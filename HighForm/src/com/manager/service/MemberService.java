package com.manager.service;

import java.util.List;

import com.manager.dao.MemberDAO;
import com.manager.model.Member;

public class MemberService {

	private MemberDAO memberDAO;

	public MemberService() {
		this.memberDAO = new MemberDAO();
	}

	// 삭제 되지 않은 회원만 조회
	public List<Member> getAvailableMembers() {
		return memberDAO.getAvailableMembers();
	}

	// 모든 회원 조회
	public List<Member> getAllMembers() {
		return memberDAO.getAllMembers();
	}

	// role 컬럼 제약조건에 맞는 형변환
	public int registerMember(Member member) {
		String changedPosition = changePositionText(member);
		member.setPosition(changedPosition);
		return memberDAO.addMemberAndReturnId(member);
	}

	private String changePositionText(Member member) {
		String getPos = member.getPosition();
		return switch (getPos) {
		case "학생" -> "STUDENT";
		case "강사" -> "PROFESSOR";
		case "관리자" -> "MANAGER";
		default -> throw new IllegalArgumentException("Unexpected value: " + getPos);
		};
	}

	// 회원 등록
	public int addMember(Member member) {
		if (member == null || member.getMemberLoginId() == null || member.getMemberName() == null) {
			System.err.println("회원 정보가 누락되었습니다.");
			return -1; // 실패 시 -1 반환
		}
		return memberDAO.addMemberAndReturnId(member);
	}

	// 회원 수정
	public boolean updateMember(Member member) {
		if (member == null || member.getMemberId() == 0) {
			System.err.println("수정할 회원 정보가 없습니다.");
			return false;
		}
		return memberDAO.updateMember(member);
	}

	// 회원 삭제
	public boolean deleteMember(int memberId) {
		if (memberId <= 0) {
			System.err.println("삭제할 회원 ID가 유효하지 않습니다.");
			return false;
		}
		return memberDAO.deleteMember(memberId);
	}
}