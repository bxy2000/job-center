package com.boxy.job.admin.dao;

import com.boxy.job.admin.core.model.JobLogGlue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JobLogGlueDaoTest {

    @Resource
    private JobLogGlueDao jobLogGlueDao;

    @Test
    public void test(){
        JobLogGlue logGlue = new JobLogGlue();
        logGlue.setJobId(1);
        logGlue.setGlueType("1");
        logGlue.setGlueSource("1");
        logGlue.setGlueRemark("1");

        logGlue.setAddTime(new Date());
        logGlue.setUpdateTime(new Date());
        int ret = jobLogGlueDao.save(logGlue);

        List<JobLogGlue> list = jobLogGlueDao.findByJobId(1);

        int ret2 = jobLogGlueDao.removeOld(1, 1);

        int ret3 =jobLogGlueDao.deleteByJobId(1);
    }

}
