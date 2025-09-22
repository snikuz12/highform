package com.manager.service;

import com.manager.dao.EnrollmentDAO;

public class EnrollmentService {

    private EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    public boolean enrollMemberToCourse(int memberId, int courseId) {
        return enrollmentDAO.insertEnrollment(memberId, courseId);
    }
}