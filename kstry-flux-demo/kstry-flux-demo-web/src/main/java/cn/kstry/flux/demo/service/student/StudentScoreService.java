package cn.kstry.flux.demo.service.student;

import cn.kstry.flux.demo.dto.student.*;
import cn.kstry.flux.demo.facade.student.QueryScoreRequest;
import cn.kstry.flux.demo.facade.student.QueryScoreResponse;
import cn.kstry.flux.demo.facade.student.QueryScoreVarScope;
import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bus.IterDataItem;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@TaskComponent
@SuppressWarnings("unused")
public class StudentScoreService {

    /**
     * 注册name为：getStudentBasic 描述信息为：查询学生基本信息 的服务节点方法
     *
     * @param id 从req域中获取变量名为studentId的值，并赋值给方法入参id
     * @return 将方法返回结果通知到var域的studentBasic变量上
     */
    @TaskService(desc = "查询学生基本信息")
    @NoticeVar(target = QueryScoreVarScope.F.studentBasic)
    public StudentBasic getStudentBasic(@ReqTaskParam(QueryScoreRequest.F.studentId) Long id) {
        // mock return result
        StudentBasic studentBasic = new StudentBasic();
        studentBasic.setId(id);
        studentBasic.setAddress("XX省XX市XX区");
        studentBasic.setName("张一");
        return studentBasic;
    }

    @TaskService(desc = "查询学生敏感信息")
    @NoticeVar(target = QueryScoreVarScope.F.studentPrivacy)
    public StudentPrivacy getStudentPrivacy(@ReqTaskParam(QueryScoreRequest.F.studentId) Long id) {
        // mock return result
        StudentPrivacy studentPrivacy = new StudentPrivacy();
        studentPrivacy.setId(id);
        studentPrivacy.setBirthday("1994-01-01");
        studentPrivacy.setIdCard("133133199401012345");
        return studentPrivacy;
    }

    @TaskService(desc = "装配学生信息")
    @NoticeVar(target = QueryScoreVarScope.F.student)
    public Student assembleStudentInfo(@VarTaskParam(QueryScoreVarScope.F.studentBasic) StudentBasic studentBasic,
                                       @VarTaskParam(QueryScoreVarScope.F.studentPrivacy) StudentPrivacy studentPrivacy) {
        Student student = new Student();
        BeanUtils.copyProperties(studentBasic, student);
        BeanUtils.copyProperties(studentPrivacy, student);
        return student;
    }

    @TaskService(desc = "获取学生学年列表")
    @NoticeVar(target = QueryScoreVarScope.F.studyExperienceList)
    public List<StudyExperience> getStudyExperienceList(@ReqTaskParam(QueryScoreRequest.F.studentId) Long id) {
        // mock return result
        return Lists.newArrayList(
                StudyExperience.builder().studentId(id).classId(1L).studyYear("2013-1-2").build(),
                StudyExperience.builder().studentId(id).classId(2L).studyYear("2014-1-2").build(),
                StudyExperience.builder().studentId(id).classId(3L).studyYear("2015-1-2").build()
        );
    }

    /**
     * 服务节点可以定义指定对数据域中的某个集合变量进行遍历执行
     *      sourceScope = ScopeTypeEnum.VARIABLE  指要对var域的变量进行遍历
     *      source = QueryScoreVarScope.F.classIds 指对var域中名为classIds的集合变量进行遍历
     *      async = true        指开启并发遍历模式
     *      strategy = IterateStrategyEnum.BEST_SUCCESS     指使用best策略遍历，best策略可以做到迭代集合中的全部项，执行失败会被忽略，将会尽量多的拿到成功项
     *
     * @param classIdItem  IterDataItem 类是框架内部类型，直接放到服务节点方法入参上，用来接收遍历项信息
     * @return @NoticeVar(target = QueryScoreVarScope.F.classInfos) 指定要将返回结果通知到var域中的classInfos变量上，classInfos变量是List类型，框架会自动将所有单个元素组合成List返回给变量
     */
    @TaskService(desc = "查询班级信息",
            iterator = @Iterator(sourceScope = ScopeTypeEnum.VARIABLE, source = QueryScoreVarScope.F.classIds, async = true, strategy = IterateStrategyEnum.BEST_SUCCESS)
    )
    @NoticeVar(target = QueryScoreVarScope.F.classInfos)
    public ClassInfo getClasInfoById(IterDataItem<Long> classIdItem) {
        // mock return result
        Optional<Long> idOptional = classIdItem.getData();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setId(idOptional.map((Object o) -> Long.valueOf(String.valueOf(o))).orElse(null));
        classInfo.setName("班级" + idOptional.orElse(null));
        return classInfo;
    }

    @TaskService(desc = "查询学生分数列表")
    @NoticeVar(target = QueryScoreVarScope.F.scoreInfos)
    public List<ScoreInfo> getStudentScoreList(@VarTaskParam(QueryScoreVarScope.F.studyExperienceList) List<StudyExperience> studyExperienceList) {
        // mock return result
        return studyExperienceList.stream().flatMap(se -> {
            List<ScoreInfo> scoreInfos = Lists.newArrayList(
                    ScoreInfo.builder().studentId(se.getStudentId()).classId(se.getClassId()).studyYear(se.getStudyYear()).course("语文").score(99).build(),
                    ScoreInfo.builder().studentId(se.getStudentId()).classId(se.getClassId()).studyYear(se.getStudyYear()).course("数学").score(88).build()
            );
            return scoreInfos.stream();
        }).collect(Collectors.toList());
    }

    @TaskService(desc = "组装成绩及班级信息")
    @NoticeVar(target = QueryScoreVarScope.F.scoreInfos)
    public List<ScoreInfo> assembleScoreClassInfo(@VarTaskParam(QueryScoreVarScope.F.scoreInfos) List<ScoreInfo> scoreInfos,
                                                  @VarTaskParam(QueryScoreVarScope.F.classInfos) List<ClassInfo> classInfos) {
        Map<Long, ClassInfo> classInfoMap = classInfos.stream().collect(Collectors.toMap(ClassInfo::getId, Function.identity(), (x, y) -> y));
        scoreInfos.forEach(scoreInfo -> {
            ClassInfo classInfo = classInfoMap.get(scoreInfo.getClassId());
            if (classInfo == null) {
                return;
            }
            scoreInfo.setClassInfo(classInfo);
        });
        return scoreInfos;
    }

    @NoticeResult
    @TaskService(desc = "各维度信息聚合")
    public QueryScoreResponse getQueryScoreResponse(@VarTaskParam(QueryScoreVarScope.F.student) Student student,
                                                    @VarTaskParam(QueryScoreVarScope.F.scoreInfos) List<ScoreInfo> scoreInfos) {
        QueryScoreResponse response = new QueryScoreResponse();
        response.setStudent(student);
        response.setScoreInfos(scoreInfos);
        return response;
    }

    @NoticeResult
    @TaskService
    public boolean gotoSchool(int askWeek, boolean askRain, boolean askHungry) {
        log.info("gotoSchool. askWeek: {}, askRain: {}, askHungry: {}", askWeek, askRain, askHungry);
        return true;
    }
}
