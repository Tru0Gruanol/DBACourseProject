package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Student;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StudentMapper {

    @Select("SELECT student_id AS studentId, student_name AS studentName, registration_time AS registrationTime FROM students")
    List<Student> getAllStudents();

    // 根据ID查询学生（用于检查是否存在）
    @Select("SELECT student_id AS studentId, student_name AS studentName, registration_time AS registrationTime FROM students WHERE student_id = #{studentId}")
    Student selectStudentById(Integer studentId);

    @Insert("INSERT INTO students(student_id, student_name, registration_time) VALUES(#{studentId}, #{studentName}, #{registrationTime})")
    int insertStudent(Student student);

    @Update("UPDATE students SET student_name = #{studentName}, registration_time = #{registrationTime} WHERE student_id = #{studentId}")
    int updateStudent(Student student);

    @Delete("DELETE FROM students WHERE student_id = #{studentId}")
    int deleteStudent(Integer studentId);
}