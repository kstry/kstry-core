package cn.kstry.framework.test.demo.facade;

import cn.kstry.framework.test.demo.bo.ClassInfo;
import cn.kstry.framework.test.demo.bo.Student;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants(innerTypeName = "F")
public class CommonFields {

    private int type;

    private int a;

    private int b;

    private String factor;

    private int calculateRes;

    private Long studentId;

    private Long classId;

    private Student student;

    private ClassInfo classInfo;
}
