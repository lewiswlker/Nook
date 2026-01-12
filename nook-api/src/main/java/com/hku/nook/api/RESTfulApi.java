package com.hku.nook.api;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RESTfulApi {
    private final Map<Integer, Map<String, Object>> dataMap;

    public RESTfulApi(ResourceUrlProvider resourceUrlProvider) {
        dataMap = new HashMap<Integer, Map<String, Object>>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", i);
            data.put("name", "name" + i);
            dataMap.put(i, data);
        }
    }

    @GetMapping("/objects/{id}")
    public Map<String, Object> getData(@PathVariable int id) {
        return dataMap.get(id);
    }

    @DeleteMapping("/objects/{id}")
    public String deleteData(@PathVariable int id) {
        dataMap.remove(id);
        return "[Delete] success";
    }

    @PostMapping("/objects")
    public String postData(@RequestBody Map<String, Object> data) {
        Integer[] array = dataMap.keySet().toArray(new Integer[0]);
        Arrays.sort(array);
        int id = array[array.length - 1] + 1;
        dataMap.put(id, data);
        return "[Post] success";
    }

    @PutMapping("/objects")
    public String putData(@RequestBody Map<String, Object> data) {
        int id = (Integer) data.get("id");
        dataMap.put(id, data);
        return "[Put] success";
    }
}
