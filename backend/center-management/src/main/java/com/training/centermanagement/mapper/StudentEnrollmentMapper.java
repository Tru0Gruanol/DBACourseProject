package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.StudentEnrollment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentEnrollmentMapper {

    @Insert("INSERT INTO student_enrollments(student_id, class_code, enrollment_time, amount_paid, status) " +
            "VALUES(#{studentId}, #{classCode}, #{enrollmentTime}, #{amountPaid}, 'active')")
    int insertEnrollment(StudentEnrollment enrollment);

    // 查询在读报名（用于缴费、退课等业务操作）
    @Select("SELECT * FROM student_enrollments WHERE student_id = #{studentId} AND class_code = #{classCode} AND status = 'active'")
    StudentEnrollment selectByStudentAndClass(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 查询任意状态的报名记录（用于检测是否存在已退课的记录）
    @Select("SELECT * FROM student_enrollments WHERE student_id = #{studentId} AND class_code = #{classCode}")
    StudentEnrollment selectAnyByStudentAndClass(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 获取学生选课的所有在读班级代码（用于课表）
    @Select("SELECT class_code FROM student_enrollments WHERE student_id = #{studentId} AND status = 'active'")
    List<String> getClassCodesByStudentId(Integer studentId);

    // 物理删除一条报名记录（仅在删除学生/班级时使用）
    @Delete("DELETE FROM student_enrollments WHERE student_id = #{studentId} AND class_code = #{classCode}")
    int deleteEnrollment(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 软取消：将报名状态改为 cancelled（退课操作）
    @Update("UPDATE student_enrollments SET status = 'cancelled' WHERE student_id = #{studentId} AND class_code = #{classCode}")
    int cancelEnrollment(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 重新报名：将已退课的记录恢复为 active（复用已有记录）
    @Update("UPDATE student_enrollments SET status = 'active', amount_paid = #{amountPaid}, enrollment_time = #{enrollmentTime} WHERE student_id = #{studentId} AND class_code = #{classCode}")
    int reEnroll(@Param("studentId") Integer studentId, @Param("classCode") String classCode, @Param("amountPaid") java.math.BigDecimal amountPaid, @Param("enrollmentTime") java.util.Date enrollmentTime);

    // 统计某班级下的在读人数（用于删除班级前的校验）
    @Select("SELECT COUNT(*) FROM student_enrollments WHERE class_code = #{classCode} AND status = 'active'")
    int countByClassCode(String classCode);

    // 统计某学生的在读选课数量（用于删除学生前的校验）
    @Select("SELECT COUNT(*) FROM student_enrollments WHERE student_id = #{studentId} AND status = 'active'")
    int countByStudentId(Integer studentId);

    // 查询某学生的全部报名记录（含已退课，缴费查询页用）
    @Select("SELECT * FROM student_enrollments WHERE student_id = #{studentId}")
    List<StudentEnrollment> getAllEnrollmentsByStudentId(Integer studentId);

    // 查询某学生的所有在读报名（缴费校验用）
    @Select("SELECT * FROM student_enrollments WHERE student_id = #{studentId} AND status = 'active'")
    List<StudentEnrollment> getActiveEnrollmentsByStudentId(Integer studentId);

    // 物理删除某学生的所有报名记录（删除学生时用）
    @Delete("DELETE FROM student_enrollments WHERE student_id = #{studentId}")
    int deleteByStudentId(Integer studentId);

    // 物理删除某班级的所有报名记录（删除班级时用）
    @Delete("DELETE FROM student_enrollments WHERE class_code = #{classCode}")
    int deleteByClassCode(String classCode);
}