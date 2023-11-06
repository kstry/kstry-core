package cn.kstry.flux.demo.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreInfo {

    private int score;

    private Long studentId;

    private String studyYear;

    private String course;

    private Long classId;

    private ClassInfo classInfo;
}
