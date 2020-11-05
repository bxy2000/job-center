package com.boxy.job.util;

import com.boxy.job.core.context.JobContext;
import com.boxy.job.core.util.ScriptUtil;
import org.junit.Test;

import java.io.IOException;

public class ScriptUtilTest {
    @Test
    public void testExecToFile() throws IOException {
        JobContext.setJobContext(new JobContext(
                -1,
                "c:\\ls\\test1.log",
                -1, -1));
        ScriptUtil.execToFile("powershell", "c:\\data\\applogs\\job\\jobhandler\\gluesource\\4_1591689834000.ps1",
                "c:\\ls\\test.log");
    }
}
