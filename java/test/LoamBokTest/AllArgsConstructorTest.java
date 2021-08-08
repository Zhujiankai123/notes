package com.zjk.spring.NotesAndTests.test.LoamBokTest;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-04-28 00:19
 */

// 全参数构造函数
// AllArgsConstructorTest(String param1,String param2,String param3)
// NonNull只是标明某个字段不能为空，对全参构造函数无影响
@AllArgsConstructor
public class AllArgsConstructorTest {
    @NonNull
    private String param1;
    private String param2;
    private String param3;
}
