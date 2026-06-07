package com.training.centermanagement.service;

import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeacherService {
    @Autowired
    private TeacherMapper teacherMapper;

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
}