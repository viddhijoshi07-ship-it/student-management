package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.dto.StudentResponse;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.DuplicateResourceException;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        String email = request.email().trim().toLowerCase();
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A student with email '" + email + "' already exists");
        }
        Student student = new Student(request.name().trim(), email, request.branch().trim(), request.year());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream().map(StudentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return StudentResponse.from(findOrThrow(id));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findOrThrow(id);
        String email = request.email().trim().toLowerCase();

        if (studentRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("Another student with email '" + email + "' already exists");
        }

        student.setName(request.name().trim());
        student.setEmail(email);
        student.setBranch(request.branch().trim());
        student.setYear(request.year());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        Student student = findOrThrow(id);
        studentRepository.delete(student);
    }

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }
}
