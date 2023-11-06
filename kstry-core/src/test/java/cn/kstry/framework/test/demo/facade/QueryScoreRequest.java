package cn.kstry.framework.test.demo.facade;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants(innerTypeName = "F")
public class QueryScoreRequest {

    private Long studentId;

    private boolean needScore;
}
