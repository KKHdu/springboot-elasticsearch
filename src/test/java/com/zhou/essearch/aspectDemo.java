package com.zhou.essearch;

import com.zhou.essearch.service.ServiceAspectDemo;
import com.zhou.essearch.tool.AspectAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class aspectDemo {
    @Test
    public static void main( String[] args )
    {
        System.out.println( "------> Hello Spring AOP! <------" );
        BeanFactory factory=new ClassPathXmlApplicationContext("beans.xml");
        ServiceAspectDemo serviceAspectDemo=(ServiceAspectDemo) factory.getBean("aspectDemo");
        // serviceAspectDemo.getCustomerById(2015);
        serviceAspectDemo.deleteCustomer("admins");

    }
}
