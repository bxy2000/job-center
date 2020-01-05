package com.boxy.job.rpc.remoting.net.impl.netty_http.client;

import com.boxy.job.rpc.remoting.net.Client;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import com.boxy.job.rpc.remoting.net.common.ConnectClient;

public class NettyHttpClient extends Client {

    private Class<? extends ConnectClient> connectClientImpl = NettyHttpConnectClient.class;

    @Override
    public void asyncSend(String address, RpcRequest rpcRequest) throws Exception {
        ConnectClient.asyncSend(rpcRequest, address, connectClientImpl, rpcReferenceBean);
    }

}
