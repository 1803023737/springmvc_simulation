package com.handler;

import baseknowledge.ScanClassTest;
import com.annotation.Controller;
import com.annotation.RequestMapping;
import com.util.DispatchActionConstant;
import com.util.RequestMappingMap;
import com.util.View;
import com.util.WebContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class AnnotationHandleServlet extends javax.servlet.http.HttpServlet {

    private String parseUri(HttpServletRequest req) {
        //String path = req.getContextPath() + "/";
        //System.out.println("path:"+path);
        String requestURI = req.getRequestURI();
        System.out.println("requestURI:"+requestURI);
        //String midUrl = requestURI.replaceFirst(path, requestURI);
        //System.out.println("midUrl:"+midUrl);
        String lastUrl = requestURI.substring(0, requestURI.lastIndexOf("."));
        return lastUrl;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.excute(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.excute(req, resp);
    }

    private void excute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获得请求的地址   去掉项目根路径+.do
        String parseUri = parseUri(request);
        //req res 封装
        WebContext.requestThreadLocal.set(request);
        WebContext.responseThreadLocal.set(response);
        //从map中获得class
        System.out.println("===="+parseUri);
        Class<?> clazz = RequestMappingMap.getRequesetMap().get(parseUri);
        Method mt = null;
        try {
            Object obj = clazz.newInstance();
            //获得该类所有的方法
            Method[] methods = clazz.getDeclaredMethods();
            //遍历所有的methods
            for (Method method : methods) {
                //是否有RequestMapping注解
                if (method.isAnnotationPresent(RequestMapping.class)){
                    //判断注解值是否与截取的值一致
                    String value = method.getAnnotation(RequestMapping.class).value();
                    if (value.equals(parseUri)) {
                        //注解值与截取的uri值一致
                        mt = method;
                        break;
                    }
                }
            }
            if(mt!=null){
                //执行用户请求
                try {
                    Object invoke = mt.invoke(obj);
                    if (invoke!=null){//有返回值 代表需要返回页面
                        View view = (View)invoke;
                        if (view.getDispathAction().equals(DispatchActionConstant.FORWARD)){//转发
                            request.getRequestDispatcher(view.getUrl()).forward(request, response);
                        }else if (view.getDispathAction().equals(DispatchActionConstant.REDIRECT)){
                            response.sendRedirect(request.getContextPath()+view.getUrl());
                        }else{
                           // request.getRequestDispatcher(view.getUrl()).forward(request, response);
                        }
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //首先初始化  将所有注解的类妨到容器中
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String basePackage = config.getInitParameter("basePackage");
        Set<Class<?>> setClasses = ScanClassTest.getAllClazzByPackagename(basePackage);
        System.out.println(setClasses.size());
        for (Class setClass : setClasses) {//遍历class容器
            //类上是否有controller注解
            if (setClass.isAnnotationPresent(Controller.class)) {
                //遍历类上所有方法
                Method[] methods = setClass.getDeclaredMethods();
                //判断方法上是否有requestMapping注解
                for (Method method : methods) {
                    //判断方法上是否有requestmapping注解
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        //获得注解的value值
                        String anoValue = method.getAnnotation(RequestMapping.class).value();
                        if (RequestMappingMap.getRequesetMap().containsKey(anoValue)) {
                            throw new RuntimeException(anoValue + "该注解标记路径重复了！");
                        }
                        RequestMappingMap.getRequesetMap().put(anoValue, setClass);
                    }
                }
            }
        }
        System.out.println("所有注解方法容器：" + RequestMappingMap.getRequesetMap());
        System.out.println("----初始化结束---");

    }
}
