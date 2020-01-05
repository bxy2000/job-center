package com.boxy.job.admin.dao;

import com.boxy.job.admin.core.model.JobUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface JobUserDao {

	public List<JobUser> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("username") String username,
                                  @Param("role") int role);
	public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("username") String username,
                             @Param("role") int role);

	public JobUser loadByUserName(@Param("username") String username);

	public int save(JobUser jobUser);

	public int update(JobUser jobUser);
	
	public int delete(@Param("id") int id);

}
