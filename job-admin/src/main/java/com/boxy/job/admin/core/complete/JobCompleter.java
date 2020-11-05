package com.boxy.job.admin.core.complete;

import com.boxy.job.admin.core.conf.JobAdminConfig;
import com.boxy.job.admin.core.model.JobInfo;
import com.boxy.job.admin.core.model.JobLog;
import com.boxy.job.admin.core.thread.JobTriggerPoolHelper;
import com.boxy.job.admin.core.trigger.TriggerTypeEnum;
import com.boxy.job.admin.core.util.I18nUtil;
import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.handler.IJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * @author xuxueli 2020-10-30 20:43:10
 */
public class JobCompleter {
    private static Logger logger = LoggerFactory.getLogger(JobCompleter.class);

    /**
     * common fresh handle entrance (limit only once)
     *
     * @param jobLog
     * @return
     */
    public static int updateHandleInfoAndFinish(JobLog jobLog) {

        // finish
        finishJob(jobLog);

        // text最大64kb 避免长度过长
        if (jobLog.getHandleMsg().length() > 15000) {
            jobLog.setHandleMsg( jobLog.getHandleMsg().substring(0, 15000) );
        }

        // fresh handle
        return JobAdminConfig.getAdminConfig().getJobLogDao().updateHandleInfo(jobLog);
    }


    /**
     * do somethind to finish job
     */
    private static void finishJob(JobLog jobLog){

        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if (IJobHandler.SUCCESS.getCode() == jobLog.getHandleCode()) {
            JobInfo jobInfo = JobAdminConfig.getAdminConfig().getJobInfoDao().loadById(jobLog.getJobId());
            if (jobInfo!=null && jobInfo.getChildJobId()!=null && jobInfo.getChildJobId().trim().length()>0) {
                triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";

                String[] childJobIds = jobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (childJobIds[i]!=null && childJobIds[i].trim().length()>0 && isNumeric(childJobIds[i]))?Integer.valueOf(childJobIds[i]):-1;
                    if (childJobId > 0) {

                        JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null);
                        ReturnT<String> triggerChildResult = ReturnT.SUCCESS;

                        // add msg
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i],
                                (triggerChildResult.getCode()==ReturnT.SUCCESS_CODE?I18nUtil.getString("system_success"):I18nUtil.getString("system_fail")),
                                triggerChildResult.getMsg());
                    } else {
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i]);
                    }
                }

            }
        }

        if (triggerChildMsg != null) {
            jobLog.setHandleMsg( jobLog.getHandleMsg() + triggerChildMsg );
        }

        // 2、fix_delay trigger next
        // on the way

    }

    private static boolean isNumeric(String str){
        try {
            int result = Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
