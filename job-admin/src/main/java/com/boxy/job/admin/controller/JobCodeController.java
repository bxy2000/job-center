package com.boxy.job.admin.controller;

import com.boxy.job.admin.core.model.JobInfo;
import com.boxy.job.admin.core.model.JobLogGlue;
import com.boxy.job.admin.core.util.I18nUtil;
import com.boxy.job.admin.dao.JobInfoDao;
import com.boxy.job.admin.dao.JobLogGlueDao;
import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.glue.GlueTypeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/jobcode")
public class JobCodeController {
	
	@Resource
	private JobInfoDao jobInfoDao;
	@Resource
	private JobLogGlueDao jobLogGlueDao;

	@RequestMapping
	public String index(HttpServletRequest request, Model model, int jobId) {
		JobInfo jobInfo = jobInfoDao.loadById(jobId);
		List<JobLogGlue> jobLogGlues = jobLogGlueDao.findByJobId(jobId);

		if (jobInfo == null) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		if (GlueTypeEnum.BEAN == GlueTypeEnum.match(jobInfo.getGlueType())) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_gluetype_unvalid"));
		}

		// valid permission
		JobInfoController.validPermission(request, jobInfo.getJobGroup());

		// Glue类型-字典
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());

		model.addAttribute("jobInfo", jobInfo);
		model.addAttribute("jobLogGlues", jobLogGlues);
		return "jobcode/jobcode.index";
	}
	
	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(Model model, int id, String glueSource, String glueRemark) {
		// valid
		if (glueRemark==null) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_glue_remark")) );
		}
		if (glueRemark.length()<4 || glueRemark.length()>100) {
			return new ReturnT<String>(500, I18nUtil.getString("jobinfo_glue_remark_limit"));
		}
		JobInfo exists_jobInfo = jobInfoDao.loadById(id);
		if (exists_jobInfo == null) {
			return new ReturnT<String>(500, I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		
		// update new code
		exists_jobInfo.setGlueSource(glueSource);
		exists_jobInfo.setGlueRemark(glueRemark);
		exists_jobInfo.setGlueUpdatetime(new Date());

		exists_jobInfo.setUpdateTime(new Date());
		jobInfoDao.update(exists_jobInfo);

		// log old code
		JobLogGlue jobLogGlue = new JobLogGlue();
		jobLogGlue.setJobId(exists_jobInfo.getId());
		jobLogGlue.setGlueType(exists_jobInfo.getGlueType());
		jobLogGlue.setGlueSource(glueSource);
		jobLogGlue.setGlueRemark(glueRemark);

		jobLogGlue.setAddTime(new Date());
		jobLogGlue.setUpdateTime(new Date());
		jobLogGlueDao.save(jobLogGlue);

		// remove code backup more than 30
		jobLogGlueDao.removeOld(exists_jobInfo.getId(), 30);

		return ReturnT.SUCCESS;
	}
	
}
