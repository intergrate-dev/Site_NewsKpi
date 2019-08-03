package com.example.commons.enumeration;

/**
 * 事件追踪接口类型枚举类
 * @author binLee
 *
 */
public enum ApiTypes {
	
	/**
	 * 话题演化
	 */
	TOPICEVOLUTION("/api/event/topics/derivative", "" , "80" , 1),
	/**
	 * 热词推荐
	 */
	HOTWORDREC("/api/event/keywords/hot", "80", "80", 2),
	/**
	 * 今日话题 ///api/event/topics/today
	 */
	TODAYTOPICS("/api/event/doclist", "10", "10", 3),
	/**
	 * 情感分析
	 */
	EMOTIONANALYSIS("/api/event/volume/distribution/emotion", "", "", 4),
	/**
	 * 渠道类型
	 */
	CHANNELTYPE("/api/event/volume/distribution/datatype", "", "", 5),
	/**
	 * 报道趋势
	 */
	REPORTTREND("/api/event/volume/trend/report", "", "", 6),
	/**
	 * 媒体报道量排行
	 */
	REPORTSRANK("/api/event/volume/rank/source", "20", "", 7);
	
	
	private String url;     // 接口地址
	private String limit;   // 接口返回条数，个别接口
	private String shownum; // 大屏显示条数，个别接口可用，主要用于数据删除规则
	private Integer type;   // 接口类型的区分，目前用于事件追踪
	
	private ApiTypes(String url, String limit, String shownum, Integer type){
		
		this.url = url;
		this.limit = limit;
		this.shownum = shownum;
		this.type = type;
	}

	public String url() {
		return this.url;
	}
	
	public String limit() {
		return this.limit;
	}
	
	public String shownum() {
		return this.shownum;
	}
	
	public Integer type() {
		return this.type;
	}
}
