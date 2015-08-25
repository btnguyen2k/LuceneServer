package lucene.spec;

import java.util.Map;

import util.IndexUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.dao.BaseBo;

/**
 * Spec for a field.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class FieldSpec extends BaseBo {

    public enum Type {
        ID("id"), STRING("string"), LONG("long"), DOUBLE("double");

        private String value;

        private Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean defaultIsStored() {
            return this == ID;
        }

        public boolean defaultIsIndexed() {
            return true;
        }
    }

    /**
     * Default field type when not specified.
     */
    public final static FieldSpec.Type DEFAULT_FIELD_TYPE = FieldSpec.Type.STRING;

    public static FieldSpec newInstance(String name, String type) {
        return newInstance(name, type != null ? FieldSpec.Type.valueOf(type.toUpperCase()) : null);
    }

    public static FieldSpec newInstance(String name, FieldSpec.Type type) {
        if (!IndexUtils.isValidName(name)) {
            return null;
        }
        if (type == null) {
            type = DEFAULT_FIELD_TYPE;
        }
        FieldSpec fieldSpec = new FieldSpec();
        fieldSpec.name(name).type(type);
        fieldSpec.markIndexed(type.defaultIsIndexed());
        fieldSpec.markStored(type.defaultIsStored());
        return fieldSpec;
    }

    public static FieldSpec newInstance(String name, Map<String, Object> fieldData) {
        String type = DPathUtils.getValue(fieldData, FIELD_TYPE, String.class);
        FieldSpec fieldSpec = newInstance(name, type);
        if (fieldSpec != null) {
            Boolean isStored = DPathUtils.getValue(fieldData, FIELD_IS_STORED, Boolean.class);
            if (isStored != null) {
                fieldSpec.markStored(isStored.booleanValue());
            }

            Boolean isIndexed = DPathUtils.getValue(fieldData, FIELD_IS_INDEXED, Boolean.class);
            if (isIndexed != null) {
                fieldSpec.markIndexed(isIndexed.booleanValue());
            }
        }
        return fieldSpec;
    }

    private final static String FIELD_TYPE = "type";
    private final static String FIELD_IS_STORED = "store";
    private final static String FIELD_IS_INDEXED = "index";

    private String name;

    @JsonIgnore
    public String name() {
        return name;
    }

    public FieldSpec name(String name) {
        this.name = IndexUtils.normalizeName(name);
        return this;
    }

    @JsonIgnore
    public FieldSpec.Type type() {
        String typeStr = getAttribute(FIELD_TYPE, String.class);
        return typeStr != null ? FieldSpec.Type.valueOf(typeStr.toUpperCase()) : null;
    }

    public FieldSpec type(FieldSpec.Type type) {
        setAttribute(FIELD_TYPE, type != null ? type.value : null);
        return this;
    }

    @JsonIgnore
    public boolean isStored() {
        Boolean result = getAttribute(FIELD_IS_STORED, Boolean.class);
        return result != null ? result.booleanValue() : false;
    }

    public FieldSpec markStored(boolean value) {
        setAttribute(FIELD_IS_STORED, value);
        return this;
    }

    @JsonIgnore
    public boolean isIndexed() {
        Boolean result = getAttribute(FIELD_IS_INDEXED, Boolean.class);
        return result != null ? result.booleanValue() : false;
    }

    public FieldSpec markIndexed(boolean value) {
        setAttribute(FIELD_IS_INDEXED, value);
        return this;
    }

    public boolean validateValue(Object value) {
        if (value == null) {
            return false;
        }
        FieldSpec.Type type = type();
        switch (type) {
        case ID:
            return true;
        case STRING:
            return true;
        case LONG:
            return value instanceof Number;
        default:
            return false;
        }
    }
}
