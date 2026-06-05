package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Classes;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassesMapper {

    @Select("SELECT class_code AS classCode, subject_id AS subjectId, teacher_id AS teacherId, " +
            "term, period, fee, location, capacity, enrolled_count AS enrolledCount, " +
            "teacher_remuneration AS teacherRemuneration FROM classes")
    List<Classes> getAllClasses();

    @Select("SELECT class_code AS classCode, subject_id AS subjectId, teacher_id AS teacherId, " +
            "term, period, fee, location, capacity, enrolled_count AS enrolledCount, " +
            "teacher_remuneration AS teacherRemuneration FROM classes WHERE class_code = #{classCode}")
    Classes getClassByCode(String classCode);

    @Insert("INSERT INTO classes(class_code, subject_id, teacher_id, term, period, fee, location, capacity, enrolled_count, teacher_remuneration) " +
            "VALUES(#{classCode}, #{subjectId}, #{teacherId}, #{term}, #{period}, #{fee}, #{location}, #{capacity}, #{enrolledCount}, #{teacherRemuneration})")
    int insertClass(Classes classes);

    @Update("UPDATE classes SET enrolled_count = enrolled_count + 1 WHERE class_code = #{classCode} AND enrolled_count < capacity")
    int incrementEnrolledCount(String classCode);

    @Select("SELECT COUNT(*) FROM classes WHERE class_code = #{classCode}")
    int countByClassCode(String classCode);

    // 新增：根据科目ID查询班级
    @Select("SELECT class_code AS classCode, subject_id AS subjectId, teacher_id AS teacherId, " +
            "term, period, fee, location, capacity, enrolled_count AS enrolledCount, " +
            "teacher_remuneration AS teacherRemuneration FROM classes WHERE subject_id = #{subjectId}")
    List<Classes> getClassesBySubjectId(Integer subjectId);

    // 新增：根据教师ID查询班级（用于教师课表）
    @Select("SELECT class_code AS classCode, subject_id AS subjectId, teacher_id AS teacherId, " +
            "term, period, fee, location, capacity, enrolled_count AS enrolledCount, " +
            "teacher_remuneration AS teacherRemuneration FROM classes WHERE teacher_id = #{teacherId}")
    List<Classes> getClassesByTeacherId(Integer teacherId);

    // 统计某科目下有多少班级（用于删除科目前的校验）
    @Select("SELECT COUNT(*) FROM classes WHERE subject_id = #{subjectId}")
    int countBySubjectId(Integer subjectId);
}