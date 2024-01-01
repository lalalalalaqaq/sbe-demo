package com.orange.sbe.netty.server;

import com.orange.sbe.netty.constants.ConfigConstants;
import com.orange.sbe.netty.codec.SbeMessageDecoder;
import com.orange.sbe.netty.codec.SbeMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

	public static void main(String[] args) throws Exception {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup work = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(boss, work)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.option(ChannelOption.SO_RCVBUF, 32*1024)
				.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
				.childOption(ChannelOption.SO_SNDBUF, 32*1024)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel sc) throws Exception {
						sc.pipeline().addLast(new SbeMessageDecoder());
						sc.pipeline().addLast(new SbeMessageEncoder());
					}
				});
		ChannelFuture cf = b.bind(ConfigConstants.REMOTEIP, ConfigConstants.PORT).sync();
		
		log.info("Netty server start ok :  {}:{}" , ConfigConstants.REMOTEIP , ConfigConstants.PORT);
		//释放连接
		cf.channel().closeFuture().sync();
		work.shutdownGracefully();
		boss.shutdownGracefully();
	}
}















