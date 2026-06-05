package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Subject;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubjectMapper {

    @Select("SELECT subject_id AS subjectId, subject_name AS subjectName, hours FROM subjects")
    List<Subject> getAllSubjects();

    @Select("SELECT subject_id AS subjectId, subject_name AS subjectName, hours FROM subjects WHERE subject_id = #{subjectId}")
    Subject getSubjectById(Integer subjectId);

    @Select("SELECT COUNT(*) FROM subjects WHERE subject_id = #{subjectId}")
    int countBySubjectId(Integer subjectId);

    // 新增：添加科目
    @Insert("INSERT INTO subjects(subject_id, subject_name, hours) VALUES(#{subjectId}, #{subjectName}, #{hours})")
    int insertSubject(Subject subject);

    // 新增：更新科目
    @Update("UPDATE subjects SET subject_name = #{subjectName}, hours = #{hours} WHERE subject_id = #{subjectId}")
    int updateSubject(Subject subject);

    // 新增：删除科目
    @Delete("DELETE FROM subjects WHERE subject_id = #{subjectId}")
    int deleteSubject(Integer subjectId);
}