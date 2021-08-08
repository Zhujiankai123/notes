package com.zjk.spring.NotesAndTests.test.validationTest;

import com.zjk.spring.dto.UsrTest2DTO;
import com.zjk.spring.dto.UsrTestDTO;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;



/**
 * 需要的注解包：
 * org.hibernate.validator.constraints
 * javax.validation.constraints
 * javax.persistence
 *
 *  @Validated和@Valid注解的区别：
 *  1、二者实际上大部分功能都是相同的
 *  2、约定俗成：Valid一般用在级联校验，Validated一般用在控制器上
 *  3、Valid是Java标准，而Validated是Spring对Valid的一种扩展
 **/

// 注意，一定要在控制类上方打上@Validated,否则参数校验会无法生效
@RestController
@Validated
public class ValidationTest {

    // 通用验证1
    // GET: 127.0.0.1:8081/LoamBokTest/validationTest/t1?id=123&age=10&name=qqq
    // RESPONSE: 123, 10, qqq
    //
    // GET: 127.0.0.1:8081/LoamBokTest/validationTest/t1?id=1234&age=10&name=qqq
    // RESPONSE: {
    //    "code": 9999,
    //    "request": "GET /LoamBokTest/validationTest/t1",
    //    "message": "服务器未知异常"
    //}
    // EXCEPTION: test1.id: 最大不能超过999
    //
    // GET: 127.0.0.1:8081/LoamBokTest/validationTest/t1?id=123&age=0&name=qqq
    // RESPONSE: {
    //    "code": 9999,
    //    "request": "GET /LoamBokTest/validationTest/t1",
    //    "message": "服务器未知异常"
    //}
    // EXCEPTION: test1.age: 需要在1和999之间
    //
    // GET: 127.0.0.1:8081/LoamBokTest/validationTest/t1?id=123&age=10&name=q
    // RESPONSE: {
    //    "code": 9999,
    //    "request": "GET /LoamBokTest/validationTest/t1",
    //    "message": "服务器未知异常"
    //}
    // EXCEPTION: test1.name: 长度需要在2和10之间
    @GetMapping("/t1")
    public String test1(@RequestParam @Max(999) @Min(100) Integer id,
                        @RequestParam @Range(min=1,max=999) Integer age,
                        @RequestParam @Length(min=2,max=10) String name){
        return id.toString()+", "+age.toString()+", "+name;
    }

    // 级联校验
    // 级联校验一定要在控制器对应字段上打上@Valid
    // 实际验证中发现@Valid打在控制器上也可以实现级联
    // POST: 127.0.0.1:8081/LoamBokTest/validationTest/t2
    // POST Body: {
    //"name":"abc",
    //"id":443
    //}
    // RESPONSE: {
    //    "name": "abc",
    //    "id": 443
    //}
    //
    // POST Body: {
    //"name":"abc",
    //"id":44
    //}
    // RESPONSE: {
    //    "code": 9999,
    //    "request": "POST /LoamBokTest/validationTest/t2",
    //    "message": "服务器未知异常"
    //}
    //
    // 注释掉@Valid以后：
    // POST Body: {
    //"name":"abc",
    //"id":44
    //}
    // RESPONSE: {
    //    "name": "abc",
    //    "id": 44
    //}
    @PostMapping("/t2")
    public UsrTestDTO test2(@RequestBody @Valid UsrTestDTO usrTestDTO){
        return usrTestDTO;
    }

    // 自定义参数校验
    // 默认参数：
    //    int length_min() default 2;
    //    int length_max() default 10;
    //    int min_id() default 100;
    //    int max_id() default 999;
    // POST: 127.0.0.1:8081/LoamBokTest/validationTest/t3
    // POST Body: {
    //"name":"abc",
    //"id":444
    //}
    // RESPONSE: {
    //    "name": "abc",
    //    "id": 444
    //}
    // POST Body: {
    //"name":"abc",
    //"id":44
    //}
    // RESPONSE: {
    //    "code": 9999,
    //    "request": "POST /LoamBokTest/validationTest/t3",
    //    "message": "服务器未知异常"
    //}
    // ... default message [验证失败]]
    @PostMapping("/t3")
    public UsrTest2DTO test3(@RequestBody @Valid UsrTest2DTO usrTest2DTO){
        return usrTest2DTO;
    }
}
