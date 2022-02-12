package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件实体类
 * @author andrew
 * @create 2021-11-02 14:06
 */
public class Event {

    //事件类型（点赞、关注等...）
    private String topic;
    private int userId;
    private int entityType;
    private int entityId;
    private int entityUserId;
    //其他日后扩展数据
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    //为了赋值方便，将所有set方法加上返回值
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;

    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;

    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;

    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
