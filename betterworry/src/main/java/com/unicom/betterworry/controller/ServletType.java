package com.unicom.betterworry.controller;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class ServletType
 */
public class ServletType extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletType() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void init() throws ServletException {  
        super.init();  
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());  
        AutowireCapableBeanFactory factory = wac.getAutowireCapableBeanFactory();  
        factory.autowireBean(this);  
    }  

}
