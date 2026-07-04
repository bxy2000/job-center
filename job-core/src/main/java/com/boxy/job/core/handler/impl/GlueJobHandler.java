package com.boxy.job.core.handler.impl;

import com.boxy.job.core.context.JobHelper;
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
	public void execute() throws Exception {
		JobHelper.log("----------- glue.version:"+ glueUpdatetime +" -----------");
		jobHandler.execute();
	}

}
