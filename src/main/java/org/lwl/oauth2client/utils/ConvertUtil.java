package org.lwl.oauth2client.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ConvertUtil {

    public static Map<String, String> Obj2Map(Object obj)  {
        Map<String, String> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if(value != null) {
                    map.put(field.getName(), field.get(obj).toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
