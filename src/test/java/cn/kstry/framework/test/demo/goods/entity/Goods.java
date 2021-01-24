package cn.kstry.framework.test.demo.goods.entity;

/**
 *
 * @author lykan
 */
public class Goods {

    private Long id;

    private String name;

    private long money;

    public Goods(Long id, String name, long money) {
        this.id = id;
        this.name = name;
        this.money = money;
    }

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

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

}
