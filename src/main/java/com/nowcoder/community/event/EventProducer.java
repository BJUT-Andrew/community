package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 发送系统通知消息的生产者
 * @author andrew
 * @create 2021-11-02 14:41
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //根据event事件，发送对应的数据到消息队列中
    public void fireEvent(Event event){
        //将事件对象转化为JSON字符串，发布到消息队列对应的主题中
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
