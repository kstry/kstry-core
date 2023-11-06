package cn.kstry.flux.demo.facade.student;

import cn.kstry.flux.demo.dto.student.ScoreInfo;
import cn.kstry.flux.demo.dto.student.Student;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@FieldNameConstants(innerTypeName = "F")
public class QueryScoreResponse {

    private Student student;

    private List<ScoreInfo> scoreInfos;
}
