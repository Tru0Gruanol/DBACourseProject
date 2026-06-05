package com.training.centermanagement.controller;

import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.service.ClassesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin
public class ClassesController {

    @Autowired
    private ClassesService classesService;

    @GetMapping
    public List<Classes> getAllClasses() {
        return classesService.getAllClasses();
    }

    @GetMapping("/{classCode}")
    public Classes getClassByCode(@PathVariable String classCode) {
        return classesService.getClassByCode(classCode);
    }

    @PostMapping
    public String addClass(@RequestBody Classes classes) {
        return classesService.addClass(classes);
    }

    @GetMapping("/by-subject")
    public List<Classes> getClassesBySubjectId(@RequestParam Integer subjectId) {
        return classesService.getClassesBySubjectId(subjectId);
    }
}