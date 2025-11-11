package nz.co.blink.debit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Minimal base class for OpenAPI polymorphic schemas (oneOf, anyOf).
 */
public abstract class AbstractOpenApiSchema {

    protected Object actualInstance;
    protected String schemaType;
    protected Boolean nullable;

    protected AbstractOpenApiSchema() {
    }

    protected AbstractOpenApiSchema(String schemaType, Boolean nullable) {
        this.schemaType = schemaType;
        this.nullable = nullable;
    }

    public Object getActualInstance() {
        return actualInstance;
    }

    public void setActualInstance(Object instance) {
        this.actualInstance = instance;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public Boolean isNullable() {
        return nullable;
    }

    /**
     * Get the schema mappings for this polymorphic type.
     */
    public abstract Map<String, Class<?>> getSchemas();
}
