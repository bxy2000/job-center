package com.boxy.job.executor.service;

import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.context.JobHelper;
import com.boxy.job.core.handler.annotation.Job;
import com.boxy.job.executor.utils.KettleRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KettleJob {
    private static Logger logger = LoggerFactory.getLogger(KettleJob.class);

    @Job("kettleJobHandler")
    public void kettleJobHandler() throws Exception {
        String filename = JobHelper.getJobParam();
        KettleRunner.runJobFromFileSystem(filename);
    }

    @Job("kettleTransHandler")
    public void kettleTransformationHandler() throws Exception {
        String filename = JobHelper.getJobParam();
        KettleRunner.runTransformationFromFileSystem(filename);
    }
}
