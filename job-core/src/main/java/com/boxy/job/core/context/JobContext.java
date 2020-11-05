package com.boxy.job.core.context;

public class JobContext {
    /**
     * job id
     */
    private final long jobId;

    /**
     * job log filename
     */
    private final String jobLogFileName;

    /**
     * shard index
     */
    private final int shardIndex;

    /**
     * shard total
     */
    private final int shardTotal;


    public JobContext(long jobId, String jobLogFileName, int shardIndex, int shardTotal) {
        this.jobId = jobId;
        this.jobLogFileName = jobLogFileName;
        this.shardIndex = shardIndex;
        this.shardTotal = shardTotal;
    }

    public long getJobId() {
        return jobId;
    }

    public String getJobLogFileName() {
        return jobLogFileName;
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public int getShardTotal() {
        return shardTotal;
    }


    // ---------------------- tool ----------------------

    private static InheritableThreadLocal<JobContext> contextHolder = new InheritableThreadLocal<JobContext>(); // support for child thread of job handler)
    public static void setJobContext(JobContext jobContext){
        contextHolder.set(jobContext);
    }

    public static JobContext getJobContext(){
        return contextHolder.get();
    }
}
