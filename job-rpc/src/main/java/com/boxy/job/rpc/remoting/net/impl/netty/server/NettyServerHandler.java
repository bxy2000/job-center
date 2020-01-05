package com.boxy.job.rpc.remoting.net.impl.netty.server;

import com.boxy.job.rpc.remoting.provider.RpcProviderFactory;
import com.boxy.job.rpc.util.ThrowableUtil;
import com.boxy.job.rpc.remoting.net.params.Beat;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import com.boxy.job.rpc.remoting.net.params.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private RpcProviderFactory rpcProviderFactory;
    private ThreadPoolExecutor serverHandlerPool;

    public NettyServerHandler(final RpcProviderFactory rpcProviderFactory, final ThreadPoolExecutor serverHandlerPool) {
        this.rpcProviderFactory = rpcProviderFactory;
        this.serverHandlerPool = serverHandlerPool;
    }


    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final RpcRequest rpcRequest) throws Exception {

        // filter beat
        if (Beat.BEAT_ID.equalsIgnoreCase(rpcRequest.getRequestId())){
            logger.debug(" job-rpc provider netty server read beat-ping.");
            return;
        }

        // do invoke
        try {
            serverHandlerPool.execute(new Runnable() {
                @Override
                public void run() {
                    // invoke + response
                    RpcResponse rpcResponse = rpcProviderFactory.invokeService(rpcRequest);

                    ctx.writeAndFlush(rpcResponse);
                }
            });
        } catch (Exception e) {
            // catch error
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setRequestId(rpcRequest.getRequestId());
            rpcResponse.setErrorMsg(ThrowableUtil.toString(e));

            ctx.writeAndFlush(rpcResponse);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error(" job-rpc provider netty server caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            ctx.channel().close();      // beat 3N, close if idle
            logger.debug(" job-rpc provider netty server close an idle channel.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
