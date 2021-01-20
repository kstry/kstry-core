package cn.kstry.framework.core.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * 策略类型
 *
 * @author lykan
 */
public enum StrategyTypeEnum {

    /**
     * 匹配成功，执行当前事件，匹配失败跳过当前事件继续执行下一个事件
     */
    FILTER("FILTER"),

    /**
     * 匹配成功，将事件流切至当前事件执行
     */
    MATCH("MATCH");

    /**
     * 策略类型
     */
    private final String type;

    StrategyTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Optional<StrategyTypeEnum> getStrategyTypeEnum(String type) {

        if (StringUtils.isBlank(type)) {
            return Optional.empty();
        }
        for (StrategyTypeEnum typeEnum : StrategyTypeEnum.values()) {
            if (typeEnum.getType().toUpperCase().equals(type.trim().toUpperCase())) {
                return Optional.of(typeEnum);
            }
        }
        return Optional.empty();
    }

    public static boolean isType(String type, StrategyTypeEnum typeEnum) {

        if (typeEnum == null) {
            return false;
        }

        Optional<StrategyTypeEnum> strategyTypeOptional = getStrategyTypeEnum(type);
        return strategyTypeOptional.isPresent() && strategyTypeOptional.get() == typeEnum;
    }
}
