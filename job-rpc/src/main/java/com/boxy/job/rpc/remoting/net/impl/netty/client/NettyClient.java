package com.boxy.job.rpc.remoting.net.impl.netty.client;

import com.boxy.job.rpc.remoting.net.Client;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import com.boxy.job.rpc.remoting.net.common.ConnectClient;

public class NettyClient extends Client {

	private Class<? extends ConnectClient> connectClientImpl = NettyConnectClient.class;

	@Override
	public void asyncSend(String address, RpcRequest rpcRequest) throws Exception {
		ConnectClient.asyncSend(rpcRequest, address, connectClientImpl, rpcReferenceBean);
	}
}
