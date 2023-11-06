package cn.kstry.framework.test.demo.facade;

import cn.kstry.framework.test.demo.bo.ScoreInfo;
import cn.kstry.framework.test.demo.bo.Student;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@FieldNameConstants(innerTypeName = "F")
public class QueryScoreResponse {

    private Student student;

    private List<ScoreInfo> scoreInfos;
}
