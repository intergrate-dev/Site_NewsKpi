package com.example.util;

import java.util.List;

public class CommonUtil {

    public static String setPageTypeIds(List<String> pageTypeIds, String pTypeIds) {
        if (pageTypeIds != null && pageTypeIds.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            pageTypeIds.stream().forEach(s -> {
                buffer.append(s).append(",");
            });
            pTypeIds = buffer.substring(0, buffer.length() - 1);
        }
        return pTypeIds;
    }
}
