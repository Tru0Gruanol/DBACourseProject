package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TeacherMapper {
    @Select("SELECT teacher_id AS teacherId, teacher_name AS teacherName, teacher_level AS teacherLevel, specialty FROM teachers")
    List<Teacher> getAllTeachers();
}