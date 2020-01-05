package com.boxy.job.core.handler.impl;

import com.boxy.job.core.log.JobLogger;
import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.handler.IJobHandler;

public class GlueJobHandler extends IJobHandler {

	private long glueUpdatetime;
	private IJobHandler jobHandler;
	public GlueJobHandler(IJobHandler jobHandler, long glueUpdatetime) {
		this.jobHandler = jobHandler;
		this.glueUpdatetime = glueUpdatetime;
	}
	public long getGlueUpdatetime() {
		return glueUpdatetime;
	}

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		JobLogger.log("----------- glue.version:"+ glueUpdatetime +" -----------");
		return jobHandler.execute(param);
	}

}
