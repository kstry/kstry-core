package cn.kstry.framework.test.demo.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习经历
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyExperience {

    private Long studentId;

    private Long classId;

    private String studyYear;
}
