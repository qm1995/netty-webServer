package com.qm.netty.http.servlet;

import java.io.IOException;
import java.nio.charset.Charset;

import com.qm.netty.server.HttpHeader;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;

public class HttpServletResponse {
	
	private static final String CONTENT_TYPE = HttpHeader.CONTENT_TYPE;
	
	private static final String DEFAULT_ENCODER = "UTF-8";
	private static final String SET_COOKIE = HttpHeader.SET_COOKIE;
	private FullHttpResponse response;
	private String characterEncoding = "UTF-8";
	
	public HttpServletResponse(FullHttpResponse response) {
		super();
		this.response = response;
		setContentType("text/html;charset=UTF-8");
	}
	
	
	public String getCharacterEncoding() {
		return characterEncoding;
	}


	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}


	public void setHeader(String name,Object value) {
		response.headers().set(name, value);
	}
	
	public void sendError(HttpResponseStatus status,String msg) {
		String errorMsg = "<body style=\"background-color: gray;\"><div style=\"text-align: center;margin-top: 200px;font-size: 40px;\\\">"+status.toString()+msg+"</div></body>";
		response.content().writeBytes(errorMsg.getBytes(Charset.forName(DEFAULT_ENCODER)));
	}
	
	public void sendRedirect(String location) throws IOException{
		response.setStatus(HttpResponseStatus.FOUND);
		setHeader(HttpHeader.LOCATION, location);
	}
	
	public void addCookie(Cookie cookie) {
		String encode = ClientCookieEncoder.LAX.encode(cookie);
		response.headers().add(SET_COOKIE, encode);
		return;
	}
	public void setContentType(String type) {
		setHeader(CONTENT_TYPE,type);
	}
	
	public void write(String s){
		s = s == null?"":s;
		write(s.getBytes());
	}
	/*public PrintWriter getWriter() throws UnsupportedEncodingException{
		if(out == null){
			Writer write = (characterEncoding != null ? new OutputStreamWriter(content, characterEncoding) : new OutputStreamWriter(content));
			out = new PrintWriter(write);
		}
		return out;
	}
	private byte[] getContentByToArray(){
		return content.toByteArray();
	}
	
	public void add(){
		response.content().writeBytes(getContentByToArray());
	}*/


	public void write(byte[] bytes) {
		// TODO Auto-generated method stub
		response.content().writeBytes(bytes);
	}
}
