package com.manager.model;

public class Course {
    private int courseId;
    private String courseName;
    private String startDate;
    private String endDate;
    private String instructor;
    private String manager;
    private String note;
    
    // 기본 생성자
    public Course() {}
    
    // 매개변수 생성자
    public Course(int courseId, String courseName, String startDate, String endDate, String instructor, String manager, String note) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.instructor = instructor;
        this.manager = manager;
        this.note = note;
    }
    
    // Getters and Setters
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public String getInstructor() { return instructor; }
    public void setInstructor(String endDate) { this.instructor = endDate; }
    
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}