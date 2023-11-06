package cn.kstry.framework.test.flow.repeatedly;

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;

@TaskComponent(name = "asf")
public class AsfBiz extends BaseBiz<String, String> {

    @Override
    @TaskService(name = "invoke-repeatedly-defined")
    public String invoke(String param) {
        return null;
    }

    @Override
    @TaskService(name = "invoke-repeatedly-defined2")
    public String invoke() {
        return null;
    }
}
