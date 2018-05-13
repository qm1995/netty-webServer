package com.qm.netty.server;

import java.io.IOException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * http服务器，这个是http服务器启动类
 * @author DBC
 *
 */
public class HttpServer { 
	
	public void start(final int port) {
		EventLoopGroup boosGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();//实际工作线程
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(boosGroup, workGroup);
			
			serverBootstrap.channel(NioServerSocketChannel.class)
							.childHandler(new ChannelInitializer<SocketChannel>() {
								@Override
								protected void initChannel(SocketChannel ch) throws Exception {
									// TODO Auto-generated method stub
									ChannelPipeline cp = ch.pipeline();
									cp.addLast("http-decoder",new HttpServerCodec());
									cp.addLast("http-aggregator",new HttpObjectAggregator(65536));
									cp.addLast("http-encoder",new HttpRequestEncoder());
									cp.addLast("http-chunked",new ChunkedWriteHandler());
									cp.addLast("httpServerHandler",new HttpServerHandler());
								}
							});
			
			ChannelFuture future = serverBootstrap.bind("127.0.0.1", port).sync();
			System.out.println("文件服务器启动-------网止：127.0.0.1，端口："+port);
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			boosGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws IOException {
		HttpServer server = new HttpServer();
		server.start(8080);
	}
}
