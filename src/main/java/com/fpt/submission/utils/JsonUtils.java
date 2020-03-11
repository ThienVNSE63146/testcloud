package com.fpt.submission.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import java.io.IOException;
import java.util.*;

@SuppressWarnings({"deprecation", "serial"})
public class JsonUtils extends UntypedObjectDeserializer {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object mapObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        String firstKey;

        JsonToken t = p.getCurrentToken();

        if (t == JsonToken.START_OBJECT) {
            firstKey = p.nextFieldName();
        } else if (t == JsonToken.FIELD_NAME) {
            firstKey = p.getCurrentName();
        } else {
            if (t != JsonToken.END_OBJECT) {
                throw ctxt.mappingException(handledType(), p.getCurrentToken());
            }
            firstKey = null;
        }


        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>(2);
        if (firstKey == null)
            return resultMap;

        p.nextToken();
        resultMap.put(firstKey, deserialize(p, ctxt));

        Set<String> listKeys = new LinkedHashSet<>();

        String nextKey;
        while ((nextKey = p.nextFieldName()) != null) {
            p.nextToken();
            if (resultMap.containsKey(nextKey)) {
                Object listObject = resultMap.get(nextKey);

                if (!(listObject instanceof List)) {
                    listObject = new ArrayList<>();
                    ((List) listObject).add(resultMap.get(nextKey));

                    resultMap.put(nextKey, listObject);
                }

                ((List) listObject).add(deserialize(p, ctxt));

                listKeys.add(nextKey);

            } else {
                resultMap.put(nextKey, deserialize(p, ctxt));
            }
        }
        return resultMap;
    }

}
