package com.boxy.job.admin.core.thread;

import com.boxy.job.admin.core.conf.JobAdminConfig;
import com.boxy.job.admin.core.model.JobGroup;
import com.boxy.job.admin.core.model.JobRegistry;
import com.boxy.job.core.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class JobRegistryMonitorHelper {
	private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);

	private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();
	public static JobRegistryMonitorHelper getInstance(){
		return instance;
	}

	private Thread registryThread;
	private volatile boolean toStop = false;
	public void start(){
		registryThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!toStop) {
					try {
						// auto registry group
						List<JobGroup> groupList = JobAdminConfig.getAdminConfig().getJobGroupDao().findByAddressType(0);
						if (groupList!=null && !groupList.isEmpty()) {

							// remove dead address (admin/executor)
							List<Integer> ids = JobAdminConfig.getAdminConfig().getJobRegistryDao().findDead(RegistryConfig.DEAD_TIMEOUT, new Date());
							if (ids!=null && ids.size()>0) {
								JobAdminConfig.getAdminConfig().getJobRegistryDao().removeDead(ids);
							}

							// fresh online address (admin/executor)
							HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
							List<JobRegistry> list = JobAdminConfig.getAdminConfig().getJobRegistryDao().findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
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

							// fresh group address
							for (JobGroup group: groupList) {
								List<String> registryList = appAddressMap.get(group.getAppName());
								String addressListStr = null;
								if (registryList!=null && !registryList.isEmpty()) {
									Collections.sort(registryList);
									addressListStr = "";
									for (String item:registryList) {
										addressListStr += item + ",";
									}
									addressListStr = addressListStr.substring(0, addressListStr.length()-1);
								}
								group.setAddressList(addressListStr);
								JobAdminConfig.getAdminConfig().getJobGroupDao().update(group);
							}
						}
					} catch (Exception e) {
						if (!toStop) {
							logger.error(">>>>>>>>>>> job, job registry monitor thread error:{}", e);
						}
					}
					try {
						TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
					} catch (InterruptedException e) {
						if (!toStop) {
							logger.error(">>>>>>>>>>> job, job registry monitor thread error:{}", e);
						}
					}
				}
				logger.info(">>>>>>>>>>> job, job registry monitor thread stop");
			}
		});
		registryThread.setDaemon(true);
		registryThread.setName("job, admin JobRegistryMonitorHelper");
		registryThread.start();
	}

	public void toStop(){
		toStop = true;
		// interrupt and wait
		registryThread.interrupt();
		try {
			registryThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
