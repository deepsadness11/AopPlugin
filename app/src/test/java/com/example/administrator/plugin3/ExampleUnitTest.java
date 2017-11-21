package com.example.administrator.plugin3;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        String str = "com/example/administrator/plugin3/MainActivity";
        String str = "=com\\example\\administrator\\plugin3\\MainActivity";
        str = str.replace("\\", ".");
        System.out.println(str);
    }
}