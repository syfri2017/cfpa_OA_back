package com.syfri.process.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/app/")
public class ActivitiController {

    /**
     * 身份认证
     */
    @RequestMapping("authentication")
    public Map<String, Object> authenticate() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("login", "admin");
        return map;
    }


    /**
     * 身份认证
     */
    @RequestMapping("rest/authenticate")
    public Map<String, Object> getAuthenticate() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("login", "admin");
        return map;
    }

    /**
     * 账号信息
     */
    @RequestMapping("rest/account")
    public Map<String, Object> getAccount() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("email", "admin");
        map.put("firstName", "My");
        map.put("fullName", "Administrator");
        map.put("id", "admin");
        map.put("lastName", "Administrator");

        Map<String, Object> groupMap = new HashMap<String, Object>();
        map.put("id", "ROLE_ADMIN");
        map.put("name", "Superusers");
        map.put("type", "security-role");

        List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();
        groups.add(groupMap);

        map.put("groups", groups);

        return map;
    }

    /**
     * 初始化
     */
    @RequestMapping("rest/models")
    public Object getModels() {
        Map<String, Object> map = new HashMap<String, Object>();
        String jsonStr = "{\"modelId\":\"9dd84f5d-e9ed-44fa-b328-c7646efd766e\",\"name\":\"TEST1\",\"key\":\"TEST\",\"description\":\"\",\"lastUpdated\":\"2019-01-20T15:14:43.200+0000\",\"lastUpdatedBy\":\"admin\",\"model\":{\"id\":\"canvas\",\"resourceId\":\"canvas\",\"stencilset\":{\"namespace\":\"http://b3mn.org/stencilset/bpmn2.0#\"},\"properties\":{\"process_id\":\"TEST\",\"name\":\"TEST1\"},\"childShapes\":[{\"bounds\":{\"lowerRight\":{\"x\":130,\"y\":193},\"upperLeft\":{\"x\":100,\"y\":163}},\"childShapes\":[],\"dockers\":[],\"outgoing\":[],\"resourceId\":\"startEvent1\",\"stencil\":{\"id\":\"StartNoneEvent\"}}],\"modelType\":\"model\"}}";
        return  JSONObject.parseObject(jsonStr);
    }
}
