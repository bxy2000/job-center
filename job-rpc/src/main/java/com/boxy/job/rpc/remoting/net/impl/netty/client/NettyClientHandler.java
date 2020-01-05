package com.boxy.job.rpc.remoting.net.impl.netty.client;

import com.boxy.job.rpc.remoting.invoker.RpcInvokerFactory;
import com.boxy.job.rpc.remoting.net.params.Beat;
import com.boxy.job.rpc.remoting.net.params.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
	private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);


	private RpcInvokerFactory rpcInvokerFactory;
	private NettyConnectClient nettyConnectClient;
	public NettyClientHandler(final RpcInvokerFactory rpcInvokerFactory, NettyConnectClient nettyConnectClient) {
		this.rpcInvokerFactory = rpcInvokerFactory;
		this.nettyConnectClient = nettyConnectClient;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {

		// notify response
		rpcInvokerFactory.notifyInvokerFuture(rpcResponse.getRequestId(), rpcResponse);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(" job-rpc netty client caught exception", cause);
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent){
			/*ctx.channel().close();      // close idle channel
			logger.debug(" job-rpc netty client close an idle channel.");*/

			nettyConnectClient.send(Beat.BEAT_PING);	// beat N, close if fail(may throw error)
			logger.debug(" job-rpc netty client send beat-ping.");

		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
