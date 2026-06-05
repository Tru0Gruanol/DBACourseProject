package com.training.centermanagement.controller;

import com.training.centermanagement.entity.Subject;
import com.training.centermanagement.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{subjectId}")
    public Subject getSubjectById(@PathVariable Integer subjectId) {
        return subjectService.getSubjectById(subjectId);
    }

    @PostMapping
    public String addSubject(@RequestBody Subject subject) {
        return subjectService.addSubject(subject);
    }

    @PutMapping
    public String updateSubject(@RequestBody Subject subject) {
        return subjectService.updateSubject(subject);
    }

    @DeleteMapping("/{subjectId}")
    public String deleteSubject(@PathVariable Integer subjectId) {
        return subjectService.deleteSubject(subjectId);
    }
}