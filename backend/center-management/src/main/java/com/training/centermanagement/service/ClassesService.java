package com.training.centermanagement.service;

import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import com.training.centermanagement.mapper.SubjectMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassesService {

    @Autowired
    private ClassesMapper classesMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private AccountMapper accountMapper;

    public List<Classes> getAllClasses() {
        return classesMapper.getAllClasses();
    }

    public Classes getClassByCode(String classCode) {
        return classesMapper.getClassByCode(classCode);
    }

    public String addClass(Classes classes) {
        if (classesMapper.countByClassCode(classes.getClassCode()) > 0) {
            return "排课失败：班级代号 " + classes.getClassCode() + " 已存在！";
        }
        if (teacherMapper.countByTeacherId(classes.getTeacherId()) == 0) {
            return "排课失败：教师ID " + classes.getTeacherId() + " 不存在！";
        }
        if (subjectMapper.countBySubjectId(classes.getSubjectId()) == 0) {
            return "排课失败：科目ID " + classes.getSubjectId() + " 不存在！";
        }
        if (classes.getEnrolledCount() == null) {
            classes.setEnrolledCount(0);
        }
        int rows = classesMapper.insertClass(classes);
        return rows > 0 ? "排课成功" : "排课失败";
    }

    public String updateClass(Classes classes) {
        Classes existing = classesMapper.getClassByCode(classes.getClassCode());
        if (existing == null) {
            return "更新失败：班级代号 " + classes.getClassCode() + " 不存在！";
        }
        if (teacherMapper.countByTeacherId(classes.getTeacherId()) == 0) {
            return "更新失败：教师ID " + classes.getTeacherId() + " 不存在！";
        }
        if (subjectMapper.countBySubjectId(classes.getSubjectId()) == 0) {
            return "更新失败：科目ID " + classes.getSubjectId() + " 不存在！";
        }
        // enrolledCount 只能由报名/退课操作修改，不允许通过编辑班级直接覆盖
        classes.setEnrolledCount(existing.getEnrolledCount());
        return classesMapper.updateClass(classes) > 0 ? "更新成功" : "更新失败";
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteClass(String classCode) {
        if (classesMapper.getClassByCode(classCode) == null) {
            return "删除失败：班级代号 " + classCode + " 不存在！";
        }
        // 检查是否有在读学生
        if (studentEnrollmentMapper.countByClassCode(classCode) > 0) {
            return "删除失败：该班级下还有在读学生，请先处理退课！";
        }
        // 所有报名均已取消：清理流水 → 清理报名 → 删除班级
        accountMapper.deleteByClassCode(classCode);
        studentEnrollmentMapper.deleteByClassCode(classCode);
        return classesMapper.deleteClass(classCode) > 0 ? "删除成功" : "删除失败";
    }

    // 新增：根据科目ID查询班级
    public List<Classes> getClassesBySubjectId(Integer subjectId) {
        return classesMapper.getClassesBySubjectId(subjectId);
    }
}