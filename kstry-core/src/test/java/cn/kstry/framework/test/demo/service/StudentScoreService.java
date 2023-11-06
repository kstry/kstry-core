package cn.kstry.framework.test.demo.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bus.IterDataItem;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.demo.bo.*;
import cn.kstry.framework.test.demo.facade.QueryScoreRequest;
import cn.kstry.framework.test.demo.facade.QueryScoreResponse;
import cn.kstry.framework.test.demo.facade.QueryScoreVarScope;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@TaskComponent
@SuppressWarnings("unused")
public class StudentScoreService {

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

    @TaskService(desc = "查询班级信息", invoke = @Invoke(timeout = 1000),
            iterator = @Iterator(sourceScope = ScopeTypeEnum.VARIABLE, source = "$.studyExperienceList[*].classId", strategy = IterateStrategyEnum.BEST_SUCCESS)
    )
    @NoticeVar(target = QueryScoreVarScope.F.classInfos)
    public Mono<ClassInfo> getClasInfoById(IterDataItem<Long> classIdItem) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            // mock return result
            Optional<Long> idOptional = classIdItem.getData();
            ClassInfo classInfo = new ClassInfo();
            classInfo.setId(idOptional.orElse(null));
            classInfo.setName("班级" + idOptional.orElse(null));
            if (classInfo.getId() == 2) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return classInfo;
        }, Executors.newFixedThreadPool(3)));
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
    public void assembleScoreClassInfo(@VarTaskParam(QueryScoreVarScope.F.scoreInfos) List<ScoreInfo> scoreInfos,
                                       @VarTaskParam(QueryScoreVarScope.F.classInfos) List<ClassInfo> classInfos) {
        Map<Long, ClassInfo> classInfoMap = classInfos.stream().collect(Collectors.toMap(ClassInfo::getId, Function.identity(), (x, y) -> y));
        scoreInfos.forEach(scoreInfo -> {
            ClassInfo classInfo = classInfoMap.get(scoreInfo.getClassId());
            if (classInfo == null) {
                return;
            }
            scoreInfo.setClassInfo(classInfo);
        });
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
}
