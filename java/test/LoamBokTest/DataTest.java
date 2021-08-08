package com.zjk.spring.NotesAndTests.test.LoamBokTest;

import lombok.Data;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-04-28 00:34
 */

// Data注解也会为类成员变量增加get和set方法，
// 但是Data会额外添加一些其他方法，可能会影响到其他部分的代码，因此一般不使用该注解
// 自动添加的方法有:
// DataTest()
// getName()
// getAge()
// setName()
// setAge()
// equals()
// canEqual()
// hashCode()
// toString()
@Data
public class DataTest {
    private String name;
    private Integer age;
}
