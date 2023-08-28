package cn.kstry.framework.test.bus.bo;

import cn.kstry.framework.core.annotation.VarTaskField;
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

    @VarTaskField
    private List<String> oneItemList;

    @VarTaskField
    private Set<String> oneItemSet;

    @VarTaskField
    private String firstItemList;

    @VarTaskField
    private int zzInt;
}
