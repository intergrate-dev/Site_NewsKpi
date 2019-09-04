package com.example.service.impl;

import com.example.bean.OperationEntity;
import com.example.bean.SiteMonitorEntity;
import com.example.redis.AppPrefix;
import com.example.redis.RedisService;
import com.example.service.SiteMonitorService;
import com.example.util.FastJsonConvertUtil;
import com.practice.bus.bean.EnumOperation;
import com.practice.bus.bean.EnumTask;
import com.practice.config.RabbitMQConfig;
import com.practice.mq.service.RabbitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SiteMonitorServiceImpl implements SiteMonitorService {
    private static Logger logger = LoggerFactory.getLogger(SiteMonitorServiceImpl.class);

    @Autowired
    RedisService redisService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    @Async
    public void parseHandleASync(OperationEntity oper, String status, EnumTask task) {
        this.parseHandle(oper, status, task, null);
    }

    @Override
    @Async
    public void processFail(OperationEntity oper, String statusFail, EnumTask task, String info) {
        this.parseHandle(oper, statusFail, task, info);
    }

    @Override
    public void parseHandle(OperationEntity oper, String status, EnumTask task, String info) {
        if (oper.getSITE_ID() == null || oper.getSITE_NAME() == null || oper.getSITE_TYPE() == null) {
            logger.info(" ==================== 站点配置信息不全 =================");
            return;
        }
        //logger.info("---------------------- parseHandle , {} ...... ---------------", status);
        List<SiteMonitorEntity> list = new ArrayList<>();
        try {
            SiteMonitorEntity entity = null;
            String[] sIds = oper.getSITE_ID().split(",");
            String[] sNames = oper.getSITE_NAME().split(",");
            String[] sTypes = oper.getSITE_TYPE().split(",");
            for (int i = 0, j = 0, k = 0; i < sIds.length; i++, j++, k++) {
                EnumOperation operation = EnumOperation.MODIFY;
                entity = new SiteMonitorEntity();
                entity.setId(sIds[i]);
                if (j < sNames.length) {
                    entity.setSiteName(sNames[j]);
                }
                if (k < sTypes.length) {
                    List<String> channles = null;
                    String channel = null;
                    switch (sTypes[k]) {
                        case ("NEWS"):
                            channel = "网站";
                            break;
                        case ("WECHAT"):
                            channel = "微信";
                            break;
                        case ("WEIBO"):
                            channel = "微博";
                            break;
                        default:
                            channel = "APP";
                    }
                    String prefix = AppPrefix.getInstance().getPrefix();
                    String keySite = prefix.concat("sid:").concat(entity.getId());
                    String keyST =  prefix.concat("sid_task:").concat(entity.getId()).concat("-").concat(task.name());
                    if (!redisService.exists(keyST)) {
                        operation = EnumOperation.ADD;
                        entity.setCreateTime(new Date());
                        redisService.set(keyST, operation.name(), -1);
                    }
                    if (redisService.exists(keySite)) {
                        channles = (List<String>) redisService.getList(keySite);
                        if (!channles.contains(channel)) {
                            channles.add(channel);
                            redisService.setList(keySite, channles);
                        }
                    } else {
                        channles = new ArrayList<>();
                        channles.add(channel);
                        redisService.setList(keySite, channles);
                    }
                    entity.setDataChannel(channles);
                }
                entity.setTask(task.name());
                entity.setStatus(status);
                entity.setUpdateTime(new Date());
                entity.setExtInfo(info);
                this.rabbitSend(entity, operation);
            }
        } catch (Exception e) {
            logger.error("======================= 站点： {}， 更新操作： {} 失败, error: {} =============", oper.getSITE_ID(), status, e.getMessage());
            e.printStackTrace();
        }
        //logger.info("---------------------- parseHandleSync , {} end  ---------------", status);
    }

    private void rabbitSend(SiteMonitorEntity sme, EnumOperation operation) throws Exception {
        RabbitMessage rm = new RabbitMessage();
        rm.setOperation(operation);
        rm.setSiteMonitor(sme);
        String target = FastJsonConvertUtil.toJsonObject(rm).toJSONString();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(target.getBytes(), messageProperties);
        rabbitTemplate.send(RabbitMQConfig.QUEUE_NAME, message);
    }
}

