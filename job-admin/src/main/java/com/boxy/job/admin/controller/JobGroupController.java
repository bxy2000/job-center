package com.boxy.job.admin.controller;

import com.boxy.job.admin.core.model.JobGroup;
import com.boxy.job.admin.core.model.JobRegistry;
import com.boxy.job.admin.core.util.I18nUtil;
import com.boxy.job.admin.dao.JobGroupDao;
import com.boxy.job.admin.dao.JobInfoDao;
import com.boxy.job.admin.dao.JobRegistryDao;
import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.enums.RegistryConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

	@Resource
	public JobInfoDao jobInfoDao;
	@Resource
	public JobGroupDao jobGroupDao;
	@Resource
	private JobRegistryDao jobRegistryDao;

	@RequestMapping
	public String index(Model model) {

		// job group (executor)
		List<JobGroup> list = jobGroupDao.findAll();

		model.addAttribute("list", list);
		return "jobgroup/jobgroup.index";
	}

	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(JobGroup jobGroup){

		// valid
		if (jobGroup.getAppName()==null || jobGroup.getAppName().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input")+"AppName") );
		}
		if (jobGroup.getAppName().length()<4 || jobGroup.getAppName().length()>64) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appName_length") );
		}
		if (jobGroup.getTitle()==null || jobGroup.getTitle().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (jobGroup.getAddressType()!=0) {
			if (jobGroup.getAddressList()==null || jobGroup.getAddressList().trim().length()==0) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit") );
			}
			String[] addresss = jobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (item==null || item.trim().length()==0) {
					return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid") );
				}
			}
		}

		int ret = jobGroupDao.save(jobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(JobGroup jobGroup){
		// valid
		if (jobGroup.getAppName()==null || jobGroup.getAppName().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input")+"AppName") );
		}
		if (jobGroup.getAppName().length()<4 || jobGroup.getAppName().length()>64) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appName_length") );
		}
		if (jobGroup.getTitle()==null || jobGroup.getTitle().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (jobGroup.getAddressType() == 0) {
			// 0=自动注册
			List<String> registryList = findRegistryByAppName(jobGroup.getAppName());
			String addressListStr = null;
			if (registryList!=null && !registryList.isEmpty()) {
				Collections.sort(registryList);
				addressListStr = "";
				for (String item:registryList) {
					addressListStr += item + ",";
				}
				addressListStr = addressListStr.substring(0, addressListStr.length()-1);
			}
			jobGroup.setAddressList(addressListStr);
		} else {
			// 1=手动录入
			if (jobGroup.getAddressList()==null || jobGroup.getAddressList().trim().length()==0) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit") );
			}
			String[] addresss = jobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (item==null || item.trim().length()==0) {
					return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid") );
				}
			}
		}

		int ret = jobGroupDao.update(jobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	private List<String> findRegistryByAppName(String appNameParam){
		HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
		List<JobRegistry> list = jobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
		if (list != null) {
			for (JobRegistry item: list) {
				if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
					String appName = item.getRegistryKey();
					List<String> registryList = appAddressMap.get(appName);
					if (registryList == null) {
						registryList = new ArrayList<String>();
					}

					if (!registryList.contains(item.getRegistryValue())) {
						registryList.add(item.getRegistryValue());
					}
					appAddressMap.put(appName, registryList);
				}
			}
		}
		return appAddressMap.get(appNameParam);
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id){

		// valid
		int count = jobInfoDao.pageListCount(0, 10, id, -1,  null, null, null);
		if (count > 0) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_0") );
		}

		List<JobGroup> allList = jobGroupDao.findAll();
		if (allList.size() == 1) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_1") );
		}

		int ret = jobGroupDao.remove(id);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/loadById")
	@ResponseBody
	public ReturnT<JobGroup> loadById(int id){
		JobGroup jobGroup = jobGroupDao.load(id);
		return jobGroup!=null?new ReturnT<JobGroup>(jobGroup):new ReturnT<JobGroup>(ReturnT.FAIL_CODE, null);
	}

}
