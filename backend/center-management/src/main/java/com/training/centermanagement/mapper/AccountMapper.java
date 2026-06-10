package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Account;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AccountMapper {

    @Insert("INSERT INTO accounts(account_date, class_code, student_id, subject_id, amount_paid) " +
            "VALUES(#{accountDate}, #{classCode}, #{studentId}, #{subjectId}, #{amountPaid})")
    int insertAccount(Account account);

    @Select("SELECT account_id AS accountId, account_date AS accountDate, class_code AS classCode, " +
            "student_id AS studentId, subject_id AS subjectId, amount_paid AS amountPaid FROM accounts")
    List<Account> getAllAccounts();

    // 新增：根据学生和班级查询缴费流水
    @Select("SELECT * FROM accounts WHERE student_id = #{studentId} AND class_code = #{classCode}")
    List<Account> getAccountsByStudentAndClass(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 新增：更新选课表中的已缴金额（累加）
    @Update("UPDATE student_enrollments SET amount_paid = amount_paid + #{amount} " +
            "WHERE student_id = #{studentId} AND class_code = #{classCode}")
    int updateEnrollmentAmountPaid(@Param("studentId") Integer studentId,
                                   @Param("classCode") String classCode,
                                   @Param("amount") BigDecimal amount);

    // 新增：查询所有欠费学生（联表），使用驼峰别名匹配前端 prop，只看在读报名
    @Select("SELECT s.student_id AS studentId, s.student_name AS studentName, " +
            "c.class_code AS classCode, c.fee AS totalFee, e.amount_paid AS totalPaid, " +
            "(c.fee - e.amount_paid) AS debt " +
            "FROM student_enrollments e " +
            "JOIN students s ON e.student_id = s.student_id " +
            "JOIN classes c ON e.class_code = c.class_code " +
            "WHERE e.amount_paid < c.fee AND e.status = 'active'")
    List<Map<String, Object>> getDebtors();

    // 统计某学生的账目流水数（用于删除学生前的校验）
    @Select("SELECT COUNT(*) FROM accounts WHERE student_id = #{studentId}")
    int countByStudentId(Integer studentId);

    // 查询某学生+班级的累计缴费总额（用于退课前判断多缴/欠费）
    @Select("SELECT COALESCE(SUM(amount_paid), 0) FROM accounts WHERE student_id = #{studentId} AND class_code = #{classCode}")
    java.math.BigDecimal getTotalPaidByStudentAndClass(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 删除某学生+某班级的所有账目流水
    @Delete("DELETE FROM accounts WHERE student_id = #{studentId} AND class_code = #{classCode}")
    int deleteByStudentAndClass(@Param("studentId") Integer studentId, @Param("classCode") String classCode);

    // 删除某学生的所有账目流水（删除学生时清理FK）
    @Delete("DELETE FROM accounts WHERE student_id = #{studentId}")
    int deleteByStudentId(Integer studentId);

    // 删除某班级的所有账目流水（删除班级时的安全网清理）
    @Delete("DELETE FROM accounts WHERE class_code = #{classCode}")
    int deleteByClassCode(String classCode);
}