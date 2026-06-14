package com.training.centermanagement.service;

import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.Notification;
import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import com.training.centermanagement.mapper.TeacherSubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class TeacherService {
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ClassesMapper classesMapper;

    @Autowired
    private TeacherSubjectMapper teacherSubjectMapper;
    @Autowired
    private NotificationService notificationService;

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = teacherMapper.getAllTeachers();
        // 为每个教师填充其任教科目ID列表
        for (Teacher t : teachers) {
            t.setSubjectIds(teacherSubjectMapper.getSubjectIdsByTeacherId(t.getTeacherId()));
        }
        return teachers;
    }

    public Teacher getTeacherById(Integer teacherId) {
        Teacher teacher = teacherMapper.getTeacherById(teacherId);
        if (teacher != null) {
            teacher.setSubjectIds(teacherSubjectMapper.getSubjectIdsByTeacherId(teacherId));
        }
        return teacher;
    }

    @Transactional(rollbackFor = Exception.class)
    public String addTeacher(Teacher teacher) {
        if (teacherMapper.countByTeacherId(teacher.getTeacherId()) > 0) {
            return "添加失败：教师ID " + teacher.getTeacherId() + " 已存在！";
        }
        int rows = teacherMapper.insertTeacher(teacher);
        if (rows > 0) {
            // 维护教师-科目关联
            saveTeacherSubjects(teacher.getTeacherId(), teacher.getSubjectIds());
            // 通知教师
            notificationService.send(teacher.getTeacherId(), "teacher",
                "👨‍🏫 账号已创建",
                teacher.getTeacherName() + " 老师，您的教师账号已创建，工号 " + teacher.getTeacherId() + "，初始密码 111111，请及时登录修改密码");
            return "添加成功";
        }
        return "添加失败";
    }

    @Transactional(rollbackFor = Exception.class)
    public String updateTeacher(Teacher teacher) {
        if (teacherMapper.getTeacherById(teacher.getTeacherId()) == null) {
            return "更新失败：教师ID " + teacher.getTeacherId() + " 不存在！";
        }
        int rows = teacherMapper.updateTeacher(teacher);
        if (rows > 0) {
            // 先清除旧关联，再写入新关联
            teacherSubjectMapper.deleteByTeacherId(teacher.getTeacherId());
            saveTeacherSubjects(teacher.getTeacherId(), teacher.getSubjectIds());
            return "更新成功";
        }
        return "更新失败";
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteTeacher(Integer teacherId) {
        if (teacherMapper.countClassesByTeacherId(teacherId) > 0) {
            return "删除失败：该教师名下还有关联班级，请先删除或转移班级！";
        }
        // 删除教师前先清除其科目关联
        teacherSubjectMapper.deleteByTeacherId(teacherId);
        return teacherMapper.deleteTeacher(teacherId) > 0 ? "删除成功" : "删除失败（ID可能不存在）";
    }

    // 保存教师的任教科目关联
    private void saveTeacherSubjects(Integer teacherId, List<Integer> subjectIds) {
        if (subjectIds != null && !subjectIds.isEmpty()) {
            for (Integer subjectId : subjectIds) {
                teacherSubjectMapper.insertTeacherSubject(teacherId, subjectId);
            }
        }
    }

    // 根据科目ID精确查询能教该科目的所有教师
    public List<Teacher> getTeachersBySubjectId(Integer subjectId) {
        List<Teacher> teachers = teacherSubjectMapper.getTeachersBySubjectId(subjectId);
        // 为每个教师填充其任教科目ID列表
        for (Teacher t : teachers) {
            t.setSubjectIds(teacherSubjectMapper.getSubjectIdsByTeacherId(t.getTeacherId()));
        }
        return teachers;
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