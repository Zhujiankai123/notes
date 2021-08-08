package com.zjk.spring.NotesAndTests.test.LoamBokTest;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-04-28 00:36
 */

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TestObj{
    private String name;
    private Integer age;
}

// GET: 127.0.0.1:8081/LoamBokTest/t1
// RESPONSE: name: abc,age: 456
@RestController
public class BuildTest {
    @GetMapping("/t1")
    public String t1(){
        // 一旦使用了builder注解，那么生成的无参构造方法实际上是一个private的，
        // 如果一定要对外开放空构造方法的话，可以添加public构造方法或直接使用lombok的NoArgsConstructor
        // 注意：此时还需要添加全参构造函数才能使得Builder和常规构造方法并存
        // 即只能通过builder方式进行构造，无法通过new的方法进行构造
        // TestObj testObj = new TestObj();
        TestObj testObj = TestObj.builder().name("abc").age(456).build();
        return "name: "+testObj.getName()+",age: "+testObj.getAge();
    }
}
