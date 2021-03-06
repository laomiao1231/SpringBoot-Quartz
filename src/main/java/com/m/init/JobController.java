package com.m.init;

import com.m.dao.JobEntityRepository;
import com.m.entity.JobEntity;
import com.m.init.dto.ModifyCronDTO;
import com.m.service.DynamicJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Created by EalenXie on 2018/6/4 16:12
 */
@RestController
@Slf4j
public class JobController {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    @Autowired
    private DynamicJobService jobService;

    @Autowired
    private JobEntityRepository repository;

    //根据ID重启某个Job
    @RequestMapping("/refresh/{id}")
    public String refresh(@PathVariable @NotNull Integer id) throws Exception {
        String result;
        JobEntity entity = jobService.getJobEntityById(id);
        if (Objects.isNull(entity)) return "error: id is not exist ";
        synchronized (log) {
            if ("CLOSE".equals(entity.getStatus())) {
                entity.setStatus("OPEN");
                repository.save(entity);
            }
            JobKey jobKey = jobService.getJobKey(entity);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(jobKey);
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
            scheduler.deleteJob(jobKey);
            JobDataMap map = jobService.getJobDataMap(entity);
            JobDetail jobDetail = jobService.getJobDetail(jobKey, entity.getDescription(), map);
            if (entity.getStatus().equals("OPEN")) {
                scheduler.scheduleJob(jobDetail, jobService.getTrigger(entity));
                result = "Refresh Job : " + entity.getName() + "\t jarPath: " + entity.getJarPath() + " success !";
            } else {
                result = "Refresh Job : " + entity.getName() + "\t jarPath: " + entity.getJarPath() + " failed ! , " +
                        "Because the Job status is " + entity.getStatus();
            }
        }
        return result;
    }

    //修改某个Job执行的Cron
    @PostMapping("/modifyJob")
    public String modifyJob(@RequestBody @Validated ModifyCronDTO dto) {
        if (!CronExpression.isValidExpression(dto.getCron())) return "cron is invalid !";
        synchronized (log) {
            JobEntity job = jobService.getJobEntityById(dto.getId());
            if (job.getStatus().equals("OPEN")) {
                try {
                    JobKey jobKey = jobService.getJobKey(job);
                    TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());
                    Scheduler scheduler = schedulerFactoryBean.getScheduler();
                    CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
                    String oldCron = cronTrigger.getCronExpression();
                    if (!oldCron.equalsIgnoreCase(dto.getCron())) {
                        job.setCron(dto.getCron());
                        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(dto.getCron());
                        CronTrigger trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobKey.getName(), jobKey.getGroup())
                                .withSchedule(cronScheduleBuilder)
                                .usingJobData(jobService.getJobDataMap(job))
                                .build();
                        scheduler.rescheduleJob(triggerKey, trigger);
                        repository.save(job);
                    }
                } catch (Exception e) {
                    log.error("printStackTrace", e);
                }
            } else {
                log.info("Job jump name : {} , Because {} status is {}", job.getName(), job.getName(), job.getStatus());
                return "modify failure , because the job is closed";
            }
        }
        return "modify success";
    }

    @GetMapping("/remove/{id}")
    public String removeJob(@PathVariable("id") @NotNull Integer id) {
        JobEntity job = jobService.getJobEntityById(id);
        if (job.getStatus().equals("OPEN")) {
            String jobName = job.getName();
            String groupName = job.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);
            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            try {
                Scheduler scheduler = schedulerFactoryBean.getScheduler();
                Trigger trigger = scheduler.getTrigger(triggerKey);
                if (trigger == null) {
                    return "this job is not exist";
                }
                scheduler.pauseTrigger(triggerKey);// 停止触发器
                scheduler.unscheduleJob(triggerKey);// 移除触发器
                scheduler.deleteJob(jobKey);// 删除任务
                job.setStatus("CLOSE");
                repository.save(job);
                return "this job " + jobName + " close";
            } catch (Exception e) {
                log.error("printStackTrace", e);
                return "this job " + jobName + " close failure";
            }
        } else {
            return "this job already close";
        }
    }


}
