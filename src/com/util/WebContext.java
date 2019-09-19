package com.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 请求参数的中转站
 */
public class WebContext {

    public static ThreadLocal<HttpServletRequest> requestThreadLocal=new ThreadLocal<>();
    public static ThreadLocal<HttpServletResponse> responseThreadLocal=new ThreadLocal<>();

    public HttpServletRequest getRequest(){
        return requestThreadLocal.get();
    }

    public HttpSession getSession(){
        return requestThreadLocal.get().getSession();
    }

    public ServletContext getServletContext(){
        return requestThreadLocal.get().getServletContext();
    }

    public HttpServletResponse getResponse(){
        return responseThreadLocal.get();
    }

}
