package com.zjk.spring.NotesAndTests.test.injectionTest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-05-07 23:44
 */

@Configuration
@PropertySource(value = "classpath:config/injection-test.properties")
public class InjectionTestImplConfiguration {

    // 常用的成品条件注解
    // @ConditionalOnClass:
    //  某个类在路径中时，才会注入该Bean
    // @ConditionalOnBean:
    //  当环境中存在指定的Bean时，才会注入该Bean
    // @ConditionalOnMissingBean:
    //  当环境中没有对应接口的实现时注入该Bean
    // @Conditional
    //  当指定的自定义条件类返回true时，才会注入该Bean，
    //  这个自定义条件类应当实现Condition接口，并对matches方法进行重写
    // @ConditionalOnProperty
    //  根据配置文件进行注入
    // ...

    @Bean
    @ConditionalOnProperty(value="impl.bean",havingValue = "c1")
    public InjectionTestCofig b1(){
        return new InjectionTestImpl_c1();
    }

    @Bean
    @ConditionalOnProperty(value="impl.bean",havingValue = "c2")
    public InjectionTestCofig b2(){
        return new InjectionTestImpl_c2();
    }

    @Bean
    @ConditionalOnProperty(value="impl.bean",havingValue = "c3")
    public InjectionTestCofig b3(){
        return new InjectionTestImpl_c3();
    }
}
