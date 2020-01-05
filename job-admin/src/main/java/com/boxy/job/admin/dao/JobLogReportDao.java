package com.boxy.job.admin.dao;

import com.boxy.job.admin.core.model.JobLogReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface JobLogReportDao {

	public int save(JobLogReport jobLogReport);

	public int update(JobLogReport jobLogReport);

	public List<JobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
                                             @Param("triggerDayTo") Date triggerDayTo);

	public JobLogReport queryLogReportTotal();

}
