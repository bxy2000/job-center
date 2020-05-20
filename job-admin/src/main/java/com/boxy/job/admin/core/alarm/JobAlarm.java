package com.boxy.job.admin.core.alarm;

import com.boxy.job.admin.core.model.JobInfo;
import com.boxy.job.admin.core.model.JobLog;

public interface JobAlarm {
    public boolean doAlarm(JobInfo info, JobLog jobLog);
}
