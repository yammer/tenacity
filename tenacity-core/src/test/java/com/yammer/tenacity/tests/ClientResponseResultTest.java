package com.yammer.tenacity.tests;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.tenacity.core.helper.ClientResponseResult;
import com.yammer.tenacity.core.helper.HttpException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ClientResponseResultTest {

    @Test
    public void testResult(){
        ClientResponseResult<String> successfullResult = ClientResponseResult.create("Hello");

        assertThat(successfullResult.isSuccess(), is(true));
        assertThat(successfullResult.getResult().isPresent(), is(true));
        assertThat(successfullResult.getResult().get(), is("Hello"));
    }

    @Test
    public void testFallback(){
        UniformInterfaceException exception = new UniformInterfaceException("failed request", mock(ClientResponse.class));
        HttpException httpException = new HttpException(exception.getMessage(), 404, exception);
        ClientResponseResult<String> failedResult = ClientResponseResult.clientFailure(httpException);

        assertThat(failedResult.isSuccess(), is(false));
        assertThat(failedResult.getFallbackException(), is(httpException));
        assertThat(failedResult.getFallbackException().getMessage(), is("failed request"));
        assertThat(failedResult.getFallbackException().getStatus(), is(404));
    }
}
