package com.boxy.job.admin.dao;

import com.boxy.job.admin.core.model.JobGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobGroupDao {

    public List<JobGroup> findAll();

    public List<JobGroup> findByAddressType(@Param("addressType") int addressType);

    public int save(JobGroup jobGroup);

    public int update(JobGroup jobGroup);

    public int remove(@Param("id") int id);

    public JobGroup load(@Param("id") int id);
}
