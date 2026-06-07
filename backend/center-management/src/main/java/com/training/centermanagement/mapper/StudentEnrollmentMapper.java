package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.StudentEnrollment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentEnrollmentMapper {

    @Insert("INSERT INTO student_enrollments(student_id, class_code, enrollment_time, amount_paid) " +
            "VALUES(#{studentId}, #{classCode}, #{enrollmentTime}, #{amountPaid})")
    int insertEnrollment(StudentEnrollment enrollment);

    @Select("SELECT * FROM student_enrollments WHERE student_id = #{studentId} AND class_code = #{classCode}")
    StudentEnrollment selectByStudentAndClass(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 新增：获取学生选课的所有班级代码（用于课表）
    @Select("SELECT class_code FROM student_enrollments WHERE student_id = #{studentId}")
    List<String> getClassCodesByStudentId(Integer studentId);

    @Delete("DELETE FROM student_enrollments WHERE student_id = #{studentId} AND class_code = #{classCode}")
    int deleteEnrollment(@Param("studentId") Integer studentId, @Param("classCode") String classCode);
}