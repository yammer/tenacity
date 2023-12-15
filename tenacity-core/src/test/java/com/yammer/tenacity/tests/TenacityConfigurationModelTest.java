package com.yammer.tenacity.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.SemaphoreConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Rule;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.validation.Validator;

//import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class TenacityConfigurationModelTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final TenacityConfiguration defaultConfiguration = new TenacityConfiguration();
    private final TenacityConfiguration configurationWithThread = new TenacityConfiguration(
            new ThreadPoolConfiguration(),
            new CircuitBreakerConfiguration(),
            new SemaphoreConfiguration(),
            1000,
            HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
    private final TenacityConfiguration configurationWithSemaphore = new TenacityConfiguration(
            new ThreadPoolConfiguration(),
            new CircuitBreakerConfiguration(),
            new SemaphoreConfiguration(),
            1000,
            HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE);
    private final Validator validator = Validators.newValidator();

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void toJson() throws Exception {
        assertThat(objectMapper.writeValueAsString(defaultConfiguration))
                .isEqualTo(readJsonFixture("tenacityConfiguration.json"));
        assertThat(objectMapper.writeValueAsString(configurationWithThread))
                .isEqualTo(readJsonFixture("tenacityConfigurationThread.json"));
        assertThat(objectMapper.writeValueAsString(configurationWithSemaphore))
                .isEqualTo(readJsonFixture("tenacityConfigurationSemaphore.json"));
    }

    @Test
    public void toObject() throws Exception {
        assertThat(objectMapper.readValue(readJsonFixture("tenacityConfiguration.json"), TenacityConfiguration.class))
                .isEqualTo(defaultConfiguration);
        assertThat(objectMapper.readValue(readJsonFixture("tenacityConfigurationThread.json"), TenacityConfiguration.class))
                .isEqualTo(configurationWithThread);
        assertThat(objectMapper.readValue(readJsonFixture("tenacityConfigurationSemaphore.json"), TenacityConfiguration.class))
                .isEqualTo(configurationWithSemaphore);
    }

    @Test
    public void validates() throws Exception {
        validator.validate(objectMapper.readValue(readJsonFixture("tenacityConfiguration.json"), TenacityConfiguration.class))
                .isEmpty();
        validator.validate(objectMapper.readValue(readJsonFixture("tenacityConfigurationThread.json"), TenacityConfiguration.class))
                .isEmpty();
        validator.validate(objectMapper.readValue(readJsonFixture("tenacityConfigurationSemaphore.json"), TenacityConfiguration.class))
                .isEmpty();
    }
    private String readJsonFixture(String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI())), StandardCharsets.UTF_8);
    }
}
