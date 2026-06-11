package com.training.centermanagement.service;

import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class TeacherService {
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ClassesMapper classesMapper;

    public List<Teacher> getAllTeachers() {
        return teacherMapper.getAllTeachers();
    }

    public Teacher getTeacherById(Integer teacherId) {
        return teacherMapper.getTeacherById(teacherId);
    }

    public String addTeacher(Teacher teacher) {
        if (teacherMapper.countByTeacherId(teacher.getTeacherId()) > 0) {
            return "添加失败：教师ID " + teacher.getTeacherId() + " 已存在！";
        }
        return teacherMapper.insertTeacher(teacher) > 0 ? "添加成功" : "添加失败";
    }

    public String updateTeacher(Teacher teacher) {
        if (teacherMapper.getTeacherById(teacher.getTeacherId()) == null) {
            return "更新失败：教师ID " + teacher.getTeacherId() + " 不存在！";
        }
        return teacherMapper.updateTeacher(teacher) > 0 ? "更新成功" : "更新失败";
    }

    public String deleteTeacher(Integer teacherId) {
        if (teacherMapper.countClassesByTeacherId(teacherId) > 0) {
            return "删除失败：该教师名下还有关联班级，请先删除或转移班级！";
        }
        return teacherMapper.deleteTeacher(teacherId) > 0 ? "删除成功" : "删除失败（ID可能不存在）";
    }

    // 根据科目名称（特长关键词）模糊匹配教师
    public List<Teacher> getTeachersBySpecialtyKeyword(String keyword) {
        return teacherMapper.getTeachersBySpecialty(keyword);
    }

    // 获取所有教师的薪酬汇总（每位教师的授课班级 + 总课时报酬）
    public List<Map<String, Object>> getTeacherSalaries() {
        List<Teacher> teachers = teacherMapper.getAllTeachers();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Teacher t : teachers) {
            Map<String, Object> item = new HashMap<>();
            item.put("teacherId", t.getTeacherId());
            item.put("teacherName", t.getTeacherName());
            item.put("teacherLevel", t.getTeacherLevel());
            item.put("specialty", t.getSpecialty());

            List<Classes> classes = classesMapper.getClassesByTeacherId(t.getTeacherId());
            BigDecimal totalRemuneration = BigDecimal.ZERO;
            for (Classes c : classes) {
                totalRemuneration = totalRemuneration.add(c.getTeacherRemuneration());
            }
            item.put("classCount", classes.size());
            item.put("totalRemuneration", totalRemuneration);
            result.add(item);
        }
        return result;
    }
}