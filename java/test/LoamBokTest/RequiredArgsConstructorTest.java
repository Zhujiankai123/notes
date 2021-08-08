package com.zjk.spring.NotesAndTests.test.LoamBokTest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-04-28 00:26
 */

// 必要参数构造方法
// RequiredArgsConstructorTest(String param1)
// 所有标识为NonNull的会自动加入构造参数列表中
@RequiredArgsConstructor
public class RequiredArgsConstructorTest {
    @NonNull
    private String param1;
    private String param2;
    private String param3;
}
