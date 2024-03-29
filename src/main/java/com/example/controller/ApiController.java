package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.anotation.ApiCheck;
import com.example.bean.BigScreenEntity;
import com.example.commons.SystemConstant;
import com.example.service.*;
import com.example.util.JsonUtil;
import com.founder.ark.common.utils.bean.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 临时单独更新
 */
@Controller
@RequestMapping("/api")
public class ApiController {
    private static Logger logger = LoggerFactory.getLogger(ApiController.class);

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

    @RequestMapping("/list")
    @ResponseBody
    public ResponseObject list() throws Exception{
        List<Integer> medias = sitenews.queryAllMedias();
        String path = ApiController.class.getClassLoader().getResource("config/api-provider.json").getPath();
        String s = JsonUtil.readJsonFile(path);
        JSONObject json = JSON.parseObject(s);
        json.put("medias", medias);
        logger.info("json: {}", json.toString());
        return ResponseObject.newSuccessResponseObject(json, SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/siteNews")
    @ResponseBody
    @ApiCheck
    public ResponseObject siteNews(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        sitenews.addSiteNews(pageTypeIds, mediaId);
        logger.info("do something ...");
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS.concat(", 完成更新的页面").concat(pageTypeIds.toString()));
    }

    @PostMapping("/keyWordKpi")
    @ResponseBody
    @ApiCheck
    public ResponseObject keyWordKpi(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        keyWordKpiService.addKeyWordKpi(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/keyWordNews")
    @ResponseBody
    @ApiCheck
    public ResponseObject keyWordNews(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        sitenews.addKeyWordNews(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/hotNews")
    @ResponseBody
    @ApiCheck
    public ResponseObject hotNews(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        sitenews.addHotNews(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/originalNews")
    @ResponseBody
    @ApiCheck
    public ResponseObject originalNews(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        sitenews.addOriginalNews(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/emotion")
    @ResponseBody
    @ApiCheck
    public ResponseObject emotion(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        emotionService.addEmotion(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/zlmtNews")
    @ResponseBody
    public ResponseObject zlmtNews(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        sitenews.addZlmtNews(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/articlePress")
    @ResponseBody
    @ApiCheck
    public ResponseObject articlePress(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        ArticlePress.addArticlePress(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/spreadMap")
    @ResponseBody
    @ApiCheck
    public ResponseObject spreadMap(String mediaId, @RequestParam("pageTypeIds") List<String> pageTypeIds) throws Exception {
        SpreadMap.addSpreadMap(pageTypeIds, mediaId);
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

    @PostMapping("/eventTrack")
    @ResponseBody
    public ResponseObject eventTrack() throws Exception {
        eventTrack.getEvenIdByMcode();
        eventTrack.saveTopicEvolution();
        eventTrack.saveHotWordsRec();
        eventTrack.saveTodayTopics();
        eventTrack.saveEmotionAnalysis();
        eventTrack.saveChannelType();
        eventTrack.saveReportTrend();
        eventTrack.saveReportsRank();
        return ResponseObject.newSuccessResponseObject(new JSONObject(), SystemConstant.REQ_SUCCESS);
    }

}
