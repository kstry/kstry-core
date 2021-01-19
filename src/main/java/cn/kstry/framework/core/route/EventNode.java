package cn.kstry.framework.core.route;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.LocateBehavior;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class EventNode {
    /**
     * 当前节点中保存的接下来，可以允许被执行的 MpaNode
     */
    private final List<EventNode> nextMapNodeList = new ArrayList<>();

    /**
     * 前一个 MapNode
     */
    private EventNode prevMapNode;

    /**
     * 具体参与控制程序执行的路由 Node
     * MapNode 与 RouteNode 一一对应
     */
    private final TaskNode taskNode;

    public EventNode(TaskNode taskNode) {
        AssertUtil.notNull(taskNode);
        this.taskNode = taskNode;
        taskNode.setMapNode(this);
    }

    public void addNextMapNode(EventNode mapNode) {
        AssertUtil.notNull(mapNode);
        if (nextMapNodeList.contains(mapNode)) {
            return;
        }
        nextMapNodeList.add(mapNode);
        mapNode.setPrevMapNode(this);
    }

    public Optional<EventNode> locateNextMapNode(LocateBehavior<TaskNode> locateBehavior) {
        if (CollectionUtils.isEmpty(nextMapNodeList)) {
            return Optional.empty();
        }

        if (locateBehavior == null) {
            return Optional.ofNullable(nextMapNodeList.get(0));
        }

        List<EventNode> resultList = nextMapNodeList.stream().filter(map -> locateBehavior.locate(map.getTaskNode())).collect(Collectors.toList());
        AssertUtil.oneSize(resultList, ExceptionEnum.INVALID_POSITIONING_BEHAVIOR);
        return Optional.of(resultList.get(0));
    }

    public Optional<EventNode> locateNextMapNode() {
        return this.locateNextMapNode(null);
    }

    public TaskNode getTaskNode() {
        return taskNode;
    }

    public EventNode getPrevMapNode() {
        return prevMapNode;
    }

    public List<EventNode> getNextMapNodeList() {
        return nextMapNodeList;
    }

    public int nextMapNodeSize() {
        if (CollectionUtils.isEmpty(this.nextMapNodeList)) {
            return 0;
        }
        return this.nextMapNodeList.size();
    }

    private void setPrevMapNode(EventNode prevMapNode) {
        this.prevMapNode = prevMapNode;
    }
}
