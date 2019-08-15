package com.zhou.essearch.tool;

import com.zhou.essearch.service.ServiceAspectDemo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


// 暂时不明白怎么用，目前直接写在测试里面  -->aspectDemo
public class AspectAction {
    public static void actionAll(){
        System.out.println( "------> Hello Spring AOP! <------" );
        BeanFactory factory=new ClassPathXmlApplicationContext("beans.xml");
        ServiceAspectDemo serviceAspectDemo=(ServiceAspectDemo) factory.getBean("aspectDemo");
    }
}
