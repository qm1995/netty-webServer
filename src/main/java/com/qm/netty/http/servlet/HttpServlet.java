package com.qm.netty.http.servlet;

public abstract class HttpServlet {
	
	
	public abstract void doSevice(HttpServletRequest request,HttpServletResponse response) throws Exception;
}
