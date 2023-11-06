package cn.kstry.framework.test.bus.bo;

import cn.kstry.framework.core.annotation.VarTaskField;
import cn.kstry.framework.core.constant.TypeConverterNames;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ConvTBo {

    @VarTaskField
    private String nowStr;

    @VarTaskField
    private String localNowStr;

    @VarTaskField
    private boolean objBool;

    @VarTaskField(converter = TypeConverterNames.ONE_ITEM_TO_LIST)
    private List<String> oneItemList;

    @VarTaskField(converter = TypeConverterNames.ONE_ITEM_TO_SET)
    private Set<String> oneItemSet;

    @VarTaskField(converter = TypeConverterNames.FIRST_ITEM_FROM_LIST)
    private String firstItemList;

    @VarTaskField(converter = TypeConverterNames.FIRST_ITEM_FROM_LIST)
    private int zzInt;
}
