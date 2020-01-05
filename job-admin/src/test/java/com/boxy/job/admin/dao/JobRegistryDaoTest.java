package com.boxy.job.admin.dao;

import com.boxy.job.admin.core.model.JobRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JobRegistryDaoTest {

    @Resource
    private JobRegistryDao jobRegistryDao;

    @Test
    public void test(){
        int ret = jobRegistryDao.registryUpdate("g1", "k1", "v1", new Date());
        if (ret < 1) {
            ret = jobRegistryDao.registrySave("g1", "k1", "v1", new Date());
        }

        List<JobRegistry> list = jobRegistryDao.findAll(1, new Date());

        int ret2 = jobRegistryDao.removeDead(Arrays.asList(1));
    }

}
