package cn.kstry.framework.test.demo.service;

import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.annotation.VarTaskParam;
import cn.kstry.framework.test.demo.bo.ClassInfo;
import cn.kstry.framework.test.demo.bo.Student;

@TaskComponent
public class FlowDemoService {

    @NoticeVar
    @TaskService(desc = "获取学生信息")
    public Student getStudentInfo(@VarTaskParam Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        student.setName("Name" + studentId);
        return student;
    }

    @NoticeVar
    @TaskService(desc = "获取班级信息")
    public ClassInfo getClassInfo(@VarTaskParam Long classId) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setId(classId);
        classInfo.setName("Name" + classId);
        return classInfo;
    }
}
