package com.yammer.tenacity.tests;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.tenacity.core.helper.ClientException;
import com.yammer.tenacity.core.helper.ClientResponseResult;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("deprecation")
@Deprecated
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
        ClientException clientException = new ClientException(exception);
        ClientResponseResult<String> failedResult = ClientResponseResult.clientFailure(clientException);

        assertThat(failedResult.isSuccess(), is(false));
        assertThat(failedResult.getFallbackException(), is(clientException));
        assertThat(failedResult.getFallbackException().getCause().getMessage(), is("failed request"));

        ClientResponseResult<String> exceptionConstructor = ClientResponseResult.clientFailure(exception);

        assertThat(exceptionConstructor.isSuccess(), is(false));
        assertThat(exceptionConstructor.getFallbackException(), is(clientException));
        assertThat(exceptionConstructor.getFallbackException().getCause().getMessage(), is("failed request"));

        ClientResponseResult<String> statusConstructor = ClientResponseResult.clientFailure("failed request", 404);

        assertThat(statusConstructor.isSuccess(), is(false));
        assertThat(statusConstructor.getFallbackException().getStatus(), is(404));
        assertThat(statusConstructor.getFallbackException().getMessage(), is("failed request"));
    }
}
