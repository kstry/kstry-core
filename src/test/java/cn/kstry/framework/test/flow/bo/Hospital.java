package cn.kstry.framework.test.flow.bo;

import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.SpringInitialization;
import cn.kstry.framework.core.annotation.StaTaskField;

@NoticeSta
@SpringInitialization
public class Hospital {

    @StaTaskField("hospital.id")
    private Long id;

    @NoticeSta
    @StaTaskField("hospital.name")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
