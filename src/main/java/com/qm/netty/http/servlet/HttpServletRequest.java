package com.qm.netty.http.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.qm.netty.server.HttpHeader;
import com.qm.netty.util.FileUtis;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

/**
 * 解析fullHttpRequest请求
 * @author qiumin
 *
 */
public class HttpServletRequest {
	
	private static final HttpMethod GET = HttpMethod.GET;
	private static final HttpMethod POST = HttpMethod.POST;
	private static final HttpMethod DELETE = HttpMethod.DELETE;
	private static final HttpMethod HEAD = HttpMethod.HEAD;
	private static final String PREFIX = FileUtis.getRootPath()+"/webapp";
	private static final String SUFFIX = ".html";
	
	private FullHttpRequest req;
	//存放前台传过来的参数，如get/post方式发来的数据
	private Map<String, String> parameterMap = new HashMap<String, String>();
	//存放用户在后台存入该类的数据
	private Map<String, Object> paramMap = new HashMap<String, Object>();
	//存放所有的请求头及其对应的值
	private Map<String, String> headerMap = new ConcurrentHashMap<String, String>();
	
	public HttpServletRequest(FullHttpRequest req) {
		this.req = req;
		try {
			Map<String, String> map = this.parse();
			//初始化
			parameterMap.putAll(map);
			setHeaderToMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getUrl(){
		return req.uri();
	}
	public Map<String, Object> getParamMap() {
		return paramMap;
	}
	
	/**
	 * 解析http请求
	 * @return
	 * @throws IOException
	 */
	public Map<String,String> parse() throws IOException{
		HttpMethod method = req.method();
		Map<String, String> paramMap = new HashMap<String, String>();
		if(method == GET) {//说明是get请求
			QueryStringDecoder decoder = new QueryStringDecoder(req.uri());//netty提供的，可以对url进行编码和解析
			Map<String, List<String>> map = decoder.parameters();//得到的结果是?后面的内容
			for(Entry<String, List<String>> e:map.entrySet()) {
				String key = e.getKey();
				List<String> value = e.getValue();
				for(String val:value) {
					paramMap.put(key, val);
				}
			}
		}else if(method == POST){
			HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(req);//netty提供的post方式解码
			List<InterfaceHttpData> datas = postDecoder.getBodyHttpDatas();
			for(InterfaceHttpData data:datas) {
				Attribute attr = (Attribute) data;
				paramMap.put(attr.getName(), attr.getValue());
			}
		}else if(method == HEAD) {
			
		}else if(method == DELETE) {
			
		}
		return paramMap;
	}
	
	public void setAttribute(String name,Object value) {
		paramMap.put(name, value);
	}
	
	public Object getAttribute(String name) {
		return paramMap.get(name);
	}
	public boolean removeAttribute(String name) {
		if(paramMap.containsKey(name)) {
			paramMap.remove(name);
			return true;
		}
		return false;
	}
	public String getParameter(String name) {
		return parameterMap.get(name);
	}
	
	public void clear() {
		parameterMap.clear();
	}
	
	
	/**
	 * 接受前台传来的cookie，但cookie中若存在中文，
	 * 目前不知道怎么解决，能用的方法都试过了，
	 * 乱码是这个：￧ﾔﾷ￦ﾀﾧ，丛来都没见过这个乱码
	 * @return
	 */
	public Cookie[] getCookie() {
		String cookie = headerMap.get(HttpHeader.COOKIE);
		if(cookie == null) {
			return null;
		}
		Set<Cookie> cookieSet = ServerCookieDecoder.LAX.decode(cookie);
		Cookie[] cookies = new DefaultCookie[cookieSet.size()];
		cookieSet.toArray(cookies);
		return cookies;
	}
	
	
	private void setHeaderToMap() {
		HttpHeaders headers = req.headers();
		List<Entry<String,String>> list = headers.entries();
		for(Entry<String, String> s:list) {
			String key = s.getKey();
			String value = s.getValue();
			headerMap.put(key, value);
		}
	}
	
	
	public Map<String, String> getHeaders(){
		return headerMap;
	}
	
	public RequestDispatcher getRequestDispatcher(String path){
		return new RequestDispatcher(path);
	}
	/**
	 * 将其作为一个内部类
	 * @author qiumin
	 *
	 */
	public static class RequestDispatcher{
		String path;

		public RequestDispatcher(String path) {
			super();
			this.path = path;
		}
		
		@SuppressWarnings("unused")
		public void forward(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException{
			if(path == null || !path.endsWith(SUFFIX)){
				response.sendError(HttpResponseStatus.NOT_FOUND, " the path {"+path+"} is error,you have to contain .html string int you param path");
				return;
			}
			FileUtis.setData(request.getParamMap());
			String content = FileUtis.getContent(path);
			if("".equals(content)){
				response.sendError(HttpResponseStatus.NOT_FOUND, " could not found the "+path);
				return;
			}
			response.write(content);
		}
	}
}
