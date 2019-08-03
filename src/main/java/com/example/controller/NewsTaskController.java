package com.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.service.ArticlePressService;
import com.example.service.EmotionService;
import com.example.service.EventTrackService;
import com.example.service.KeyWordKpiService;
import com.example.service.LiangHuiNewsService;
import com.example.service.SiteKpiService;
import com.example.service.SiteNewsService;
import com.example.service.SpreadMapService;

//@Component
public class NewsTaskController {
	private final Logger log = LoggerFactory.getLogger(NewsTaskController.class);
	@Autowired
	private SiteKpiService SiteKpi;
	@Autowired
	private SiteNewsService SiteNews;
	@Autowired
	private SpreadMapService SpreadMap;
	@Autowired
	private EventTrackService eventTrack;
	@Autowired
	private LiangHuiNewsService liangHuiNewsService;
	@Autowired
	private ArticlePressService ArticlePress;
	@Autowired
	private EmotionService emotionService;
	@Autowired
	private KeyWordKpiService keyWordKpiService;
	

	@Scheduled(cron="0 10 6-23 * * *")
	@Async
	public void LiangHuiNews(){
		try{
			log.info("====================两会新闻开始更新====================");
			//liangHuiNewsService.addLHNews();
			//liangHuiNewsService.addXinMTNews();
			//liangHuiNewsService.addSourceNews();
			log.info("====================两会新闻已更新=====================");
		}catch(Exception e){
			log.error("两会新闻更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 15 6-23 * * *")
	@Async
	public void LiangHuiKpi(){
		try{
			log.info("====================两会传播分析开始更新====================");
			//liangHuiNewsService.addLHKeyWordKpi();
			log.info("====================两会已更新=====================");
		}catch(Exception e){
			log.error("两会传播更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 15 6-23/3 * * *")
	@Async
	public void SpreadMap(){
		try{
			log.info("====================传播地图开始更新====================");
			SpreadMap.addSpreadMap(null, null);
			log.info("====================传播地图已更新=====================");
		}catch(Exception e){
			log.error("传播地图更新失败",e);
			return;
		}
	}
	
	@Scheduled(cron="0 10 6-22/4 * * *")
	@Async
	public void ArticlePress(){
		try{			
			log.info("======================文章传播分析数据开始更新====================");
			ArticlePress.addArticlePress(null, null);
			log.info("======================文章传播分析数据已更新=====================");
		}catch(Exception e){
			log.error("文章传播分析更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 40 6,9,11,15,18,21 * * *")
	@Async
	public void SiteKpi(){
		try{
			log.info("======================传播分析数据开始更新====================");
			SiteKpi.addWebWord();
			log.info("======================传播分析数据已更新=====================");
		}catch(Exception e){
			log.error("传播分析更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 15 6,8,11,14,17,20,23 * * *")
	@Async
	public void CommonSiteNews(){
		try{
			log.info("======================站点新闻开始更新=====================");
			SiteNews.addSiteNews(null, null);
			log.info("======================站点新闻已更新======================");
		}catch(Exception e){
			log.error("站点新闻更新失败",e);
			return;
		}
	}
	
	@Scheduled(cron="0 05 7,10,13,16,19,22 * * *")
	@Async
	public void SiteNews(){
		try{
			log.info("======================站点热点、原创新闻开始更新=====================");
			SiteNews.addHotNews(null, null);
			SiteNews.addOriginalNews(null, null);
			log.info("======================站点热点、原创新闻开始更新======================");
		}catch(Exception e){
			log.error("站点新闻更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 35 7,10,13,16,19,22 * * *")
	@Async
	public void KeyWordNews(){
		try{
			log.info("======================站点关键词新闻开始更新=====================");
			SiteNews.addKeyWordNews(null, null);
			log.info("======================站点关键词新闻开始更新======================");
		}catch(Exception e){
			log.error("站点新闻更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 05 7,10,13,16,19,22 * * *")
	@Async
	public void addZlmtNews(){
		try{
			log.info("======================站点关键词新闻开始更新=====================");
			SiteNews.addZlmtNews(null, null);
			log.info("======================站点关键词新闻开始更新======================");
		}catch(Exception e){
			log.error("站点新闻更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 40 7,9,11,13,15,17,19,21,23 * * *")
	@Async
	public void addEmotion(){
		try{
			log.info("======================情感分析开始更新=====================");
			emotionService.addEmotion(null, null);;
			log.info("======================情感分析开始更新======================");
		}catch(Exception e){
			log.error("站点新闻更新失败",e);
			return;
		}
	}
	@Scheduled(cron="0 25 6,10,14,18,22 * * *")
	@Async
	public void addKeyWordKpi(){
		try{
			log.info("======================情感分析开始更新=====================");
			keyWordKpiService.addKeyWordKpi(null, null);
			log.info("======================情感分析开始更新======================");
		}catch(Exception e){
			log.error("站点新闻更新失败",e);
			return;
		}
	}
	/**
	 * 事件追踪处理任务
	 */
	@Scheduled(cron="0 30 6-23/3 * * *")
	@Async
	public void EventTrack(){
		try{
			log.info("======================事件追踪开始更新=====================");
			eventTrack.getEvenIdByMcode();
			eventTrack.saveTopicEvolution();
			eventTrack.saveHotWordsRec();
			eventTrack.saveTodayTopics();
			eventTrack.saveEmotionAnalysis();
			eventTrack.saveChannelType();
			eventTrack.saveReportTrend();
			eventTrack.saveReportsRank();
			log.info("======================事件追踪已更新=======================");
		}catch(Exception e){
			log.error("事件追踪更新失败",e);
			return;
		}
	}
}
