package com.training.centermanagement.service;

import com.training.centermanagement.entity.Student;
import com.training.centermanagement.mapper.StudentMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private AccountMapper accountMapper;

    public String addStudent(Student student) {
        // 检查学生ID是否已存在
        Student existing = studentMapper.selectStudentById(student.getStudentId());
        if (existing != null) {
            return "学生建档失败：学生ID " + student.getStudentId() + " 已存在！";
        }
        student.setRegistrationTime(new Date());
        studentMapper.insertStudent(student);
        return "学生建档成功！";
    }

    public List<Student> getAllStudents() {
        return studentMapper.getAllStudents();
    }

    public String updateStudent(Student student) {
        Student existing = studentMapper.selectStudentById(student.getStudentId());
        if (existing == null) {
            return "更新失败：学生ID " + student.getStudentId() + " 不存在！";
        }
        return studentMapper.updateStudent(student) > 0 ? "更新成功" : "更新失败";
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteStudent(Integer studentId) {
        Student existing = studentMapper.selectStudentById(studentId);
        if (existing == null) {
            return "删除失败：学生ID " + studentId + " 不存在！";
        }
        // 检查是否有在读报名
        if (studentEnrollmentMapper.countByStudentId(studentId) > 0) {
            return "删除失败：该学生还有未退课的报名记录，请先退课后再删除！";
        }
        // 所有报名均已取消：清理流水 → 清理报名 → 删除学生
        accountMapper.deleteByStudentId(studentId);
        studentEnrollmentMapper.deleteByStudentId(studentId);
        return studentMapper.deleteStudent(studentId) > 0 ? "删除成功" : "删除失败";
    }
}