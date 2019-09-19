package com.controller;

import com.annotation.Controller;
import com.annotation.RequestMapping;
import com.util.View;

@Controller
public class TestController {

    @RequestMapping(value = "/test1")
    public View test1() {
        System.out.println("============test1==========");
        View view =new View("index.jsp","msg","123");
        return view;
    }

    @RequestMapping(value = "/test3")
    public void test3() {
        System.out.println("============test33333333333==========");
    }

}
