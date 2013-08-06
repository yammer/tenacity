package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.helper.ResultWithFallback;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultWithFallbackTest {

    @Test
    public void testResult(){
        ResultWithFallback<String,Integer> successfullResult = ResultWithFallback.create("Hello");

        assertThat(successfullResult.isPresent(), is(true));
        assertThat(successfullResult.hasFallback(), is(false));
        assertThat(successfullResult.getResult(), is("Hello"));
    }

    @Test
    public void testFallback(){
        ResultWithFallback<String,Integer> failedResult = ResultWithFallback.failedCommand(1);

        assertThat(failedResult.isPresent(), is(false));
        assertThat(failedResult.hasFallback(), is(true));
        assertThat(failedResult.getFallback(), is(1));
    }

    @Test(expected = NullPointerException.class)
    public void createResultWithNullFails(){
        ResultWithFallback.<String,String>create(null);
    }

    @Test(expected = NullPointerException.class)
    public void createFallbackWithNullFails(){
        ResultWithFallback.<String,String>failedCommand(null);
    }

    @Test(expected = IllegalStateException.class)
    public void callingGetResultOnFailedResultThrowsException(){
        ResultWithFallback.<String,String>failedCommand("Ahhhh").getResult();
    }
}
