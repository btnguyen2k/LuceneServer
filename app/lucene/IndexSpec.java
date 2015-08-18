package lucene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.dao.BaseBo;

/**
 * Spec for an index.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class IndexSpec extends BaseBo {

    public final static String SPEC_FILE = "index.spec";

    public static IndexSpec newInstance(String name) {
        IndexSpec spec = new IndexSpec();
        spec.name(name);
        return spec;
    }

    @SuppressWarnings("unchecked")
    public static IndexSpec newInstance(String name, Map<String, Object> requestData) {
        IndexSpec spec = newInstance(name);
        if (spec != null) {
            spec.fields(DPathUtils.getValue(requestData, ATTR_FIELDS, Map.class));
        }
        return spec;
    }

    /**
     * Loads index spec from a directory.
     * 
     * @param dir
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static IndexSpec loadSpec(final Directory dir) throws IOException {
        try (IndexInput input = dir.openInput(SPEC_FILE, null)) {
            String specAsJson = input.readString();
            IndexSpec spec = new IndexSpec();
            try {
                Map<String, Object> specMap = SerializationUtils.fromJsonString(specAsJson,
                        Map.class);
                spec.fromMap(specMap);
                return spec;
            } catch (Exception e) {
                return null;
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            return null;
        }
    }

    /**
     * Saves index spec to a directory.
     * 
     * @param dir
     * @param spec
     * @throws IOException
     */
    public static void saveSpec(final Directory dir, final IndexSpec spec) throws IOException {
        try (IndexOutput output = dir.createOutput(SPEC_FILE, null)) {
            String specAsJson = SerializationUtils.toJsonString(spec.toMap());
            output.writeString(specAsJson);
        }
    }

    private final static String ATTR_NAME = "name";
    private final static String ATTR_SECRET = "secret";
    private final static String ATTR_FIELDS = "fields";

    /**
     * Merges with another spec.
     * 
     * @param spec
     * @return
     */
    public IndexSpec merge(IndexSpec spec) {
        return merge(spec, false);
    }

    /**
     * Merges with another spec.
     * 
     * @param spec
     * @param override
     * @return
     */
    public IndexSpec merge(IndexSpec spec, boolean override) {
        if (spec != null) {
            if (override) {
                this.secret(spec.secret());
            }
            Map<String, FieldSpec> existingFields = fields();
            Map<String, FieldSpec> newFields = spec.fields();
            if (newFields != null) {
                for (Entry<String, FieldSpec> entry : newFields.entrySet()) {
                    String fieldName = entry.getKey().trim().toLowerCase();
                    if (existingFields.get(fieldName) == null || override) {
                        field(fieldName, entry.getValue());
                    }
                }
            }
        }
        return this;
    }

    @JsonIgnore
    public String name() {
        return getAttribute(ATTR_NAME, String.class);
    }

    public IndexSpec name(String name) {
        setAttribute(ATTR_NAME, name);
        return this;
    }

    @JsonIgnore
    public String secret() {
        return getAttribute(ATTR_SECRET, String.class);
    }

    public IndexSpec secret(String secret) {
        setAttribute(ATTR_SECRET, secret);
        return this;
    }

    private Map<String, FieldSpec> fields;

    @SuppressWarnings("unchecked")
    public Map<String, FieldSpec> fields() {
        if (fields == null) {
            Map<String, FieldSpec> myFields = new HashMap<String, FieldSpec>();
            Map<String, Map<String, Object>> fieldsData = getAttribute(ATTR_FIELDS, Map.class);
            if (fieldsData != null) {
                for (Entry<String, Map<String, Object>> entry : fieldsData.entrySet()) {
                    String fieldName = entry.getKey().trim().toLowerCase();
                    FieldSpec field = FieldSpec.newInstance(fieldName, entry.getValue());
                    if (field != null) {
                        myFields.put(fieldName, field);
                    }
                }
            }
            fields = myFields;
        }
        return Collections.unmodifiableMap(fields);
    }

    public FieldSpec field(String name) {
        return fields().get(name.trim().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    public IndexSpec field(String name, FieldSpec spec) {
        Map<String, Map<String, Object>> fieldsData = getAttribute(ATTR_FIELDS, Map.class);
        if (fieldsData == null) {
            fieldsData = new HashMap<String, Map<String, Object>>();
        }
        fieldsData.put(name.trim().toLowerCase(), spec.toMap());
        setAttribute(ATTR_FIELDS, fieldsData);
        return this;
    }

    public IndexSpec fields(Map<String, ?> fieldSpecs) {
        Map<String, Object> tmp = new HashMap<String, Object>();
        if (fieldSpecs != null) {
            for (Entry<String, ?> entry : fieldSpecs.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldData = entry.getValue();
                if (fieldData instanceof Map) {
                    tmp.put(fieldName, fieldData);
                } else if (fieldData instanceof FieldSpec) {
                    tmp.put(fieldName, ((FieldSpec) fieldData).toMap());
                }
            }
        }
        setAttribute(ATTR_FIELDS, tmp);
        return this;
    }

    /*----------------------------------------------------------------------*/
    @Override
    protected IndexSpec setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        if (StringUtils.equals(name, ATTR_FIELDS)) {
            fields = null;
        }
        return this;
    }

    public IndexSpec fromMap(Map<String, Object> dataMap) {
        super.fromMap(dataMap);
        fields = null;
        return this;
    }
}
