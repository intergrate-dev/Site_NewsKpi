package com.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 这里使用work queues模式
 * 一个队列 两个消费者
 * Spring AMQP默认已经帮我们实现了：
 * ①队列/消息持久化
 * ②手动应答
 * ③公平分发，能者多劳
 */
@Configuration
public class RabbitMQConfig {
	/*@Value("${app.config.identify}")
	private static String identify;*/

	//"local_sitemonitor_queue";
	public static final String QUEUE_NAME = "index_queue_1";

	// 向RabbitMQ发消息时，不用默认的序列化方式，使用json格式
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/*@Bean
	public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}*/

	// 创建队列 在该Bean加入Spring容器时就会创建该队列
	@Bean
	public Queue indexQueue() {
		return new Queue(QUEUE_NAME);
	}
	
	/*// 创建消费者1
	@Bean
	public RabbitMQReceiver receiver1() {
		return new RabbitMQReceiver();
	}
	
	// 创建消费者2
	@Bean
	public RabbitMQReceiver receiver2() {
		return new RabbitMQReceiver();
	}*/
}
