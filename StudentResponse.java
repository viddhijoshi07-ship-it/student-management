package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.Student;

public record StudentResponse(Long id, String name, String email, String branch, Integer year) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getBranch(), s.getYear());
    }
}
