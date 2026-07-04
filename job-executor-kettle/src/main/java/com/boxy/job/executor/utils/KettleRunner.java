package com.boxy.job.executor.utils;

import com.boxy.job.core.context.JobHelper;
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

    public static void runJobFromFileSystem(String filename) throws Exception {

        JobMeta jobMeta = new JobMeta(filename, null);

        Job job = new Job(null, jobMeta);

        job.setLogLevel(LogLevel.BASIC);

        job.start();

        job.waitUntilFinished();

        Result result = job.getResult();

        JobHelper.log(result.getLogText());

        job = null;
        jobMeta = null;
        if (result.getNrErrors() == 0) {
            JobHelper.handleSuccess();
        } else {
            JobHelper.handleFail();
        }
    }

    public static void runTransformationFromFileSystem(String filename) throws Exception {

        TransMeta transMeta = new TransMeta(filename, (Repository) null);

        Trans transformation = new Trans(transMeta);

        transformation.setLogLevel(LogLevel.BASIC);

        transformation.execute(new String[0]);

        transformation.waitUntilFinished();

        Result result = transformation.getResult();

        LoggingBuffer appender = KettleLogStore.getAppender();

        String logText = appender.getBuffer(transformation.getLogChannelId(), false).toString();

        JobHelper.log(logText);

//        System.out.println("\n====================\n" + logText + "\n====================\n");

        transformation = null;
        transMeta = null;

        if (result.getNrErrors() == 0) {
            JobHelper.handleSuccess();
        } else {
            JobHelper.handleFail();
        }
    }
}
