package cn.kstry.framework.test.flow.bo;

import cn.kstry.framework.core.annotation.NoticeResult;
import cn.kstry.framework.core.annotation.ReqTaskField;

import javax.validation.constraints.NotNull;

@NoticeResult
public class Goods {

    @NotNull(message = "goods.id 不能为空")
    @ReqTaskField("id")
    private Long id;

    private String name;

    private Long price;

    private Hospital hospital;

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

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }
}
