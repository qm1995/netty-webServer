package com.qm.netty.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.qm.netty.http.servlet.HttpServlet;
import com.qm.netty.http.servlet.HttpServletRequest;
import com.qm.netty.http.servlet.HttpServletResponse;
import com.qm.netty.util.Config;
import com.qm.netty.util.FileUtis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object>{
	
	private static final HttpResponseStatus BAD_REQUEST = HttpResponseStatus.BAD_REQUEST;
	private static final HttpResponseStatus FROBIDDEN = HttpResponseStatus.FORBIDDEN;
	private static final HttpResponseStatus NOT_FOUND = HttpResponseStatus.NOT_FOUND;
	private static final String CONTENT_LENGTH = HttpHeader.CONTENT_LENGTH;
	private static final String CONTENT_TYPE = HttpHeader.CONTENT_TYPE;
	private static final HttpVersion HTTP_1_1 = HttpVersion.HTTP_1_1;
	private static final HttpResponseStatus OK = HttpResponseStatus.OK;
	@SuppressWarnings("unused")
	private static final String CONNECTION = HttpHeader.CONNECTION;
	private static final String DEFAULT_DIR = "\\src\\test\\java\\com\\qm\\netty\\http\\test/";
	private static final String DEFAULT_URL = "index";
	@SuppressWarnings("unused")
	private static final String PREFIX = DEFAULT_DIR;
	private static final String SUFFIX = ".html";
	
	private HttpServlet httpServlet;
	
	private FullHttpRequest request;
	private FullHttpResponse response;
	@SuppressWarnings("unused")
	private Map<String, String> dataMap = new HashMap<String, String>();
	
	public HttpServerHandler() {
		super();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object obj){
		// TODO Auto-generated method stub
			if(obj instanceof FullHttpRequest) {
				FullHttpRequest request = (FullHttpRequest) obj;
				this.request = request;
				if("/favicon.ico".equals(request.uri())) {
					return;
				}
				response = new DefaultFullHttpResponse(HTTP_1_1,OK);
				response.headers().set(CONTENT_TYPE,"text/html;charset=UTF-8");
				if(request.decoderResult().isFailure()) {
					sendError(ctx,response,BAD_REQUEST);
				}
				String path = request.uri();
				
				HttpServletRequest req = new HttpServletRequest(request);
				HttpServletResponse res = new HttpServletResponse(response);
				try {
					path = "/".equals(path)?path+DEFAULT_URL+SUFFIX:path;
					if(!path.endsWith(SUFFIX)){
						res.sendError(HttpResponseStatus.NOT_FOUND, " the path {"+path+"} is error,you have to contain .html string int you param path");
						return;
					}
					String className = Config.getInstances().getString(path);
					if(className == null){
						String content = FileUtis.getContent(path);
						if("".equals(content)){
							res.sendError(HttpResponseStatus.NOT_FOUND, " could not found the "+path);
						}else{
							response.content().writeBytes(content.getBytes());
						}
						return;
					}
					Class<?> clazz = Class.forName(className);
					if(clazz.getSuperclass() == HttpServlet.class){
						httpServlet = (HttpServlet) clazz.newInstance();
						httpServlet.doSevice(req, res);
					}else{
						res.sendError(HttpResponseStatus.NOT_FOUND, " could not found the class name for "+className);
					}
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					res.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.toString());
					return;
					
				}finally{
					setContentLength(response);
					ctx.writeAndFlush(response);
				}
				
				
			}
	}

	private void setContentLength(FullHttpResponse response) {
		// TODO Auto-generated method stub
		response.headers().set(CONTENT_LENGTH,response.content().readableBytes());
	}


	private void sendError(ChannelHandlerContext ctx,FullHttpResponse response, HttpResponseStatus status) {
		// TODO Auto-generated method stub
		response.setStatus(status);
		String errorMsg = "<body style=\"background-color: gray;\"><div style=\"text-align: center;margin-top: 200px;font-size: 40px;\\\">"+status.toString()+"</div></body>";
		response.content().writeBytes(errorMsg.getBytes(Charset.forName("UTF-8")));
		ctx.writeAndFlush(response);
	}

	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		setContentLength(response);
		super.channelReadComplete(ctx);
	}

	private String sanitizeUri(String uri){
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new Error("转码出错");
			}
		}
		uri.replace("/", File.separatorChar+"");
		if(uri.contains(File.separator + ".") || uri.contains("."+File.separator)
				|| uri.startsWith(".")
				|| uri.endsWith(".")) {
			return null;
		}
		return System.getProperty("user.dir")+uri;
	}
	
	/**
	 * 负责html的转发
	 * @param response
	 * @param url
	 */
	private void sendWeb(FullHttpResponse response,String url) {
		Map<String, String> content = FileUtis.getFileContent();
		String fileContent = "";
		HttpServletRequest parese = new HttpServletRequest(request);
		parese.getCookie();
		Map<String, Object> paramMap = parese.getParamMap();
		FileUtis.setData(paramMap);
		if("/".equals(url)) {
			url = DEFAULT_URL+SUFFIX;
			for(Entry<String, String> e:content.entrySet()) {
				if(e.getKey().endsWith(url)) {
					fileContent = e.getValue();
					fileContent = FileUtis.getResolveContent(fileContent);
					break;
				}
			}
		}else if(url.endsWith(SUFFIX)){
			url = url.substring(1);
			for(Entry<String, String> e:content.entrySet()) {
				if(e.getKey().endsWith(url)) {
					fileContent = e.getValue();
					fileContent = FileUtis.getResolveContent(fileContent);
					break;
				}
			}
		}
		response.content().writeBytes(fileContent.getBytes());
		setContentLength(response);
	}
	
	
	/**
	 * 负责浏览本工程下的所有可读文件和目录
	 * @param response
	 * @param url
	 * @param ctx
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	private void viewLocalFile(FullHttpResponse response,String url,ChannelHandlerContext ctx) throws Exception {
		String path = sanitizeUri(url);
		if(path == null) {
			sendError(ctx,response,FROBIDDEN);
			return;
		}
		
		File file = new File(path);
		if(file.isHidden() || !file.exists()) {
			sendError(ctx,response,NOT_FOUND);
		}
		if(!file.isFile()) {
			sendListing(response,file);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		}catch (FileNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
			sendError(ctx,response,NOT_FOUND);
			return;
		}
		long fileLength = randomAccessFile.length();
		response.content().writeBytes(new ChunkedFile(randomAccessFile, 0, fileLength, 8192).readChunk(ctx));
		setContentLength(response);
		ctx.writeAndFlush(response);
	}
	
	
	private void sendListing(FullHttpResponse response,File dir) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul><li>链接：<a href=\"..\">..</a><li><br/>");
		for(File f:dir.listFiles()) {
			String p = f.getAbsolutePath().split("spiderDemo")[1];
			sb.append("<li>链接：<a href="+p+">"+f.getName()+"</a><li><br/>");
		}
		sb.append("</ul>");
		ByteBuf buffer = Unpooled.copiedBuffer(sb,CharsetUtil.UTF_8);
		
		response.content().writeBytes(buffer);
	}
	
	public static String getRootDir() {
		return System.getProperty("user.dir");
	}
	
	
}
