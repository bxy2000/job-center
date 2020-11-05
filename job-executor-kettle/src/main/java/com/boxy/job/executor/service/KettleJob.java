package com.boxy.job.executor.service;

import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.handler.annotation.Job;
import com.boxy.job.executor.utils.KettleRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KettleJob {
    private static Logger logger = LoggerFactory.getLogger(KettleJob.class);

    @Job("kettleJobHandler")
    public ReturnT<String> kettleJobHandler(String filename) throws Exception {
        return KettleRunner.runJobFromFileSystem(filename);
    }

    @Job("kettleTransHandler")
    public ReturnT<String> kettleTransformationHandler(String filename) throws Exception {
        return KettleRunner.runTransformationFromFileSystem(filename);
    }
}
