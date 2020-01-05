package com.boxy.job.rpc.remoting.net.impl.netty.client;

import com.boxy.job.rpc.remoting.invoker.RpcInvokerFactory;
import com.boxy.job.rpc.remoting.net.impl.netty.codec.NettyDecoder;
import com.boxy.job.rpc.remoting.net.impl.netty.codec.NettyEncoder;
import com.boxy.job.rpc.serialize.Serializer;
import com.boxy.job.rpc.util.IpUtil;
import com.boxy.job.rpc.remoting.net.common.ConnectClient;
import com.boxy.job.rpc.remoting.net.params.Beat;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import com.boxy.job.rpc.remoting.net.params.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

public class NettyConnectClient extends ConnectClient {


    private EventLoopGroup group;
    private Channel channel;


    @Override
    public void init(String address, final Serializer serializer, final RpcInvokerFactory rpcInvokerFactory) throws Exception {
        final NettyConnectClient thisClient = this;

        Object[] array = IpUtil.parseIpPort(address);
        String host = (String) array[0];
        int port = (int) array[1];


        this.group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0,0,Beat.BEAT_INTERVAL, TimeUnit.SECONDS))    // beat N, close if fail
                                .addLast(new NettyEncoder(RpcRequest.class, serializer))
                                .addLast(new NettyDecoder(RpcResponse.class, serializer))
                                .addLast(new NettyClientHandler(rpcInvokerFactory, thisClient));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        this.channel = bootstrap.connect(host, port).sync().channel();

        // valid
        if (!isValidate()) {
            close();
            return;
        }

        logger.debug(" job-rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);
    }


    @Override
    public boolean isValidate() {
        if (this.channel != null) {
            return this.channel.isActive();
        }
        return false;
    }

    @Override
    public void close() {
        if (this.channel != null && this.channel.isActive()) {
            this.channel.close();        // if this.channel.isOpen()
        }
        if (this.group != null && !this.group.isShutdown()) {
            this.group.shutdownGracefully();
        }
        logger.debug(" job-rpc netty client close.");
    }


    @Override
    public void send(RpcRequest rpcRequest) throws Exception {
        this.channel.writeAndFlush(rpcRequest).sync();
    }
}
