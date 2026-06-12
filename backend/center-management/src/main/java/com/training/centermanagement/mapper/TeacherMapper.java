package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Teacher;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TeacherMapper {

    @Select("SELECT teacher_id AS teacherId, teacher_name AS teacherName, teacher_level AS teacherLevel, specialty FROM teachers")
    List<Teacher> getAllTeachers();

    @Select("SELECT teacher_id AS teacherId, teacher_name AS teacherName, teacher_level AS teacherLevel, specialty FROM teachers WHERE teacher_id = #{teacherId}")
    Teacher getTeacherById(Integer teacherId);

    @Select("SELECT COUNT(*) FROM teachers WHERE teacher_id = #{teacherId}")
    int countByTeacherId(Integer teacherId);

    @Insert("INSERT INTO teachers(teacher_id, teacher_name, teacher_level, specialty) VALUES(#{teacherId}, #{teacherName}, #{teacherLevel}, #{specialty})")
    int insertTeacher(Teacher teacher);

    @Update("UPDATE teachers SET teacher_name = #{teacherName}, teacher_level = #{teacherLevel}, specialty = #{specialty} WHERE teacher_id = #{teacherId}")
    int updateTeacher(Teacher teacher);

    @Delete("DELETE FROM teachers WHERE teacher_id = #{teacherId}")
    int deleteTeacher(Integer teacherId);

    @Select("SELECT COUNT(*) FROM classes WHERE teacher_id = #{teacherId}")
    int countClassesByTeacherId(Integer teacherId);

    // 登录验证：按教师号+密码查询
    @Select("SELECT teacher_id AS teacherId, teacher_name AS teacherName, teacher_level AS teacherLevel, specialty, password FROM teachers WHERE teacher_id = #{teacherId} AND password = #{password}")
    Teacher login(Integer teacherId, String password);

    // 修改密码
    @Update("UPDATE teachers SET password = #{newPassword} WHERE teacher_id = #{teacherId} AND password = #{oldPassword}")
    int changePassword(Integer teacherId, String oldPassword, String newPassword);
}