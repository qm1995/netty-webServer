package com.qm.netty.demo;

import com.qm.netty.http.servlet.HttpServlet;
import com.qm.netty.http.servlet.HttpServletRequest;
import com.qm.netty.http.servlet.HttpServletResponse;

public class UserServlet extends HttpServlet {

	@Override
	public void doSevice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		String url = request.getUrl();
		if("/".equals(url)){
			request.getRequestDispatcher("/index.html").forward(request, response);
		}else{
			String name = request.getParameter("username");
			User u = new User("张三", 23);
			System.out.println("从前台传过来的参数是："+name);
			request.setAttribute("user", u);
			request.setAttribute("username", name);
			request.getRequestDispatcher("/2.html").forward(request, response);
		}
	}

}
