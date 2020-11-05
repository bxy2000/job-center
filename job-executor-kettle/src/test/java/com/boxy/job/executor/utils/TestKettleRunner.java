package com.boxy.job.executor.utils;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class TestKettleRunner {
    @Test
    public void testJob() throws Exception {
        KettleRunner.runJobFromFileSystem("C:\\kettle\\time\\timer.kjb");
    }

    @Test
    public void testTrans() throws Exception {
        KettleRunner.runTransformationFromFileSystem("C:\\kettle\\time\\synchronize-after-merge.ktr");
    }
}
