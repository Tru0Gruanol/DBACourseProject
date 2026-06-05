package com.training.centermanagement.service;

import com.training.centermanagement.entity.Subject;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.SubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private ClassesMapper classesMapper;

    public List<Subject> getAllSubjects() {
        return subjectMapper.getAllSubjects();
    }

    public Subject getSubjectById(Integer subjectId) {
        return subjectMapper.getSubjectById(subjectId);
    }

    public String addSubject(Subject subject) {
        if (subjectMapper.getSubjectById(subject.getSubjectId()) != null) {
            return "科目ID已存在！";
        }
        return subjectMapper.insertSubject(subject) > 0 ? "添加成功" : "添加失败";
    }

    public String updateSubject(Subject subject) {
        return subjectMapper.updateSubject(subject) > 0 ? "更新成功" : "更新失败（可能ID不存在）";
    }

    public String deleteSubject(Integer subjectId) {
        if (classesMapper.countBySubjectId(subjectId) > 0) {
            return "删除失败：该科目下还有班级，请先删除班级";
        }
        return subjectMapper.deleteSubject(subjectId) > 0 ? "删除成功" : "删除失败";
    }
}