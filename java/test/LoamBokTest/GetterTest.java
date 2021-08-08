package com.zjk.spring.NotesAndTests.test.LoamBokTest;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-04-28 00:29
 */

// 对象属性赋值可以通过带参构造方法，
// 也可以通过无参构造方法创建对象，然后为对象的属性赋值
// 如果对象的属性为私有属性，那么是无法直接赋值的，要使用set方法进行赋值
// Getter:
// getName()
// getAge()
// Setter:
// setName(String name)
// setAge(String name)
@Getter
@Setter
public class GetterTest {
    private String name;
    private Integer age;
}
