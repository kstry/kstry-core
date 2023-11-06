package cn.kstry.framework.test.diagram.constants;

import lombok.experimental.FieldNameConstants;

@SuppressWarnings("unused")
@FieldNameConstants(innerTypeName = "F")
public class SCS {

    /**
     * 计算组件
     */
    private String CALCULATE_SERVICE;

    /**
     * 计算组件中服务节点
     */
    @FieldNameConstants(innerTypeName = "F")
    public static class CALCULATE_SERVICE {

        /**
         * 加一
         */
        private String INCREASE_ONE;

        /**
         * 相乘
         */
        private String MULTIPLY_PLUS;

        /**
         * 增加超时
         */
        private String INCREASE_TIMEOUT;

        /**
         * 数组元素 +1
         */
        private String INCREASE_ARRAY_ONE;

        /**
         * 计算错误
         */
        private String CALCULATE_ERROR;

        /**
         * 计算错误，进行降级
         */
        private String CALCULATE_ERROR_DEMOTION;
    }
}
