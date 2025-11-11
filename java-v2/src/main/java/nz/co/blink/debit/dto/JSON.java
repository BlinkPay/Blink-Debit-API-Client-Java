package nz.co.blink.debit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;
import java.util.Set;

/**
 * Minimal JSON utility for OpenAPI generated DTOs.
 */
public class JSON {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ObjectMapper getDefault() {
        return mapper;
    }

    /**
     * Register descendants for polymorphic deserialization.
     * No-op for now as generated deserializers handle this.
     */
    public static void registerDescendants(Class<?> cls, Map<String, Class<?>> descendants) {
        // No-op: generated deserializers handle polymorphism
    }

    /**
     * Register discriminator mappings for polymorphic deserialization.
     * No-op for now as generated deserializers handle this.
     */
    public static void registerDiscriminator(Class<?> cls, String propertyName, Map<String, Class<?>> mappings) {
        // No-op: generated deserializers handle polymorphism
    }

    /**
     * Check if an instance is of a given class.
     */
    public static boolean isInstanceOf(Class<?> cls, Object instance, Set<Class<?>> visited) {
        if (instance == null) {
            return false;
        }
        return cls.isAssignableFrom(instance.getClass());
    }
}
