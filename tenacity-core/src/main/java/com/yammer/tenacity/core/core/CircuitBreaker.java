package com.yammer.tenacity.core.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.StringTenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;

@JsonDeserialize(using = CircuitBreaker.Deserializer.class)
public class CircuitBreaker {
    public enum State {
        OPEN, CLOSED, FORCED_OPEN, FORCED_CLOSED,
        FORCED_RESET //Used to "unset" any FORCED state
    }

    public static class Deserializer extends StdDeserializer<CircuitBreaker> {
        private static final long serialVersionUID = -1293812392173912L;
        private transient final TenacityPropertyKeyFactory keyFactory = new StringTenacityPropertyKeyFactory();

        public Deserializer() {
            super(CircuitBreaker.class);
        }

        @Override
        public CircuitBreaker deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            final ObjectNode objectNode = p.readValueAsTree();
            final TenacityPropertyKey key  = keyFactory.from(objectNode.get("id").asText());
            return new CircuitBreaker(key, State.valueOf(objectNode.get("state").asText().toUpperCase()));
        }
    }

    @NotNull @Valid
    private final TenacityPropertyKey id;
    @NotNull @Valid
    private final State state;

    @JsonCreator
    public CircuitBreaker(@JsonProperty("id") TenacityPropertyKey id,
                          @JsonProperty("state") State state) {
        this.id = id;
        this.state = state;
    }

    public static CircuitBreaker open(TenacityPropertyKey id) {
        return new CircuitBreaker(id, State.OPEN);
    }

    public static CircuitBreaker closed(TenacityPropertyKey id) {
        return new CircuitBreaker(id, State.CLOSED);
    }

    public static CircuitBreaker forcedOpen(TenacityPropertyKey id) {
        return new CircuitBreaker(id, State.FORCED_OPEN);
    }

    public static CircuitBreaker forcedClosed(TenacityPropertyKey id) {
        return new CircuitBreaker(id, State.FORCED_CLOSED);
    }

    public static Optional<CircuitBreaker> usingHystrix(TenacityPropertyKey id) {
        final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(id);

        if (circuitBreaker == null) {
            return Optional.absent();
        }

        final HystrixCommandProperties commandProperties = TenacityCommand.getCommandProperties(id);

        if (commandProperties.circuitBreakerForceOpen().get()) {
            return Optional.of(CircuitBreaker.forcedOpen(id));
        } else if (commandProperties.circuitBreakerForceClosed().get()) {
            return Optional.of(CircuitBreaker.forcedClosed(id));
        } else if (circuitBreaker.allowRequest()) {
            return Optional.of(CircuitBreaker.closed(id));
        } else {
            return Optional.of(CircuitBreaker.open(id));
        }
    }

    public TenacityPropertyKey getId() {
        return id;
    }

    public boolean isOpen() {
        return state == State.OPEN || state == State.FORCED_OPEN;
    }

    public State getState() {
        return state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CircuitBreaker other = (CircuitBreaker) obj;
        return Objects.equals(this.id.name(), other.id.name())
                && Objects.equals(this.state, other.state);
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "id=" + id +
                ", state=" + state +
                '}';
    }
}