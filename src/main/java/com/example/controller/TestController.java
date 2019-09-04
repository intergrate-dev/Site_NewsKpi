package com.example.controller;

import java.text.SimpleDateFormat;

import com.example.bean.TokenEntity;
import com.example.commons.TokenGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bean.BigScreenEntity;
import com.example.service.ArticlePressService;
import com.example.service.EmotionService;
import com.example.service.EventTrackService;
import com.example.service.KeyWordKpiService;
import com.example.service.LiangHuiNewsService;
import com.example.service.SiteKpiService;
import com.example.service.SiteNewsService;
import com.example.service.SpreadMapService;

@Controller
@RequestMapping("/news")
public class TestController {
    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private LiangHuiNewsService liangHuiNewsService;
    @Autowired
    private SiteNewsService sitenews;
    @Autowired
    private SiteKpiService KpiService;
    @Autowired
    private ArticlePressService ArticlePress;
    @Autowired
    private BigScreenEntity BSEntity;
    @Autowired
    private EmotionService emotionService;
    @Autowired
    private KeyWordKpiService keyWordKpiService;
    @Autowired
    private SiteKpiService SiteKpiService;
    @Autowired
    private EventTrackService eventTrack;
    @Autowired
    private SpreadMapService SpreadMap;
    @Autowired
    TokenGet tokenGet;

    @RequestMapping("/test")
    public @ResponseBody
    String test1() throws Exception {
        TokenEntity token = tokenGet.getToken();
        logger.info("========= access_token: {} ==================", token.getTK_TOKEN());
        /*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sitenews.addSiteNews(null, null);*/
        return token.getTK_TOKEN();
    }

    @RequestMapping("/KeyWordKpi")
    public @ResponseBody
    String test4() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        keyWordKpiService.addKeyWordKpi(null, null);
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/KeyWord")
    public @ResponseBody
    String test2() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        sitenews.addKeyWordNews(null, null);
        //sitenews.addSiteNews();
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/HotNews")
    public @ResponseBody
    String test5() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        sitenews.addHotNews(null, null);
        //sitenews.addSiteNews();
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/emotion")
    public @ResponseBody
    String test3() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        emotionService.addEmotion(null, null);
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/kpi")
    public @ResponseBody
    String kpi() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        //KpiService.addkpi("-784752488", "news", "中工网", "26229", "7天");
        //KpiService.addWebWord();
        //SiteKpiService.addWebWord();
        ArticlePress.addArticlePress(null, null);
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/eventTrack")
    public @ResponseBody
    String eventTrack() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        eventTrack.getEvenIdByMcode();
        eventTrack.saveTopicEvolution();
        eventTrack.saveHotWordsRec();
        eventTrack.saveTodayTopics();
        eventTrack.saveEmotionAnalysis();
        eventTrack.saveChannelType();
        eventTrack.saveReportTrend();
        eventTrack.saveReportsRank();
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/SpreadMap")
    public @ResponseBody
    String SpreadMap() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        SpreadMap.addSpreadMap(null, null);
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

    @RequestMapping("/sitekpi")
    public @ResponseBody
    String sitekpi() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()) + "开始");
        SiteKpiService.addWebWord();
        System.out.println(df.format(System.currentTimeMillis()) + "结束");
        return "haha";
    }

}
