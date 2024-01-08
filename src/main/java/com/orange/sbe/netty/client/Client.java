package com.orange.sbe.netty.client;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.netty.constants.ConfigConstants;
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
		new Client().connect(ConfigConstants.PORT, ConfigConstants.REMOTEIP);
	}


	public void connect(int port, String host) throws Exception {
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) {
						ch.pipeline().addLast(new SbeMessageDecoder());
						ch.pipeline().addLast(new SbeMessageEncoder());
					}
				});
		ChannelFuture future = b.connect(new InetSocketAddress(host, port),
				new InetSocketAddress(ConfigConstants.LOCALIP, ConfigConstants.LOCAL_PORT)).sync();
		Channel c = future.channel();
		System.out.println("success");
		c.writeAndFlush(mockRepeatReq());
		future.channel().closeFuture().sync();

	}

	private static List<WithdrawRequest> mockRepeatReq() {
		WithdrawRequest withdrawRequest1 = new WithdrawRequest(10001L, BigDecimal.valueOf(10.12), Currency.BTC, Market.NASDAQ);
		WithdrawRequest withdrawRequest2 = new WithdrawRequest(20001L, BigDecimal.valueOf(10.13), Currency.CNY, Market.NASDAQ);
		WithdrawRequest withdrawRequest3 = new WithdrawRequest(30001L, BigDecimal.valueOf(10.14), Currency.USD, Market.NASDAQ);
		return List.of(withdrawRequest1,withdrawRequest2,withdrawRequest3);
	}

}