package com.training.centermanagement.service;

import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduleService {

    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private ClassesMapper classesMapper;

    public List<Map<String, Object>> getStudentSchedule(Integer studentId) {
        List<String> classCodes = studentEnrollmentMapper.getClassCodesByStudentId(studentId);
        List<Map<String, Object>> schedule = new ArrayList<>();
        for (String code : classCodes) {
            Classes cls = classesMapper.getClassByCode(code);
            if (cls != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("classCode", cls.getClassCode());
                item.put("subjectId", cls.getSubjectId());
                item.put("teacherId", cls.getTeacherId());
                item.put("term", cls.getTerm());
                item.put("period", cls.getPeriod());
                item.put("location", cls.getLocation());
                schedule.add(item);
            }
        }
        return schedule;
    }

    public List<Map<String, Object>> getTeacherSchedule(Integer teacherId) {
        List<Classes> classes = classesMapper.getClassesByTeacherId(teacherId);
        List<Map<String, Object>> schedule = new ArrayList<>();
        for (Classes cls : classes) {
            Map<String, Object> item = new HashMap<>();
            item.put("classCode", cls.getClassCode());
            item.put("subjectId", cls.getSubjectId());
            item.put("term", cls.getTerm());
            item.put("period", cls.getPeriod());
            item.put("location", cls.getLocation());
            item.put("teacherRemuneration", cls.getTeacherRemuneration());
            item.put("fee", cls.getFee());
            item.put("enrolledCount", cls.getEnrolledCount());
            item.put("capacity", cls.getCapacity());
            schedule.add(item);
        }
        return schedule;
    }
}