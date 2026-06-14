package com.training.centermanagement.service;

import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.Subject;
import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import com.training.centermanagement.mapper.SubjectMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ClassesService {

    @Autowired
    private ClassesMapper classesMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private NotificationService notificationService;

    public List<Classes> getAllClasses() {
        return classesMapper.getAllClasses();
    }

    public Classes getClassByCode(String classCode) {
        return classesMapper.getClassByCode(classCode);
    }

    public String addClass(Classes classes) {
        if (classesMapper.countByClassCode(classes.getClassCode()) > 0) {
            return "排课失败：班级代号 " + classes.getClassCode() + " 已存在！";
        }
        if (teacherMapper.countByTeacherId(classes.getTeacherId()) == 0) {
            return "排课失败：教师ID " + classes.getTeacherId() + " 不存在！";
        }
        if (subjectMapper.countBySubjectId(classes.getSubjectId()) == 0) {
            return "排课失败：科目ID " + classes.getSubjectId() + " 不存在！";
        }
        if (classes.getEnrolledCount() == null) {
            classes.setEnrolledCount(0);
        }
        int rows = classesMapper.insertClass(classes);
        if (rows > 0) {
            // 通知教师
            Subject subject = subjectMapper.getSubjectById(classes.getSubjectId());
            String subjectName = subject != null ? subject.getSubjectName() : "";
            Teacher teacher = teacherMapper.getTeacherById(classes.getTeacherId());
            String teacherName = teacher != null ? teacher.getTeacherName() : "";
            notificationService.send(classes.getTeacherId(), "teacher",
                "📅 新排课",
                teacherName + " 老师，您已被安排授课 " + classes.getClassCode() + "（" + subjectName + "），时间：" + classes.getPeriod() + "，教室：" + classes.getLocation());
            return "排课成功";
        }
        return "排课失败";
    }

    public String updateClass(Classes classes) {
        Classes existing = classesMapper.getClassByCode(classes.getClassCode());
        if (existing == null) {
            return "更新失败：班级代号 " + classes.getClassCode() + " 不存在！";
        }
        if (teacherMapper.countByTeacherId(classes.getTeacherId()) == 0) {
            return "更新失败：教师ID " + classes.getTeacherId() + " 不存在！";
        }
        if (subjectMapper.countBySubjectId(classes.getSubjectId()) == 0) {
            return "更新失败：科目ID " + classes.getSubjectId() + " 不存在！";
        }
        // enrolledCount 只能由报名/退课操作修改，不允许通过编辑班级直接覆盖
        classes.setEnrolledCount(existing.getEnrolledCount());
        int rows = classesMapper.updateClass(classes);
        if (rows > 0) {
            // 检查关键字段是否变更，如有则通知教师和全体在读学生
            boolean periodChanged = !Objects.equals(existing.getPeriod(), classes.getPeriod());
            boolean locationChanged = !Objects.equals(existing.getLocation(), classes.getLocation());
            boolean teacherChanged = !Objects.equals(existing.getTeacherId(), classes.getTeacherId());

            if (periodChanged || locationChanged || teacherChanged) {
                Subject subject = subjectMapper.getSubjectById(classes.getSubjectId());
                String subjectName = subject != null ? subject.getSubjectName() : "";
                StringBuilder changes = new StringBuilder();
                if (periodChanged) changes.append("上课时间变更为 ").append(classes.getPeriod()).append("；");
                if (locationChanged) changes.append("教室变更为 ").append(classes.getLocation()).append("；");
                if (teacherChanged) {
                    Teacher newTeacher = teacherMapper.getTeacherById(classes.getTeacherId());
                    changes.append("任课教师变更为 ").append(newTeacher != null ? newTeacher.getTeacherName() : "").append("；");
                }

                // 通知教师
                notificationService.send(classes.getTeacherId(), "teacher",
                    "📅 班级信息变更",
                    classes.getClassCode() + "（" + subjectName + "）" + changes);

                // 通知全体在读学生
                List<Map<String, Object>> activeStudents = studentEnrollmentMapper.getActiveStudentsByClassCode(classes.getClassCode());
                for (Map<String, Object> s : activeStudents) {
                    Integer sid = (Integer) s.get("studentId");
                    notificationService.send(sid, "student",
                        "📅 班级信息变更",
                        "您所在的班级 " + classes.getClassCode() + "（" + subjectName + "）" + changes);
                }
            }
            return "更新成功";
        }
        return "更新失败";
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteClass(String classCode) {
        if (classesMapper.getClassByCode(classCode) == null) {
            return "删除失败：班级代号 " + classCode + " 不存在！";
        }
        // 检查是否有在读学生
        if (studentEnrollmentMapper.countByClassCode(classCode) > 0) {
            return "删除失败：该班级下还有在读学生，请先处理退课！";
        }
        // 所有报名均已取消：清理流水 → 清理报名 → 删除班级
        accountMapper.deleteByClassCode(classCode);
        studentEnrollmentMapper.deleteByClassCode(classCode);
        return classesMapper.deleteClass(classCode) > 0 ? "删除成功" : "删除失败";
    }

    // 新增：根据科目ID查询班级
    public List<Classes> getClassesBySubjectId(Integer subjectId) {
        return classesMapper.getClassesBySubjectId(subjectId);
    }
}