package com.training.centermanagement.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeacherSubjectMapper {

    // 查询某教师的所有任教科目ID
    @Select("SELECT subject_id FROM teacher_subjects WHERE teacher_id = #{teacherId}")
    List<Integer> getSubjectIdsByTeacherId(Integer teacherId);

    // 查询某科目的所有任课教师
    @Select("SELECT t.teacher_id AS teacherId, t.teacher_name AS teacherName, t.teacher_level AS teacherLevel, t.specialty " +
            "FROM teachers t INNER JOIN teacher_subjects ts ON t.teacher_id = ts.teacher_id " +
            "WHERE ts.subject_id = #{subjectId}")
    List<com.training.centermanagement.entity.Teacher> getTeachersBySubjectId(Integer subjectId);

    // 插入一条教师-科目关联
    @Insert("INSERT INTO teacher_subjects(teacher_id, subject_id) VALUES(#{teacherId}, #{subjectId})")
    int insertTeacherSubject(@Param("teacherId") Integer teacherId, @Param("subjectId") Integer subjectId);

    // 删除某教师的所有科目关联（用于更新前清理）
    @Delete("DELETE FROM teacher_subjects WHERE teacher_id = #{teacherId}")
    int deleteByTeacherId(Integer teacherId);

    // 删除某科目的所有教师关联
    @Delete("DELETE FROM teacher_subjects WHERE subject_id = #{subjectId}")
    int deleteBySubjectId(Integer subjectId);

    // 检查是否存在某教师与某科目的关联
    @Select("SELECT COUNT(*) FROM teacher_subjects WHERE teacher_id = #{teacherId} AND subject_id = #{subjectId}")
    int countByTeacherIdAndSubjectId(@Param("teacherId") Integer teacherId, @Param("subjectId") Integer subjectId);
}
