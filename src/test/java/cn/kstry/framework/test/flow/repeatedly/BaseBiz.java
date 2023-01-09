package cn.kstry.framework.test.flow.repeatedly;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseBiz<A, B> {

    protected abstract A invoke(B param);

    protected abstract A invoke();
}
