package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.helper.ResultWithFallback;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultWithFallbackTest {

    @Test
    public void testResult(){
        ResultWithFallback<String,Integer> successfullResult = ResultWithFallback.create("Hello");

        assertThat(successfullResult.isSuccess(), is(true));
        assertThat(successfullResult.getResult(), is("Hello"));
    }

    @Test
    public void testFallback(){
        ResultWithFallback<String,Integer> failedResult = ResultWithFallback.failedCommand(1);

        assertThat(failedResult.isSuccess(), is(false));
        assertThat(failedResult.getFallback(), is(1));
    }
}
