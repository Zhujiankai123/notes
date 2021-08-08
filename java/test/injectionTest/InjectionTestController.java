package com.zjk.spring.NotesAndTests.test.injectionTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-05-07 23:30
 */

@RestController
@RequestMapping("/test")
public class InjectionTestController {

    // 注入方式1，所有接口的实现都打上@Component，然后通过名称注入
    // GET: 127.0.0.1:8081/injectionTest/test/t1
    // RESPONSE: InjectionTestImpl-1 InjectionTestImpl-2 InjectionTestImpl-3
    @Autowired
    private InjectionTest injectionTestImpl1;
    @Autowired
    private InjectionTest injectionTestImpl2;
    @Autowired
    private InjectionTest injectionTestImpl3;

    @GetMapping("/t1")
    public String t1(){
        return injectionTestImpl1.getName()+" "+
                injectionTestImpl2.getName()+" "+
                injectionTestImpl3.getName();
    }

    // 注入方法2，使用Qualifier指定注入的Bean
    // GET: 127.0.0.1:8081/injectionTest/test/t2
    // RESPONSE: InjectionTestImpl-3
    @Autowired
    @Qualifier(value = "injectionTestImpl3")
    private InjectionTest injectionTestImpl4;
    @GetMapping("/t2")
    public String t2(){
        return injectionTestImpl4.getName();
    }

    // 注入方法3,使用配置类进行注入(@Configuration+@Bean)
    // 要注意需要注入的具体实现是不能打上@Bean、@Component等的
    // 并使用Condition的自定义条件注解或成品条件注解注入指定的Bean
    // GET: 127.0.0.1:8081/injectionTest/test/t3
    // RESPONSE: InjectionTestImpl-c1
    @Autowired
    private InjectionTestCofig injectionTestCofig;

    @GetMapping("/t3")
    public String t3(){
        return injectionTestCofig.getName();
    }
}
