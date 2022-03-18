/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.monitor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
public class NodeTracking {

    private String nodeId;

    private String nodeName;

    private BpmnTypeEnum nodeType;

    private String ability;

    private String methodName;

    private String targetName;

    private final Set<String> toNodeIds = Sets.newConcurrentHashSet();

    private Integer index;

    private String threadId;

    private volatile LocalDateTime startTime;

    private volatile LocalDateTime endTime;

    private volatile Long spendTime;

    private final List<ParamTracking> paramTracking = Lists.newArrayList();

    private final List<NoticeTracking> noticeTracking = Lists.newArrayList();

    @JSONField(serialize = false)
    private Throwable taskException;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Set<String> getToNodeIds() {
        return toNodeIds;
    }

    public void addToNodeId(String toNodeId) {
        this.toNodeIds.add(toNodeId);
    }

    public void refreshToNodeIds(Set<String> ids) {
        toNodeIds.clear();
        toNodeIds.addAll(ids);
    }

    public BpmnTypeEnum getNodeType() {
        return nodeType;
    }

    public void setNodeType(BpmnTypeEnum nodeType) {
        this.nodeType = nodeType;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(Long spendTime) {
        this.spendTime = spendTime;
    }

    public List<ParamTracking> getParamTracking() {
        return paramTracking;
    }

    public void addParamTracking(ParamTracking paramTracking) {
        AssertUtil.notNull(paramTracking);
        this.paramTracking.add(paramTracking);
    }

    public List<NoticeTracking> getNoticeTracking() {
        return noticeTracking;
    }

    public void addNoticeTracking(NoticeTracking noticeTracking) {
        AssertUtil.notNull(noticeTracking);
        this.noticeTracking.add(noticeTracking);
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Throwable getTaskException() {
        return taskException;
    }

    public String getTaskExceptionCause() {
        if (taskException == null) {
            return null;
        }
        return GlobalUtil.expToString(taskException);
    }

    public void setTaskException(Throwable taskException) {
        this.taskException = taskException;
    }

    public boolean finishService() {
        if (nodeType != BpmnTypeEnum.SERVICE_TASK || startTime == null) {
            return true;
        }
        return spendTime != null;
    }
}
