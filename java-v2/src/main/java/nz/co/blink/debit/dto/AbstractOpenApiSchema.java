package nz.co.blink.debit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Minimal base class for OpenAPI polymorphic schemas (oneOf, anyOf).
 */
public abstract class AbstractOpenApiSchema {

    protected Object actualInstance;
    protected String schemaType;

    protected AbstractOpenApiSchema() {
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
}
