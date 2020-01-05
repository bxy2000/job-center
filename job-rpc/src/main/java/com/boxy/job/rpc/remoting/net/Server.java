package com.boxy.job.rpc.remoting.net;

import com.boxy.job.rpc.remoting.net.params.BaseCallback;
import com.boxy.job.rpc.remoting.provider.RpcProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Server {
	protected static final Logger logger = LoggerFactory.getLogger(Server.class);

	private BaseCallback startedCallback;
	private BaseCallback stopedCallback;

	public void setStartedCallback(BaseCallback startedCallback) {
		this.startedCallback = startedCallback;
	}

	public void setStopedCallback(BaseCallback stopedCallback) {
		this.stopedCallback = stopedCallback;
	}

	public abstract void start(final RpcProviderFactory rpcProviderFactory) throws Exception;

	public void onStarted() {
		if (startedCallback != null) {
			try {
				startedCallback.run();
			} catch (Exception e) {
				logger.error("job-rpc, server startedCallback error.", e);
			}
		}
	}

	public abstract void stop() throws Exception;

	public void onStoped() {
		if (stopedCallback != null) {
			try {
				stopedCallback.run();
			} catch (Exception e) {
				logger.error("job-rpc, server stopedCallback error.", e);
			}
		}
	}
}
