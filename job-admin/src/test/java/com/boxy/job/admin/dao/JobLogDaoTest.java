package com.boxy.job.admin.dao;

import com.boxy.job.admin.core.model.JobLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JobLogDaoTest {

    @Resource
    private JobLogDao jobLogDao;

    @Test
    public void test(){
        List<JobLog> list = jobLogDao.pageList(0, 10, 1, 1, null, null, 1);
        int list_count = jobLogDao.pageListCount(0, 10, 1, 1, null, null, 1);

        JobLog log = new JobLog();
        log.setJobGroup(1);
        log.setJobId(1);

        long ret1 = jobLogDao.save(log);
        JobLog dto = jobLogDao.load(log.getId());

        log.setTriggerTime(new Date());
        log.setTriggerCode(1);
        log.setTriggerMsg("1");
        log.setExecutorAddress("1");
        log.setExecutorHandler("1");
        log.setExecutorParam("1");
        ret1 = jobLogDao.updateTriggerInfo(log);
        dto = jobLogDao.load(log.getId());


        log.setHandleTime(new Date());
        log.setHandleCode(2);
        log.setHandleMsg("2");
        ret1 = jobLogDao.updateHandleInfo(log);
        dto = jobLogDao.load(log.getId());


        List<Long> ret4 = jobLogDao.findClearLogIds(1, 1, new Date(), 100, 100);

        int ret2 = jobLogDao.delete(log.getJobId());

    }

}
