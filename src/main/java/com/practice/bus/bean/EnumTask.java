package com.practice.bus.bean;

import java.io.Serializable;

/**
 * 操作类型的枚举类
 *
 */
public enum EnumTask implements Serializable {
	// 第一行一定要写枚举类的实例
	SPREADMAP("spreadMap"),
	ARTICLEPRESS("articlePress"),
	WEBWORD("webWord"),
	SITENEWS("siteNews"),
	HOTNEWS("hotNews"),
	ORIGINALNEWS("originalNews"),
	KEYWORDNEWS("keyWordNews"),
	ZLMTNEWS("zlMtNews"),
	EMOTION("emotion"),
	KEYWORDKPI("keyWordKpi");

	final String task;

	EnumTask(String task) {
		this.task = task;
	}

}