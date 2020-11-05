package com.boxy.job.executor.utils;

import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.log.JobLogger;
import com.boxy.job.executor.service.KettleJob;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KettleRunner {
    private static Logger logger = LoggerFactory.getLogger(KettleRunner.class);

    static {
        try {
            KettleEnvironment.init();
        } catch (Exception e) {
            logger.error("kettle初始化失败！");
        }
    }

    public static ReturnT<String> runJobFromFileSystem(String filename) throws Exception {

        JobMeta jobMeta = new JobMeta(filename, null);

        Job job = new Job(null, jobMeta);

        job.setLogLevel(LogLevel.BASIC);

        job.start();

        job.waitUntilFinished();

        Result result = job.getResult();

        JobLogger.log(result.getLogText());

        job = null;
        jobMeta = null;
        if (result.getNrErrors() == 0) {
            return ReturnT.SUCCESS;
        } else {
            return ReturnT.FAIL;
        }
    }

    public static ReturnT<String> runTransformationFromFileSystem(String filename) throws Exception {

        TransMeta transMeta = new TransMeta(filename, (Repository) null);

        Trans transformation = new Trans(transMeta);

        transformation.setLogLevel(LogLevel.BASIC);

        transformation.execute(new String[0]);

        transformation.waitUntilFinished();

        Result result = transformation.getResult();

        LoggingBuffer appender = KettleLogStore.getAppender();

        String logText = appender.getBuffer(transformation.getLogChannelId(), false).toString();

        JobLogger.log(logText);

//        System.out.println("\n====================\n" + logText + "\n====================\n");

        transformation = null;
        transMeta = null;

        if (result.getNrErrors() == 0) {
            return ReturnT.SUCCESS;
        } else {
            return ReturnT.FAIL;
        }
    }
}
