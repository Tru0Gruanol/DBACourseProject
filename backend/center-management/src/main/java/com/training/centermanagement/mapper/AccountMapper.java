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

    // 新增：查询所有欠费学生（联表）
    @Select("SELECT s.student_id, s.student_name, c.class_code, c.fee, e.amount_paid, " +
            "(c.fee - e.amount_paid) AS debt " +
            "FROM student_enrollments e " +
            "JOIN students s ON e.student_id = s.student_id " +
            "JOIN classes c ON e.class_code = c.class_code " +
            "WHERE e.amount_paid < c.fee")
    List<Map<String, Object>> getDebtors();
}