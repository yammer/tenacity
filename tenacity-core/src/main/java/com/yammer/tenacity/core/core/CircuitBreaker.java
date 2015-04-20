package com.yammer.tenacity.core.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yammer.tenacity.core.properties.StringTenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;

@JsonDeserialize(using = CircuitBreaker.Deserializer.class)
public class CircuitBreaker {
    public static class Deserializer extends StdDeserializer<CircuitBreaker> {
        private static final long serialVersionUID = -1293812392173912L;
        private final TenacityPropertyKeyFactory keyFactory = new StringTenacityPropertyKeyFactory();

        public Deserializer() {
            super(CircuitBreaker.class);
        }

        @Override
        public CircuitBreaker deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectNode objectNode = p.readValueAsTree();
            final TenacityPropertyKey key  = keyFactory.from(objectNode.get("id").asText());
            return new CircuitBreaker(key, objectNode.get("open").asBoolean());
        }
    }

    @NotNull @Valid
    private final TenacityPropertyKey id;
    private final boolean open;

    @JsonCreator
    public CircuitBreaker(@JsonProperty("id") TenacityPropertyKey id,
                          @JsonProperty("open") boolean open) {
        this.id = id;
        this.open = open;
    }

    public static CircuitBreaker open(TenacityPropertyKey id) {
        return new CircuitBreaker(id, true);
    }

    public static CircuitBreaker closed(TenacityPropertyKey id) {
        return new CircuitBreaker(id, false);
    }

    public TenacityPropertyKey getId() {
        return id;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, open);
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
                && Objects.equals(this.open, other.open);
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "id=" + id +
                ", open=" + open +
                '}';
    }
}