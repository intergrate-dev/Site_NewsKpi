package com.example.test.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.util.FastJsonConvertUtil;
import com.practice.bus.bean.DocInfo;
import com.practice.bus.bean.EnumOperation;
import com.practice.config.RabbitMQConfig;
import com.practice.mq.service.RabbitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文档业务CRUD对应的Service
 */
@Service
public class DocService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * @param docInfo
     */
    public void createDoc(DocInfo docInfo) {
        //TODO 这里拿到文档信息，调用dao执行入库

        //入库成功后，向RabbitMQ发消息 由消费者来解析文件，提取摘要，构造documentTemplate，索引文档
        RabbitMessage rabbitMessage = new RabbitMessage();
        rabbitMessage.setOperation(EnumOperation.ADD);
        rabbitMessage.setDocInfo(docInfo);
        // rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, rabbitMessage);

        JSONObject json = new JSONObject();
        //json.put("send_msg", "34535435435435 ..... grgtrgrger");
        //rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, json.toString());
        //String target = "send_msg--34535435435435 ..... grgtrgrger";

        //json.put("send_msg", FastJsonConvertUtil.convertObjectToJSON(rabbitMessage));
        json.put("operation", EnumOperation.ADD);
        json.put("docInfo", docInfo);
        String target = json.toJSONString();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(target.getBytes(), messageProperties);
        rabbitTemplate.send(RabbitMQConfig.QUEUE_NAME, message);

        logger.info("创建文档，发送MQ消息：" + json);
    }

    /**
     * @param docInfo
     */
    public void modifyDoc(DocInfo docInfo) {
        //TODO 这里拿到文档信息，调用dao执行更新

        //更新成功后，向RabbitMQ发消息 由消费者来解析文件，提取摘要，构造documentTemplate，索引文档
        RabbitMessage rabbitMessage = new RabbitMessage();
        rabbitMessage.setOperation(EnumOperation.MODIFY);
        rabbitMessage.setDocInfo(docInfo);
        // rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, rabbitMessage);
        /*CorrelationData correlationData = new CorrelationData(docInfo.getDocId().toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, FastJsonConvertUtil.toJsonObject(rabbitMessage), correlationData);*/

        JSONObject json = new JSONObject();
        json.put("send_msg", "34535435435435 ..... grgtrgrger");
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, json);
        logger.info("修改文档，发送MQ消息：" + rabbitMessage);
    }

    public void deleteDoc(DocInfo docInfo) {
        //TODO 这里拿到文档信息，调用dao执行删除

        //删除成功后，向RabbitMQ发消息 由消费者来通知es删除被索引文档
        RabbitMessage rabbitMessage = new RabbitMessage();
        rabbitMessage.setOperation(EnumOperation.DELETE);
        rabbitMessage.setDocInfo(docInfo);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, rabbitMessage);
        logger.info("删除文档，发送MQ消息：" + rabbitMessage);
    }
}
