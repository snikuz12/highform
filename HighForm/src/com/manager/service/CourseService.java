package com.manager.service;

import com.manager.dao.CourseDAO;
import com.manager.model.Course;
import java.util.List;

public class CourseService {
    private CourseDAO courseDAO;
    
    public CourseService() {
        this.courseDAO = new CourseDAO();
    }
    
    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }
    
    public boolean addCourse(Course course) {
        // 비즈니스 로직 검증
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("강의명은 필수입니다.");
        }
        if (course.getInstructor() == null || course.getInstructor().trim().isEmpty()) {
            throw new IllegalArgumentException("강사는 필수입니다.");
        }
        
        return courseDAO.addCourse(course);
    }
    
    public boolean updateCourse(Course course) {
        return courseDAO.updateCourse(course);
    }
    
    public boolean deleteCourse(int courseId) {
        return courseDAO.deleteCourse(courseId);
    }
}