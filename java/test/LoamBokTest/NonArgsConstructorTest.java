package com.zjk.spring.NotesAndTests.test.LoamBokTest;

import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-04-28 00:22
 */

// 无参构造函数测试
// NonArgsConstructorTest()
// NonNull只是标明某个字段不能为空，对无参构造函数无影响
@NoArgsConstructor
public class NonArgsConstructorTest {
    @NonNull
    private String param1;
    private String param2;
    private String param3;
}
