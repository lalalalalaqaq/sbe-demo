package com.orange.sbe.netty.client;

import java.math.BigDecimal;
import java.net.InetSocketAddress;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.netty.Constants;
import com.orange.sbe.netty.codec.SbeMessageDecoder;
import com.orange.sbe.netty.codec.SbeMessageEncoder;

import com.orange.sbe.stub.Currency;
import com.orange.sbe.stub.Market;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

	EventLoopGroup group = new NioEventLoopGroup();

	public static void main(String[] args) throws Exception {
		new Client().connect(Constants.PORT, Constants.REMOTEIP);
	}


	public void connect(int port, String host) throws Exception {
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new SbeMessageDecoder());
						ch.pipeline().addLast(new SbeMessageEncoder());
					}
				});
		ChannelFuture future = b.connect(new InetSocketAddress(host, port),
				new InetSocketAddress(Constants.LOCALIP, Constants.LOCAL_PORT)).sync();
		Channel c = future.channel();
		c.writeAndFlush(new WithdrawRequest(10001, BigDecimal.valueOf(10.12), Currency.BTC, Market.NASDAQ));
		future.channel().closeFuture().sync();

	}

}