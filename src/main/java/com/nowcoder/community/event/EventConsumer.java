package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessages(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        event.getData().forEach(content::put); // 额外数据

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = TOPIC_PUBLISH)
    public void handlePublishMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticSearchService.saveDiscussPost(discussPost);
    }

    @KafkaListener(topics = TOPIC_DELETE)
    public void handleDeleteMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticSearchService.deleteDiscussPost(discussPost);
    }
}
