package com.zjk.spring.NotesAndTests.test.injectionTest;

import org.springframework.stereotype.Component;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 2021-05-07 23:33
 */

@Component
public class InjectionTestImpl3 implements InjectionTest {
    @Override
    public String getName() {
        return "InjectionTestImpl-3";
    }
}
