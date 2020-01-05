package com.boxy.job.rpc.remoting.net;

import com.boxy.job.rpc.remoting.invoker.reference.RpcReferenceBean;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Client {
	protected static final Logger logger = LoggerFactory.getLogger(Client.class);
	// ---------------------- init ----------------------
	protected volatile RpcReferenceBean rpcReferenceBean;

	public void init(RpcReferenceBean rpcReferenceBean) {
		this.rpcReferenceBean = rpcReferenceBean;
	}

    // ---------------------- send ----------------------
	public abstract void asyncSend(String address, RpcRequest rpcRequest) throws Exception;

}
