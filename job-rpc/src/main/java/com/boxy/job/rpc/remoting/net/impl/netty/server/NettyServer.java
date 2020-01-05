package com.boxy.job.rpc.remoting.net.impl.netty.server;

import com.boxy.job.rpc.remoting.net.impl.netty.codec.NettyDecoder;
import com.boxy.job.rpc.remoting.net.impl.netty.codec.NettyEncoder;
import com.boxy.job.rpc.remoting.provider.RpcProviderFactory;
import com.boxy.job.rpc.util.ThreadPoolUtil;
import com.boxy.job.rpc.remoting.net.Server;
import com.boxy.job.rpc.remoting.net.params.Beat;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import com.boxy.job.rpc.remoting.net.params.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyServer extends Server {

    private Thread thread;

    @Override
    public void start(final RpcProviderFactory rpcProviderFactory) throws Exception {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // param
                final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                        NettyServer.class.getSimpleName(),
                        rpcProviderFactory.getCorePoolSize(),
                        rpcProviderFactory.getMaxPoolSize());
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    // start server
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL*3, TimeUnit.SECONDS))     // beat 3N, close if idle
                                            .addLast(new NettyDecoder(RpcRequest.class, rpcProviderFactory.getSerializerInstance()))
                                            .addLast(new NettyEncoder(RpcResponse.class, rpcProviderFactory.getSerializerInstance()))
                                            .addLast(new NettyServerHandler(rpcProviderFactory, serverHandlerPool));
                                }
                            })
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    // bind
                    ChannelFuture future = bootstrap.bind(rpcProviderFactory.getPort()).sync();

                    logger.info(" job-rpc remoting server start success, nettype = {}, port = {}", NettyServer.class.getName(), rpcProviderFactory.getPort());
                    onStarted();

                    // wait util stop
                    future.channel().closeFuture().sync();

                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.info(" job-rpc remoting server stop.");
                    } else {
                        logger.error(" job-rpc remoting server error.", e);
                    }
                } finally {

                    // stop
                    try {
                        serverHandlerPool.shutdown();    // shutdownNow
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                }
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void stop() throws Exception {

        // destroy server thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        // on stop
        onStoped();
        logger.info(" job-rpc remoting server destroy success.");
    }

}
