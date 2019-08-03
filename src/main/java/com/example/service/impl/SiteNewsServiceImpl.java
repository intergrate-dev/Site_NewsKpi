package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.example.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.bean.BigScreenEntity;
import com.example.bean.OperationEntity;
import com.example.bean.SiteNewsEntity;
import com.example.bean.TokenEntity;
import com.example.commons.DateHelper;
import com.example.commons.TokenGet;
import com.example.dao.GetTableIdDao;
import com.example.dao.SiteNewsDao;
import com.example.http.HttpAPIService;
import com.example.service.SiteNewsService;

@Service
public class SiteNewsServiceImpl implements SiteNewsService {
    private final Logger log = LoggerFactory
            .getLogger(SiteNewsServiceImpl.class);
    @Autowired
    private SiteNewsDao siteNewsDao;
    @Autowired
    private GetTableIdDao getTableIdDao;
    @Autowired
    private TokenGet tokenGet;
    @Autowired
    private BigScreenEntity BSEntity;
    @Resource
    private HttpAPIService httpAPIService;

    private static final String HOT_PAGESIZE = "6"; // 热点新闻返回数据量，orig=3||4
    private static final String ORIGINAL_PAGESIZE = "8"; // 原创新闻返回数据量
    private static final String COMMON_PAGESIZE = "15"; // 一般新闻返回数据量

    private static JSONObject dataJson = new JSONObject();

    /**
     * 站点新闻更新</br> 主要包含：网站监控、网站竞媒对比、微信监控、微信竞媒、微博监控、微博竞媒、App监控、App竞媒
     */
    @Override
    public void addSiteNews(List<String> pageTypeIds, String mediaId) {

        // 获取当前的时间，作为删除旧数据的时间节点
        String currentTime = DateHelper.parse2String(new Date(),
                DateHelper.DateFormat.YMDHMS);
        log.info("[站点新闻-普通新闻]，删除数据时间节点：" + currentTime);
        String pTypeIds = "14,15,16,17,18,20,23,24,49,64,65,81,82,83";
        pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
        List<OperationEntity> opList = siteNewsDao.selectSiteNewsId(pTypeIds, mediaId);
        JSONArray isGeten = new JSONArray();
        String[] ptIDs = {"14", "15", "16", "17", "18", "20", "23", "24",
                "49", "64", "65", "81", "82", "83"};
        List<String> ptList = Arrays.asList(ptIDs);
        if (opList != null && opList.size() > 0) {
            String pageTypeID = "";
            String SITE_ID = "";
            String SITE_TYPE = "";
            Map<String, Integer> resultMap = new HashMap<String, Integer>();
            for (OperationEntity oper : opList) {
                pageTypeID = oper.getPageTypeID();
                SITE_ID = oper.getSITE_ID();
                SITE_TYPE = oper.getSITE_TYPE();
                if (ptList.contains(pageTypeID)) {
                    String sType = "";
                    if ("14".equals(pageTypeID) || "18".equals(pageTypeID)) {
                        sType = "news";
                    } else if ("17".equals(pageTypeID)
                            || "24".equals(pageTypeID)
                            || "64".equals(pageTypeID)
                            || "65".equals(pageTypeID) || "82".equals(pageTypeID)) {
                        sType = "wechat";
                    } else if ("16".equals(pageTypeID)
                            || "23".equals(pageTypeID) || "81".equals(pageTypeID)) {
                        sType = "weibo";
                    } else if ("15".equals(pageTypeID)
                            || "20".equals(pageTypeID) || "83".equals(pageTypeID)) {
                        sType = "app";
                    }
                    String[] sid = SITE_ID.split(",");
                    String[] TYPEs = SITE_TYPE.split(",");
                    for (int m = 0; m < sid.length; m++) {
                        if (isGeten != null && isGeten.contains(sid[m])) {
                            continue;
                        }
                        if ("49".equals(pageTypeID)) {
                            sType = TYPEs[m].toLowerCase();
                        }
                        int successNum = insertNews(sid[m], sType, pageTypeID,
                                0);
                        if (successNum > 0) {
                            clearData(sid[m], 0, currentTime, successNum);
                        }
                        resultMap.put(sid[m], successNum);
                        isGeten.add(sid[m]);
                    }
                }
            }
        } else {
            return;
        }
    }

    /**
     * 原创新闻</br> 页面： 影响力展示 id:11
     */
    @Override
    public void addOriginalNews(List<String> pageTypeIds, String mediaId) {
        // 获取当前的时间，作为删除旧数据的时间节点
        String currentTime = DateHelper.parse2String(new Date(),
                DateHelper.DateFormat.YMDHMS);
        log.info("[站点新闻-原创新闻]，删除数据时间节点：" + currentTime);
        String pTypeIds = "11,79";
        pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
        List<OperationEntity> opList = siteNewsDao.selectSiteNewsId(pTypeIds, mediaId);
        if (opList != null && opList.size() > 0) {
            String pageTypeID = "";
            // Map<String, Integer> resultMap = new HashMap<String, Integer>();
            List<String> siteList = new ArrayList<String>();
            for (OperationEntity oper : opList) {
                pageTypeID = oper.getPageTypeID();
                // if("90".equals(oper.getMEDIA_ID())){
                if ("11".equals(pageTypeID)) {
                    siteList = insertOriNews(oper.getSITE_ID(),
                            oper.getSITE_TYPE(), 1, pageTypeID, currentTime,
                            siteList);
                } else if ("79".equals(pageTypeID)) {

                    siteList = insertOriNews(oper.getSITE_ID(),
                            oper.getSITE_TYPE(), 1, pageTypeID, currentTime,
                            siteList);
                    insertOriNews(oper.getSITE_ID(), oper.getSITE_TYPE(), 2,
                            pageTypeID, currentTime, siteList);
                    // 按照站点执行删除操作
                    /*
                     * if(resultMap.size()>0){ for(Map.Entry<String, Integer>
                     * entry : resultMap.entrySet()){ clearData(entry.getKey(),
                     * 1, currentTime, entry.getValue()); } }
                     */
                }
            }
        } else {
            return;
        }

    }

    /**
     * 今日热点、本周热点</br> 页面：APP监控 id:15 页面： 微博监控 id:16 页面： 微信监控 id:17
     */
    @Override
    public void addHotNews(List<String> pageTypeIds, String mediaId) {
        // 获取当前的时间，作为删除旧数据的时间节点
        String currentTime = DateHelper.parse2String(new Date(),
                DateHelper.DateFormat.YMDHMS);
        log.info("[站点新闻-热点新闻]，删除数据时间节点：" + currentTime);
        String pTypeIds = "14,15,16,17";
        pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
        List<OperationEntity> opList = siteNewsDao.selectSiteNewsId(pTypeIds, mediaId);
        // TODO
        List<String> isGeten = new ArrayList<String>();
        if (opList != null && opList.size() > 0) {
            String pageTypeID = "";
            String SITE_ID = "";

            for (OperationEntity oper : opList) {
                pageTypeID = oper.getPageTypeID();
                SITE_ID = oper.getSITE_ID();
                if ("15".equals(pageTypeID) || "14".equals(pageTypeID)
                        || "16".equals(pageTypeID) || "17".equals(pageTypeID)) {
                    String[] sid = SITE_ID.split(",");
                    String sType = "";
                    if ("15".equals(pageTypeID)) {
                        sType = "app";
                    } else if ("14".equals(pageTypeID)) {
                        sType = "news";
                    } else if ("16".equals(pageTypeID)) {
                        sType = "weibo";
                    } else if ("17".equals(pageTypeID)) {
                        sType = "wechat";
                    }
                    for (int m = 0; m < sid.length; m++) {
                        if (isGeten != null && isGeten.contains(sid[m])) {
                            continue;
                        }
                        int success_today = insertHotNews(sid[m], sType,
                                pageTypeID, 3); // 今日热点
                        if (success_today > 0) {
                            clearData(sid[m], 3, currentTime, success_today);
                        }
                        int success_week = insertHotNews(sid[m], sType,
                                pageTypeID, 4); // 本周热点
                        if (success_week > 0) {
                            clearData(sid[m], 4, currentTime, success_week);
                        }
                        isGeten.add(sid[m]);
                    }
                }
            }

            /*
             * // 删除今日热点旧数据，按照站点执行删除操作 if(resultMap_today.size()>0){
             * for(Map.Entry<String, Integer> entry :
             * resultMap_today.entrySet()){ clearData(entry.getKey(), 3,
             * currentTime, entry.getValue()); } } // 删除本周热点旧数据，按照站点执行删除操作
             * if(resultMap_week.size()>0){ for(Map.Entry<String, Integer> entry
             * : resultMap_week.entrySet()){ clearData(entry.getKey(), 4,
             * currentTime, entry.getValue()); } }
             */
        } else {
            return;
        }
    }

    /**
     * 添加关键词新闻，主要用于：领导人活动关键词、区县新闻关键词
     */
    @Override
    public void addKeyWordNews(List<String> pageTypeIds, String mediaId) {

        // 获取当前的时间，作为删除旧数据的时间节点
        String currentTime = DateHelper.parse2String(new Date(),
                DateHelper.DateFormat.YMDHMS);
        log.info("[站点新闻-关键词]，删除数据时间节点：" + currentTime);
        String pTypeIds = "39,45,72";
        pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
        List<OperationEntity> opList = siteNewsDao.selectSiteNewsId(pTypeIds, mediaId);
        List<String> kwList = new ArrayList<String>();
        if (opList != null && opList.size() > 0) {
            for (OperationEntity oper : opList) {
                String pageTypeID = oper.getPageTypeID();
                String maidid = oper.getMEDIA_ID();
                if ("39".equals(pageTypeID)) {
                    // 获取区县新闻配置信息
                    List<JSONObject> areaList = siteNewsDao.getAreaNews(maidid);
                    if (areaList != null && areaList.size() > 0) {
                        String areaKw = "";
                        for (JSONObject area : areaList) {
                            if (null != area
                                    && null != area.getString("keyWord")) {
                                areaKw = area.getString("keyWord");
                            }
                            if (!StringUtils.isBlank(areaKw)
                                    && !kwList.contains(areaKw)) {
                                kwList.add(areaKw);
                                insertNewsByFts(area, null, pageTypeID,
                                        currentTime, "", null, null, null);
                            }
                        }
                    }
                } else if ("45".equals(pageTypeID) || "72".equals(pageTypeID)) {
                    // 获取领导活动关键词
                    String[] kws = null;
                    JSONObject extfileds = JSONObject.parseObject(oper
                            .getExtfileds());
                    String leaderSel = extfileds.containsKey("leaderSel") ? extfileds
                            .getString("leaderSel") : null;
                    String selTime = extfileds.containsKey("selTime") ? extfileds
                            .getString("selTime") : null;
                    String config = oper.getConfigure();
                    if ("72".equals(pageTypeID) && "90".equals(maidid)) {
                        String keyword = extfileds.containsKey("KeyWord") ? extfileds
                                .getString("KeyWord") : config;
                        kws = keyword.split(",");
                        // kws = config.split(",");
                    } else {
                        if (null != config && !config.isEmpty()) {
                            kws = config.split(",");
                        }
                    }
                    if (null != kws && kws.length > 0) {
                        String fileds = extfileds.containsKey("removeWord") ? extfileds
                                .getString("removeWord") : "";
                        for (String kw : kws) {
                            if (null != kw && !kwList.contains(kw)) {
                                kwList.add(kw);
                                // 关键词数组中不包含当前关键词，则需要调用接口添加数据
                                insertNewsByFts(null, kw, pageTypeID,
                                        currentTime, fileds, maidid, leaderSel, selTime);
                                /*
                                 * if("90".equals(maidid)&&"72".equals(pageTypeID
                                 * )){ insertxingsha(maidid,kw,pageTypeID,
                                 * currentTime,fileds); }
                                 */
                            }
                        }
                    }
                } else if ("74".equals(pageTypeID) || "75".equals(pageTypeID)) {
                    //
                    // String config = oper.getConfigure();
                    JSONObject extfileds = JSONObject.parseObject(oper
                            .getExtfileds());
                    if (null != extfileds && !extfileds.isEmpty()) {
                        String keyword = extfileds.getString("KeyWord");// .replaceAll(",",
                        // "|");
                        String[] kws = keyword.split(",");
                        if (null != kws && kws.length > 0) {
                            String fileds = extfileds.containsKey("removeWord") ? extfileds
                                    .getString("removeWord") : "";
                            for (String kw : kws) {
                                if (null != kw && !kwList.contains(kw)) {
                                    kwList.add(kw);
                                    // 关键词数组中不包含当前关键词，则需要调用接口添加数据
                                    insertNewsByFts(null, kw, pageTypeID,
                                            currentTime, fileds, maidid, null, null);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    /**
     * 主流媒体报道
     */
    @Override
    public void addZlmtNews(List<String> pageTypeIds, String mediaId) {
        // 获取当前的时间，作为删除旧数据的时间节点
        // TODO add tag(task json) for monitor
        String currentTime = DateHelper.parse2String(new Date(),
                DateHelper.DateFormat.YMDHMS);
        log.info("[站点新闻-关键词]，删除数据时间节点：" + currentTime);
        String pTypeIds = "45,68,69,70";
        pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
        List<OperationEntity> opList = siteNewsDao.selectSiteNewsId(pTypeIds, mediaId);
        if (opList != null && opList.size() > 0) {
            for (OperationEntity oper : opList) {
                String pageTypeID = oper.getPageTypeID();
                // String SITE_NAME = oper.getSITE_NAME();
                // 获取领导活动关键词
                String config = oper.getConfigure();
                String sites = oper.getSITE_ID();
                String types = oper.getSITE_TYPE();
                String mediaIdV = oper.getMEDIA_ID();
                if (null != config && !config.isEmpty()) {
                    JSONObject extfileds = JSONObject.parseObject(oper
                            .getExtfileds());
                    String fileds = extfileds.containsKey("removeWord") ? extfileds
                            .getString("removeWord") : "";

                    if ("68".equals(pageTypeID) || "69".equals(pageTypeID)
                            || "70".equals(pageTypeID)) {
                        // 关键词数组中不包含当前关键词，则需要调用接口添加数据
                        insertNewsByMadia(mediaIdV, sites, types, config,
                                pageTypeID, currentTime, fileds, null, null);
                    } else if ("45".equals(pageTypeID)) {
                        String leaderSel = extfileds.containsKey("leaderSel") ? extfileds
                                .getString("leaderSel") : null;
                        String selTime = extfileds.containsKey("selTime") ? extfileds
                                .getString("selTime") : null;
                        String[] confs = config.split(",");
                        for (String con : confs) {
                            if (types.contains("NEWS") || types.contains("APP")
                                    || types.contains("WEIBO")
                                    || types.contains("WECHAT")) {
                                insertNewsByMadia(mediaIdV, sites, types, con,
                                        pageTypeID, currentTime, fileds, leaderSel, selTime);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Integer> queryAllMedias() {
        return siteNewsDao.queryAllMedias();
    }

    /**
     * 插入通过全文检索接口返回的全部新闻，主要包括区县新闻、领导关键词新闻
     * @param mediaId
     * @param leaderKw
     * @param pageTypeID
     * @param currentTime
     * @param removeWord
     */
    public void insertxingsha(String mediaId, String leaderKw,
                              String pageTypeID, String currentTime, String removeWord) {
        JSONArray dates = getdata(-30);
        String sites = "-417149749";
        String channelid = "2031035781";
        String types = "app";
        JSONObject sorts = new JSONObject();
        sorts.put("field", "pubdate");
        sorts.put("order", "desc");
        JSONArray sort = new JSONArray();
        sort.add(sorts);
        int success_leaderKw = searchMadiaAndInsert(mediaId, sites, types,
                leaderKw, pageTypeID, dates, sort, removeWord, channelid, null);
        if (success_leaderKw > 0) {
            clearData(sites + pageTypeID, 0, currentTime, success_leaderKw);
        } else {
            success_leaderKw = searchMadiaAndInsert(mediaId, sites, types,
                    leaderKw, pageTypeID, dates, sort, removeWord, null, null);
            if (success_leaderKw > 0) {
                clearData(sites + pageTypeID, 0, currentTime, success_leaderKw);
            }
        }
    }

    /**
     * 插入通过全文检索接口返回的全部新闻，主要包括区县新闻、领导关键词新闻
     * @param mediaId
     * @param sites
     * @param types
     * @param leaderKw
     * @param pageTypeID
     * @param currentTime
     * @param removeWord
     * @param leaderSel
     * @param selTime
     */
    public void insertNewsByMadia(String mediaId, String sites, String types,
                                  String leaderKw, String pageTypeID, String currentTime,
                                  String removeWord, String leaderSel, String selTime) {
        JSONArray dates = null;
        if (selTime != null && !"".equals(selTime) && "7D".equals(selTime)) {
            dates = getdata(-7);
        } else {
            dates = getdata(-30);
        }
        JSONObject sorts = new JSONObject();
        sorts.put("field", "pubdate");
        sorts.put("order", "desc");
        JSONArray sort = new JSONArray();
        sort.add(sorts);
        int success_leaderKw = 0;
        if (mediaId.equals("90")) {
            if ("68".equals(pageTypeID) || "69".equals(pageTypeID)
                    || "70".equals(pageTypeID)) {
                // 长沙个性化
                success_leaderKw = searchChangShaXian(mediaId, sites, types,
                        leaderKw, pageTypeID, dates, sort, removeWord);
            } else {
                success_leaderKw = searchMadiaAndInsert(mediaId, sites, types,
                        leaderKw, pageTypeID, dates, sort, removeWord, null, leaderSel);
            }
        } else {
            success_leaderKw = searchMadiaAndInsert(mediaId, sites, types,
                    leaderKw, pageTypeID, dates, sort, removeWord, null, leaderSel);
        }
        // 领导人活动关键词

        if (success_leaderKw > 0) {
            clearData(mediaId + leaderKw + pageTypeID, 0, currentTime,
                    success_leaderKw);
        }
    }

    public int searchChangShaXian(String mediaId, String sites, String types,
                                  String leaderKw, String pageTypeID, JSONArray dates,
                                  JSONArray sort, String removeWord) {
        JSONArray siteids = new JSONArray();
        JSONArray wechatid = new JSONArray();
        JSONArray weiboid = new JSONArray();
        String[] sids = sites.split(",");
        String[] stypes = types.split(",");
        for (int i = 0; i < sids.length; i++) {
            String stype = stypes[i].toLowerCase();
            if ("news".equals(stype) || "app".equals(stype)
                    || "epaper".equals(stype)) {
                siteids.add(sids[i]);
            } else if ("wechat".equals(stype)) {
                wechatid.add(sids[i]);
            } else if ("weibo".equals(stype)) {
                weiboid.add(sids[i]);
            }
        }
        int successNum = 0;
        String keyWord = "";
        String qxName = "";
        String outWord = "";
        if (!"".endsWith(removeWord)) {
            outWord = removeWord;
        }
        keyWord = leaderKw;
        // String mediaId = area.getString("mediaId");
        JSONObject conditions = new JSONObject();
        JSONArray typejson = new JSONArray();
        if (outWord != null && !"".equals(outWord)) {
            String[] outWords = outWord.split(",");
            for (String n : outWords) {
                typejson.add(n);
            }
        }
        if (typejson.size() > 0) {
            conditions.put("title_not", typejson);
        }
        String title = "";
        JSONArray titleJs = new JSONArray();
        if (keyWord.contains("and")) {
            title = keyWord.replaceAll("and", " ");
            titleJs.add(title);
        } else {
            if (keyWord.contains("|")) {
                String[] nam = keyWord.split("\\|");
                for (String n : nam) {
                    titleJs.add(n);
                }
            } else {
                titleJs.add(keyWord);
            }
        }

        JSONArray datatype = new JSONArray();
        datatype.add("news");
        datatype.add("app");
        datatype.add("wechat");
        datatype.add("weibo");
        datatype.add("epaper");
        /*
         * JSONArray Rank = new JSONArray(); Rank.add(1); Rank.add(2);
         */
        if (siteids != null && siteids.size() > 0) {
            conditions.put("sites", siteids);
        }
        if (wechatid != null && wechatid.size() > 0) {
            conditions.put("wechatuid", wechatid);
        }
        if (weiboid != null && weiboid.size() > 0) {
            conditions.put("weibouid", weiboid);
        }
        conditions.put("datatype", datatype);
        conditions.put("pubtime", dates);
        conditions.put("title", titleJs);
        conditions.put("duplicate", "1");

        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl() + "/api/fts";
        int ishongwang = 0;
        int total = 0;
        JSONArray layin = new JSONArray();
        for (int m = 1; m < 10; m++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", token.getTK_TOKEN());
            map.put("pagesize", ORIGINAL_PAGESIZE);
            map.put("page", String.valueOf(m));
            map.put("conditions", conditions.toString());
            map.put("sorts", sort.toString());
            String tokens = httpAPIService.doPost(url, map);
            if (!StringUtils.isBlank(tokens)) {
                JSONObject json = JSONObject.parseObject(tokens);
                JSONArray documents = json.getJSONArray("documents");
                if (documents != null && documents.size() > 0) {
                    total = json.getIntValue("total");
                    log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果:"
                            + documents.size());
                    for (Object obj1 : documents) {
                        JSONObject jsonobj = JSONObject.parseObject(obj1
                                .toString());
                        String source = jsonobj.getString("source");
                        if (layin.size() < 15) {
                            if (source != null && source.contains("红网")) {
                                ishongwang = ishongwang + 1;
                                if (ishongwang < 6) {
                                    layin.add(jsonobj);
                                }
                            } else {
                                layin.add(jsonobj);
                            }
                        }
                    }

                } else {
                    log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果为空:"
                            + tokens + "，当前token=" + token.getTK_TOKEN());
                    break;
                }
            }
            if (layin.size() >= 15) {
                break;
            }
        }
        if (layin.size() > 0) {
            for (Object lay : layin) {
                JSONObject jsonobj = JSONObject.parseObject(lay.toString());
                String id = jsonobj.getString("id");
                SiteNewsEntity kpiEntity = new SiteNewsEntity();
                kpiEntity.setSYS_DELETEFLAG(0);
                Date date = new Date();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = format.format(date);
                kpiEntity.setSN_LASTMODIFIED(time);
                kpiEntity.setSN_ID(id);
                String titles = jsonobj.getString("title");
                if (titles == null || "".equals(titles)) {
                    continue;
                }
                String centent = filterEmoji(titles, "*");
                if (!isChinaWord(centent)) {
                    continue;
                }
                title = centent;
                kpiEntity.setSN_TITLE(titles);
                kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                String summary = jsonobj.getString("summary");
                if (summary != null && !"".equals(summary)) {
                    String summarys = filterEmoji(summary, "*");
                    if (!isChinaWord(summarys)) {
                        continue;
                    }
                    summary = summarys;
                    kpiEntity.setSN_SUMMARY(summary);
                }
                String words = jsonobj.getString("keywords");
                if (words != null && !"".equals(words)) {
                    String EmojiWord = filterEmoji(words, "*");
                    if (!isChinaWord(EmojiWord)) {
                        continue;
                    }
                    if (EmojiWord != null && EmojiWord.length() > 254) {
                        EmojiWord = EmojiWord.substring(0, 254);
                    }
                    kpiEntity.setSN_KEYWORDS(EmojiWord);
                }
                if (jsonobj.getString("dataType") != null
                        && "wechat".equals(jsonobj.getString("dataType"))) {
                    kpiEntity
                            .setSN_SOURCE(jsonobj.getString("channel") + "-微信");
                } else {
                    kpiEntity.setSN_SOURCE(jsonobj.getString("source"));
                }

                kpiEntity.setSN_SOURCEID(jsonobj.getString("sourceId"));
                kpiEntity.setSN_CHANNEL(jsonobj.getString("channel"));
                kpiEntity.setSN_CHANNELID(jsonobj.getString("channelId"));
                kpiEntity.setSN_VISITCOUNT(jsonobj.getIntValue("visitCount"));
                kpiEntity.setSN_REBACKCOUNT(jsonobj.getIntValue("rebackCount"));
                kpiEntity.setSN_LIKECOUNT(jsonobj.getIntValue("likeCount"));
                kpiEntity.setSN_PRESSCOUNT(total);
                kpiEntity.setSN_FORWARDCOUNT(jsonobj
                        .getIntValue("forwardCount"));
                kpiEntity.setSN_DATATYPE(jsonobj.getString("dataType"));
                kpiEntity.setSN_AUTHOR(jsonobj.getString("author"));
                kpiEntity.setSN_ORIGINAL(0);
                kpiEntity.setSN_ISHOW(1);
                kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                kpiEntity.setSN_SITETERM(mediaId + keyWord + pageTypeID);
                String con = getNews(jsonobj.getString("id"));
                kpiEntity.setSN_DOCUMENT(con);
                int num = siteNewsDao.addSiteNews(kpiEntity);
                if (num == 1) {
                    dataJson.put(id, jsonobj.getString("title"));
                    successNum++;
                } else {
                    log.error("站点[" + titleJs.toString()
                            + "]-关键词新闻，添加失败！title=" + kpiEntity.getSN_TITLE());
                }
            }
        }

        return successNum;
    }

    /**
     * 通过关键词检索新闻，并插入数据库
     * @param mediaId
     * @param sites
     * @param types
     * @param leaderKw
     * @param pageTypeID
     * @param dates
     * @param sort
     * @param removeWord
     * @param channelid
     * @param leaderSel
     * @return
     */
    public int searchMadiaAndInsert(String mediaId, String sites, String types,
                                    String leaderKw, String pageTypeID, JSONArray dates,
                                    JSONArray sort, String removeWord, String channelid, String leaderSel) {
        JSONArray siteids = new JSONArray();
        JSONArray wechatid = new JSONArray();
        JSONArray weiboid = new JSONArray();
        String[] sids = sites.split(",");
        String[] stypes = types.split(",");
        for (int i = 0; i < sids.length; i++) {
            String stype = stypes[i].toLowerCase();
            if ("news".equals(stype) || "app".equals(stype)
                    || "epaper".equals(stype)) {
                siteids.add(sids[i]);
            } else if ("wechat".equals(stype)) {
                wechatid.add(sids[i]);
            } else if ("weibo".equals(stype)) {
                weiboid.add(sids[i]);
            }

        }
        int successNum = 0;
        String keyWord = "";
        String qxName = "";
        String outWord = "";

        if (!"".endsWith(removeWord)) {
            outWord = removeWord;
        }
        keyWord = leaderKw;
        // String mediaId = area.getString("mediaId");

        JSONObject conditions = new JSONObject();

        JSONArray typejson = new JSONArray();
        if (outWord != null && !"".equals(outWord)) {
            String[] outWords = outWord.split(",");
            for (String n : outWords) {
                typejson.add(n);
            }
        }

        String title = "";
        JSONArray titleJs = new JSONArray();
        if (keyWord.contains("and")) {
            title = keyWord.replaceAll("and", " ");
            titleJs.add(title);
        } else {
            if (keyWord.contains("|")) {
                String[] nam = keyWord.split("\\|");
                for (String n : nam) {
                    titleJs.add(n);
                }
            } else {
                titleJs.add(keyWord);
            }
        }

        JSONArray datatype = new JSONArray();
        datatype.add("news");
        datatype.add("app");
        datatype.add("wechat");
        datatype.add("weibo");
        datatype.add("epaper");
        /*
         * JSONArray Rank = new JSONArray(); Rank.add(1); Rank.add(2);
         */
        if (siteids != null && siteids.size() > 0) {
            conditions.put("sites", siteids);
        }
        if (wechatid != null && wechatid.size() > 0) {
            conditions.put("wechatuid", wechatid);
        }
        if (weiboid != null && weiboid.size() > 0) {
            conditions.put("weibouid", weiboid);
        }
        if (channelid != null) {
            JSONArray channelids = new JSONArray();
            channelids.add(channelid);
            conditions.put("channels", channelids);
        }
        conditions.put("datatype", datatype);
        conditions.put("pubtime", dates);
        if ("45".equals(pageTypeID)) {
            if (leaderSel != null && !"".equals(leaderSel)) {
                if ("2".equals(leaderSel)) {
                    conditions.put("content", titleJs);
                    if (typejson.size() > 0) {
                        conditions.put("content_not", typejson);
                    }
                } else if ("3".equals(leaderSel)) {
                    conditions.put("title_content", titleJs);
                    if (typejson.size() > 0) {
                        conditions.put("title_content_not", typejson);
                    }
                } else {
                    if (typejson.size() > 0) {
                        conditions.put("title_not", typejson);
                    }
                    conditions.put("title", titleJs);
                }
            } else {
                conditions.put("title", titleJs);
                if (typejson.size() > 0) {
                    conditions.put("title_not", typejson);
                }
            }
        } else {
            conditions.put("title", titleJs);
            if (typejson.size() > 0) {
                conditions.put("title_not", typejson);
            }
        }

        conditions.put("duplicate", "1");
        /*
         * conditions.put("sourceRank", Rank);
         * conditions.put("channelRank",Rank);
         */

        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl() + "/api/fts";
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", token.getTK_TOKEN());
        if ("72".equals(pageTypeID)) {
            map.put("pagesize", "2");
        } else {
            map.put("pagesize", ORIGINAL_PAGESIZE);
        }

        map.put("conditions", conditions.toString());
        map.put("sorts", sort.toString());
        String tokens = httpAPIService.doPost(url, map);

        if (!StringUtils.isBlank(tokens)) {
            JSONObject json = JSONObject.parseObject(tokens);
            JSONArray documents = json.getJSONArray("documents");
            if (documents != null && documents.size() > 0) {
                int total = json.getIntValue("total");
                log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果:"
                        + documents.size());
                // TODO JOIN(START)
                // TODO TRY_CATCH, WHEN EXCEPTION OCCUR JOIN(WARN)
                for (Object obj : documents) {
                    JSONObject jsonobj = JSONObject.parseObject(obj.toString());
                    String id = jsonobj.getString("id");
                    SiteNewsEntity kpiEntity = new SiteNewsEntity();
                    kpiEntity.setSYS_DELETEFLAG(0);
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    String time = format.format(date);
                    kpiEntity.setSN_LASTMODIFIED(time);
                    kpiEntity.setSN_ID(id);
                    String titles = jsonobj.getString("title");
                    if (titles == null || "".equals(titles)) {
                        continue;
                    }
                    String centent = filterEmoji(titles, "*");
                    if (!isChinaWord(centent)) {
                        continue;
                    }
                    title = centent;
                    kpiEntity.setSN_TITLE(titles);
                    kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                    kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                    String summary = jsonobj.getString("summary");
                    if (summary != null && !"".equals(summary)) {
                        String summarys = filterEmoji(summary, "*");
                        if (!isChinaWord(summarys)) {
                            continue;
                        }
                        summary = summarys;
                        kpiEntity.setSN_SUMMARY(summary);
                    }
                    String words = jsonobj.getString("keywords");
                    if (words != null && !"".equals(words)) {
                        String EmojiWord = filterEmoji(words, "*");
                        if (!isChinaWord(EmojiWord)) {
                            continue;
                        }
                        if (EmojiWord != null && EmojiWord.length() > 254) {
                            EmojiWord = EmojiWord.substring(0, 254);
                        }
                        kpiEntity.setSN_KEYWORDS(EmojiWord);
                    }
                    if (jsonobj.getString("dataType") != null
                            && "wechat".equals(jsonobj.getString("dataType"))) {
                        kpiEntity.setSN_SOURCE(jsonobj.getString("channel")
                                + "-微信");
                    } else {
                        kpiEntity.setSN_SOURCE(jsonobj.getString("source"));
                    }

                    kpiEntity.setSN_SOURCEID(jsonobj.getString("sourceId"));
                    kpiEntity.setSN_CHANNEL(jsonobj.getString("channel"));
                    kpiEntity.setSN_CHANNELID(jsonobj.getString("channelId"));
                    kpiEntity.setSN_VISITCOUNT(jsonobj
                            .getIntValue("visitCount"));
                    kpiEntity.setSN_REBACKCOUNT(jsonobj
                            .getIntValue("rebackCount"));
                    kpiEntity.setSN_LIKECOUNT(jsonobj.getIntValue("likeCount"));
                    kpiEntity.setSN_PRESSCOUNT(total);
                    kpiEntity.setSN_FORWARDCOUNT(jsonobj
                            .getIntValue("forwardCount"));
                    kpiEntity.setSN_DATATYPE(jsonobj.getString("dataType"));
                    kpiEntity.setSN_AUTHOR(jsonobj.getString("author"));
                    // String originalPicUrl =
                    /*
                     * if(originalPicUrl!=null && "".equals(originalPicUrl)){
                     * String[] picurls = originalPicUrl.split(";");
                     * kpiEntity.setSN_ORIGINALPICURL(picurls[0]); }
                     */
                    // kpiEntity.setSN_ORIGINALPICURL(jsonobj.getString("originalPicUrl"));

                    kpiEntity.setSN_ORIGINAL(0);
                    kpiEntity.setSN_ISHOW(1);
                    kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                    if ("72".equals(pageTypeID)) {
                        kpiEntity.setSN_SITETERM(sites + pageTypeID);
                    } else {
                        kpiEntity
                                .setSN_SITETERM(mediaId + keyWord + pageTypeID);
                    }

                    String con = getNews(jsonobj.getString("id"));
                    kpiEntity.setSN_DOCUMENT(con);
                    int num = siteNewsDao.addSiteNews(kpiEntity);
                    if (num == 1) {
                        dataJson.put(id, jsonobj.getString("title"));
                        successNum++;
                    } else {
                        log.error("站点[" + titleJs.toString()
                                + "]-关键词新闻，添加失败！title="
                                + kpiEntity.getSN_TITLE());
                    }
                }
            } else {
                log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果为空:"
                        + tokens + "，当前token=" + token.getTK_TOKEN());
            }
        }
        // JOIN(COMPLETE COMMIT)
        return successNum;
    }

    /**
     * 实时清除指定数据
     *
     * @param siteID
     * @param oriType
     * @param taskExeTime
     * @param success
     */
    private void clearData(String siteID, int oriType, String taskExeTime,
                           int success) {

        int deletedNum = 0;
        List<Long> rmDuplicate = new ArrayList<Long>();
        List<Long> dupData = new ArrayList<Long>();
        List<Long> delIds = new ArrayList<Long>();

        int delNum = 0;
        String remainNum = "0";
        if (0 == oriType) { // 一般新闻
            remainNum = COMMON_PAGESIZE;
        } else if (1 == oriType) { // 原创新闻
            remainNum = ORIGINAL_PAGESIZE;
        } else if (3 == oriType || 4 == oriType) {
            remainNum = HOT_PAGESIZE;
        }
        if (success < Integer.parseInt(remainNum)) { // 成功插入数小于规定值
            delNum = Integer.parseInt(remainNum) - success;
        }

        String ids = "";
        if (success > 0) { // 成功插入数据，才执行删除
            if (delNum > 0) {
                // 先查询任务时间点之前的旧数据，进行去重
                List<SiteNewsEntity> snList = siteNewsDao.querySiteOldNews(
                        siteID, oriType, taskExeTime);
                if (null != snList && snList.size() > 0) {
                    for (SiteNewsEntity sn : snList) {
                        if (dataJson.containsKey(sn.getSN_ID())) {
                            delIds.add(sn.getId());
                        } else {
                            rmDuplicate.add(sn.getId());
                        }
                    }

                    try {
                        if (rmDuplicate.size() > 0) {
                            Collections.sort(rmDuplicate,
                                    Collections.reverseOrder());
                            if (delNum >= rmDuplicate.size()) {
                                // 相当于数组长度为0，数据量不足不需要删除
                                delNum = rmDuplicate.size();
                            }
                            log.info("删除时需要保留条数：" + delNum);
                            // 避免出现下标越界
                            if (delNum < 0) {
                                delNum = 0;
                            }
                            dupData = rmDuplicate.subList(delNum,
                                    rmDuplicate.size());
                        }
                    } catch (Exception e) {
                        log.error("清理[站点新闻]数据，去重过程出现异常！");
                        e.printStackTrace();
                        return;
                    }

                    if (dupData.size() > 0) {
                        for (Long id : dupData) {
                            delIds.add(id);
                        }
                    }

                    if (delIds.size() > 0) {
                        for (int i = 0; i < delIds.size(); i++) {
                            if (i == delIds.size() - 1) {
                                ids += delIds.get(i);
                                break;
                            }
                            ids += delIds.get(i) + ",";
                        }

                    }
                }
            }
            // 开始执行清理脚本
            deletedNum = siteNewsDao.deleteNews(siteID, oriType, taskExeTime,
                    ids);
        }
        // 初始化json
        dataJson.clear();
        log.info("清理[站点新闻]数据，类型=" + oriType + "，siteID=[" + siteID + "]成功插入数据："
                + success + "，共删除数据：" + deletedNum);
    }

    /**
     * 插入通过全文检索接口返回的全部新闻，主要包括区县新闻、领导关键词新闻
     * @param areaJson
     * @param leaderKw
     * @param pageTypeID
     * @param currentTime
     * @param removeWord
     * @param maidid
     * @param leaderSel
     * @param selTime
     */
    public void insertNewsByFts(JSONObject areaJson, String leaderKw,
                                String pageTypeID, String currentTime, String removeWord,
                                String maidid, String leaderSel, String selTime) {
        JSONArray dates = null;
        if (selTime != null && !"".equals(selTime) && "7D".equals(selTime)) {
            dates = getdata(-7);
        } else {
            dates = getdata(-30);
        }
        JSONObject sorts = new JSONObject();
        sorts.put("field", "pubdate");
        sorts.put("order", "desc");
        JSONArray sort = new JSONArray();
        sort.add(sorts);
        if (null != areaJson && null == leaderKw) { // 区县新闻
            int success_area = 0;
            success_area = searchFtsAndInsert(areaJson, null, pageTypeID,
                    dates, sort, "", null, null);
            if (success_area > 0) {
                clearData(areaJson.getString("keyWord"), 0, currentTime,
                        success_area);
            }
        } else { // 领导人活动关键词
            int success_leaderKw = searchFtsAndInsert(null, leaderKw,
                    pageTypeID, dates, sort, removeWord, maidid, leaderSel);
            if (success_leaderKw > 0) {
                if ("72".equals(pageTypeID) && "90".equals(maidid)) {
                    leaderKw = "yqrd" + maidid + pageTypeID;
                }
                clearData(leaderKw, 0, currentTime, success_leaderKw);
            }
            // resultMap.put(leaderKw, success_leaderKw);
        }
        // return resultMap;
    }

    /**
     * 通过关键词检索新闻，并插入数据库
     * @param area
     * @param leaderKw
     * @param pageTypeID
     * @param dates
     * @param sort
     * @param removeWord
     * @param maidid
     * @param leaderSel
     * @return
     */
    public int searchFtsAndInsert(JSONObject area, String leaderKw,
                                  String pageTypeID, JSONArray dates, JSONArray sort,
                                  String removeWord, String maidid, String leaderSel) {
        int successNum = 0;
        String keyWord = "";
        String qxName = "";
        String outWord = "";
        if (null != area && null == leaderKw) {
            keyWord = area.getString("keyWord");
            qxName = area.getString("qxName");
            outWord = area.getString("outWord");
        } else {
            if (!"".endsWith(removeWord)) {
                outWord = removeWord;
            }
            keyWord = leaderKw;
        }
        // String mediaId = area.getString("mediaId");

        JSONObject conditions = new JSONObject();
        // if (null != area && null == leaderKw) {
        JSONArray typejson = new JSONArray();
        if (outWord != null && !"".equals(outWord)) {
            String[] outWords = outWord.split(",");
            for (String n : outWords) {
                typejson.add(n);
            }
        }
        if (typejson.size() > 0) {
            conditions.put("title_not", typejson);
        }
        // }

        String title = "";
        JSONArray titleJs = new JSONArray();
        if (keyWord.contains("and")) {
            title = keyWord.replaceAll("and", " ");
            titleJs.add(title);
        } else {
            if (keyWord.contains("|")) {
                String[] nam = keyWord.split("\\|");
                for (String n : nam) {
                    titleJs.add(n);
                }
            } else {
                titleJs.add(keyWord);
            }
        }

        JSONArray datatype = new JSONArray();
        datatype.add("news");
        datatype.add("app");
        datatype.add("wechat");
        datatype.add("epaper");
        /*
         * JSONArray Rank = new JSONArray(); Rank.add(1); Rank.add(2);
         */
        conditions.put("datatype", datatype);
        conditions.put("pubtime", dates);
        if ("72".equals(pageTypeID)) {
            conditions.put("title_content", titleJs);
        } else {
            if (leaderSel != null && !"".equals(leaderSel)) {
                if ("2".equals(leaderSel)) {
                    conditions.put("content", titleJs);
                } else if ("3".equals(leaderSel)) {
                    conditions.put("title_content", titleJs);
                } else {
                    conditions.put("title", titleJs);
                }
            } else {
                conditions.put("title", titleJs);
            }
        }
        conditions.put("duplicate", "1");
        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl() + "/api/fts";
        Map<String, String> map = new HashMap<String, String>();
        // map.put("access_token", token.getTK_TOKEN());
        map.put("pagesize", COMMON_PAGESIZE);
        map.put("conditions", conditions.toString());
        map.put("sorts", sort.toString());
        String tokens = null;
        if ("72".equals(pageTypeID) && "90".equals(maidid)) {
            String responses = httpAPIService.postMap(url, map,
                    token.getTK_TOKEN());
            JSONObject reses = JSONObject.parseObject(responses);
            int code = reses.getIntValue("status");
            if (code == 0) {
                tokens = reses.getString("data");
            }
        } else {
            map.put("access_token", token.getTK_TOKEN());
            tokens = httpAPIService.doPost(url, map);
        }

        if (!StringUtils.isBlank(tokens)) {
            JSONObject json = JSONObject.parseObject(tokens);
            JSONArray documents = json.getJSONArray("documents");
            if (documents != null && documents.size() > 0) {
                log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果:"
                        + documents.size());
                for (Object obj : documents) {
                    JSONObject jsonobj = JSONObject.parseObject(obj.toString());
                    if (jsonobj.getString("channel").equals("未知")) {
                        log.info("================ channel 未知 ============ url: " + url + ", condition: " + conditions.toString() +
                                ", pageTypeId: " + pageTypeID);
                    }
                    String id = jsonobj.getString("id");
                    SiteNewsEntity kpiEntity = new SiteNewsEntity();
                    kpiEntity.setSYS_DELETEFLAG(0);
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    String time = format.format(date);
                    kpiEntity.setSN_LASTMODIFIED(time);

                    kpiEntity.setSN_ID(id);
                    String titles = jsonobj.getString("title");
                    if (titles == null || "".equals(titles)) {
                        continue;
                    }
                    String centent = filterEmoji(titles, "*");
                    if (!isChinaWord(centent)) {
                        continue;
                    }
                    title = centent;
                    kpiEntity.setSN_TITLE(titles);
                    kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                    kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                    String summary = jsonobj.getString("summary");
                    if (summary != null && !"".equals(summary)) {
                        String summarys = filterEmoji(summary, "*");
                        if (!isChinaWord(summarys)) {
                            continue;
                        }
                        summary = summarys;

                        kpiEntity.setSN_SUMMARY(summary);
                    }
                    String words = jsonobj.getString("keywords");
                    if (words != null && !"".equals(words)) {
                        String EmojiWord = filterEmoji(words, "*");
                        if (!isChinaWord(EmojiWord)) {
                            continue;
                        }
                        if (EmojiWord != null && EmojiWord.length() > 254) {
                            EmojiWord = EmojiWord.substring(0, 254);
                        }
                        kpiEntity.setSN_KEYWORDS(EmojiWord);
                    }
                    if (jsonobj.getString("dataType") != null
                            && "wechat".equals(jsonobj.getString("dataType"))) {
                        kpiEntity.setSN_SOURCE(jsonobj.getString("channel")
                                + "-微信");
                    } else {
                        kpiEntity.setSN_SOURCE(jsonobj.getString("source"));
                    }

                    kpiEntity.setSN_SOURCEID(jsonobj.getString("sourceId"));
                    kpiEntity.setSN_CHANNEL(jsonobj.getString("channel"));
                    kpiEntity.setSN_CHANNELID(jsonobj.getString("channelId"));
                    kpiEntity.setSN_VISITCOUNT(jsonobj
                            .getIntValue("visitCount"));
                    kpiEntity.setSN_REBACKCOUNT(jsonobj
                            .getIntValue("rebackCount"));
                    kpiEntity.setSN_LIKECOUNT(jsonobj.getIntValue("likeCount"));
                    kpiEntity.setSN_FORWARDCOUNT(jsonobj
                            .getIntValue("forwardCount"));
                    kpiEntity.setSN_DATATYPE(jsonobj.getString("dataType"));
                    kpiEntity.setSN_AUTHOR(jsonobj.getString("author"));
                    // String originalPicUrl =
                    /*
                     * if(originalPicUrl!=null && "".equals(originalPicUrl)){
                     * String[] picurls = originalPicUrl.split(";");
                     * kpiEntity.setSN_ORIGINALPICURL(picurls[0]); }
                     */
                    // kpiEntity.setSN_ORIGINALPICURL(jsonobj.getString("originalPicUrl"));

                    kpiEntity.setSN_ORIGINAL(0);
                    kpiEntity.setSN_ISHOW(1);
                    kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                    if ("72".equals(pageTypeID) && "90".equals(maidid)) {
                        kpiEntity.setSN_SITETERM("yqrd" + maidid + pageTypeID);
                    } else {
                        kpiEntity.setSN_SITETERM(keyWord);
                    }

                    String con = getNews(jsonobj.getString("id"));
                    kpiEntity.setSN_DOCUMENT(con);
                    int num = siteNewsDao.addSiteNews(kpiEntity);
                    if (num == 1) {
                        dataJson.put(id, jsonobj.getString("title"));
                        successNum++;
                    } else {
                        log.error("站点[" + titleJs.toString()
                                + "]-关键词新闻，添加失败！title="
                                + kpiEntity.getSN_TITLE());
                    }
                }/*
                 * else{ String siteterm = isHave.get(0).getString("siteterm");
                 * if(siteterm != null && !siteterm.contains(qxName)){ String
                 * site = siteterm+ "," + qxName;
                 * siteNewsDao.updateSiteNews(id,site); } }
                 */
            } else {
                log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果为空:"
                        + tokens + "，当前token=" + token.getTK_TOKEN());
            }
        }
        return successNum;
    }

    public List<String> insertOriNews(String SITE_ID, String types,
                                      int newstype, String pageTypeID, String currentTime,
                                      List<String> rmDuplicate) {

        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl();
        url = url + "/api/transmission/source/article/original";
        JSONArray dateobj = getdata(-7);

        String[] sid = SITE_ID.split(",");
        // String[] sname = names.split(",");
        String[] type = types.split(",");

        String datatype = null;
        for (int z = 0; z < sid.length; z++) {
            if (rmDuplicate != null && rmDuplicate.contains(sid[z])) {
                continue;
            }
            if ("NEWS".equalsIgnoreCase(type[z])) {
                datatype = "news";
            } else if ("APP".equalsIgnoreCase(type[z])) {
                datatype = "app";
            } else if ("WECHAT".equalsIgnoreCase(type[z])) {
                datatype = "wechat";
            } else if ("WEIBO".equalsIgnoreCase(type[z])) {
                datatype = "weibo";
            } else {
                continue;
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", token.getTK_TOKEN());
            map.put("pagesize", ORIGINAL_PAGESIZE);
            map.put("sourceids", sid[z]);
            // map.put("mediaid", "443");
            map.put("datatype", datatype);
            if (newstype == 1) {
                map.put("sort", "2");
            } else {
                if ("weibo".equalsIgnoreCase(type[z])) {
                    map.put("sort", "4");
                } else {
                    map.put("sort", "5");
                }
            }
            map.put("starttime", dateobj.get(0).toString());
            map.put("endtime", dateobj.get(1).toString());

            String tokens = httpAPIService.doPost(url, map);

            if (!StringUtils.isBlank(tokens)) {
                JSONObject json = JSONObject.parseObject(tokens);
                JSONArray documents = json.getJSONArray("documents");
                if (documents != null && documents.size() > 0) {
                    log.info("站点[" + sid[z] + "]-原创新闻，类型=" + datatype
                            + "数据返回结果:" + documents.size());

                    int successNum = 0;
                    for (Object obj : documents) {
                        JSONObject jsonobj = JSONObject.parseObject(obj
                                .toString());

                        SiteNewsEntity kpiEntity = new SiteNewsEntity();
                        kpiEntity.setSYS_DELETEFLAG(0);
                        Date date = new Date();
                        DateFormat format = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss");
                        String time = format.format(date);
                        kpiEntity.setSN_LASTMODIFIED(time);
                        kpiEntity.setSN_ID(jsonobj.getString("id"));
                        String title = jsonobj.getString("title");
                        if (title == null || "".equals(title)) {
                            continue;
                        }
                        String centent = filterEmoji(title, "*");
                        if (!isChinaWord(centent)) {
                            continue;
                        }
                        title = centent;
                        kpiEntity.setSN_TITLE(title);
                        kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                        kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                        String summary = jsonobj.getString("summary");
                        if (summary != null && !"".equals(summary)) {
                            String summ = filterEmoji(summary, "*");
                            if (!isChinaWord(summ)) {
                                continue;
                            }
                            summary = summ;
                            kpiEntity.setSN_SUMMARY(summary);
                        }
                        String words = jsonobj.getString("keywords");
                        if (words != null && !"".equals(words)) {
                            String keyword = filterEmoji(words, "*");
                            if (!isChinaWord(keyword)) {
                                continue;
                            }
                            if (keyword != null && keyword.length() > 254) {
                                keyword = keyword.substring(0, 254);
                            }
                            kpiEntity.setSN_KEYWORDS(keyword);
                        }
                        if (jsonobj.getString("dataType") != null
                                && "wechat".equals(jsonobj
                                .getString("dataType"))) {
                            kpiEntity.setSN_SOURCE(jsonobj.getString("channel")
                                    + "-微信");
                        } else {
                            kpiEntity.setSN_SOURCE(jsonobj.getString("source"));
                        }
                        kpiEntity.setSN_SOURCEID(jsonobj.getString("sourceId"));
                        kpiEntity.setSN_CHANNEL(jsonobj.getString("channel"));
                        kpiEntity.setSN_CHANNELID(jsonobj
                                .getString("channelId"));
                        kpiEntity.setSN_VISITCOUNT(jsonobj
                                .getIntValue("visitCount"));
                        kpiEntity.setSN_REBACKCOUNT(jsonobj
                                .getIntValue("rebackCount"));
                        kpiEntity.setSN_LIKECOUNT(jsonobj
                                .getIntValue("likeCount"));
                        kpiEntity.setSN_FORWARDCOUNT(jsonobj
                                .getIntValue("forwardCount"));
                        kpiEntity.setSN_DATATYPE(jsonobj.getString("dataType"));
                        kpiEntity.setSN_AUTHOR(jsonobj.getString("author"));
                        kpiEntity.setSN_ORIGINALPICURL(jsonobj
                                .getString("originalPicUrl"));
                        kpiEntity.setSN_ORIGINAL(newstype);
                        kpiEntity.setSN_ISHOW(1);
                        kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                        kpiEntity.setSN_SITETERM(sid[z]);
                        /*
                         * String con = getNews(jsonobj.getString("id"));
                         * if("WEIBO".equalsIgnoreCase(type[z])){ String centent
                         * = filterEmoji(getNews(jsonobj .getString("id")),"*");
                         * if(!isChinaWord(centent)){ continue; } con = centent;
                         * } kpiEntity.setSN_DOCUMENT(con);
                         */
                        int num = siteNewsDao.addSiteNews(kpiEntity);
                        if (num == 1) {
                            dataJson.put(jsonobj.getString("id"),
                                    jsonobj.getString("title"));
                            successNum++;
                        } else {
                            log.error("站点[" + sid[z] + "]-原创新闻，类型=" + datatype
                                    + "添加失败！title=" + kpiEntity.getSN_TITLE());
                        }
                        /*
                         * else{ String siteterm =
                         * isHave.get(0).getString("siteterm"); if(siteterm !=
                         * null && !siteterm.contains(sid[z])){ String site =
                         * siteterm+ "," + sid[z];
                         * siteNewsDao.updateSiteNews(id,site); } }
                         */
                    }
                    if (successNum > 0) {
                        clearData(sid[z], newstype, currentTime, successNum);
                    }
                    // resultMap.put(sid[z], successNum);
                } else {
                    log.info("站点[" + sid[z] + "]-原创新闻，类型=" + datatype
                            + "数据返回结果为空:" + tokens + "，当前token="
                            + token.getTK_TOKEN());
                }
            }
        }
        return rmDuplicate;
    }

    public int insertHotNews(String SITE_ID, String type, String pageTypeID,
                             int orig) {
        int successNum = 0;
        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl();
        url = url + "/api/transmission/source/article/original";

        JSONArray dates = null;
        if (orig == 3) {
            dates = getdata(-1);
        } else if (orig == 4) {
            dates = getdata(-7);
        } else {
            dates = getdata(-30);
        }
        int m = 0;
        for (int i = 0; i < 10; i++) {
            if (m > 1) {
                break;
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", token.getTK_TOKEN());
            map.put("sourceids", SITE_ID);
            map.put("pagesize", COMMON_PAGESIZE);
            map.put("page", String.valueOf(i + 1));
            map.put("datatype", type);
            map.put("sort", "2");
            map.put("starttime", dates.get(0).toString());
            map.put("endtime", dates.get(1).toString());

            String tokens = httpAPIService.doPost(url, map);

            if (!StringUtils.isBlank(tokens)) {
                JSONObject json = JSONObject.parseObject(tokens);
                JSONArray documents = json.getJSONArray("documents");
                if (documents != null && documents.size() > 0) {
                    log.info("站点[" + SITE_ID + "]-普通新闻，类型=" + type + "数据返回结果:"
                            + documents.size());
                    for (Object obj : documents) {
                        JSONObject jsonobj = JSONObject.parseObject(obj
                                .toString());
                        if (!jsonobj.containsKey("converPicUrl") || StringUtils.isBlank(jsonobj
                                .getString("converPicUrl"))) {
                            continue;
                        }
                        if (m > 1) {
                            break;
                        }
                        String id = jsonobj.getString("id");
                        SiteNewsEntity kpiEntity = new SiteNewsEntity();

                        kpiEntity.setSYS_DELETEFLAG(0);
                        Date date = new Date();
                        DateFormat format = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss");
                        String time = format.format(date);
                        kpiEntity.setSN_LASTMODIFIED(time);
                        kpiEntity.setSN_ID(jsonobj.getString("id"));

                        String title = jsonobj.getString("title");
                        if (title == null || "".equals(title)) {
                            continue;
                        }
                        String centent = filterEmoji(title, "*");
                        if (!isChinaWord(centent)) {
                            continue;
                        }
                        title = centent;
                        kpiEntity.setSN_TITLE(title);
                        kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                        kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                        String summary = jsonobj.getString("summary");
                        if (summary != null && !"".equals(summary)) {
                            String summ = filterEmoji(summary, "*");
                            if (!isChinaWord(summ)) {
                                continue;
                            }
                            summary = summ;
                            kpiEntity.setSN_SUMMARY(summary);
                        }
                        String words = jsonobj.getString("keywords");
                        if (words != null && !"".equals(words)) {
                            String keyword = filterEmoji(words, "*");
                            if (!isChinaWord(keyword)) {
                                continue;
                            }
                            if (keyword != null && keyword.length() > 254) {
                                keyword = keyword.substring(0, 254);
                            }
                            kpiEntity.setSN_KEYWORDS(keyword);
                        }
                        if (jsonobj.getString("dataType") != null
                                && "wechat".equals(jsonobj
                                .getString("dataType"))) {
                            kpiEntity.setSN_SOURCE(jsonobj.getString("channel")
                                    + "-微信");
                        } else {
                            kpiEntity.setSN_SOURCE(jsonobj.getString("source"));
                        }
                        kpiEntity.setSN_SOURCEID(jsonobj.getString("sourceId"));
                        kpiEntity.setSN_CHANNEL(jsonobj.getString("channel"));
                        kpiEntity.setSN_CHANNELID(jsonobj
                                .getString("channelId"));
                        kpiEntity.setSN_VISITCOUNT(jsonobj
                                .getIntValue("visitCount"));
                        kpiEntity.setSN_REBACKCOUNT(jsonobj
                                .getIntValue("rebackCount"));
                        kpiEntity.setSN_LIKECOUNT(jsonobj
                                .getIntValue("likeCount"));
                        kpiEntity.setSN_FORWARDCOUNT(jsonobj
                                .getIntValue("forwardCount"));
                        kpiEntity.setSN_DATATYPE(jsonobj.getString("dataType"));
                        kpiEntity.setSN_AUTHOR(jsonobj.getString("author"));

                        kpiEntity.setSN_ORIGINALPICURL(jsonobj
                                .getString("converPicUrl"));
                        kpiEntity.setSN_ORIGINAL(orig);
                        kpiEntity.setSN_ISHOW(1);
                        kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                        kpiEntity.setSN_SITETERM(SITE_ID);
                        String content = getNews(id);
                        if (content != null && !"".equals(content)) {
                            String cent = filterEmoji(content, "*");
                            if (!isChinaWord(cent)) {
                                continue;
                            }
                            content = cent;
                            kpiEntity.setSN_DOCUMENT(content);
                        }
                        int num = siteNewsDao.addSiteNews(kpiEntity);
                        if (1 == num) {
                            m++;
                            dataJson.put(jsonobj.getString("id"),
                                    jsonobj.getString("title"));
                            successNum++;
                        } else {
                            log.info("站点[" + SITE_ID + "]-普通新闻，类型=" + type
                                    + "添加失败！title=" + kpiEntity.getSN_TITLE());
                        }
                    }
                    return successNum;
                } else {
                    log.info("站点[" + SITE_ID + "]-普通新闻，类型=" + type
                            + "数据返回结果为空:" + tokens + "，当前token="
                            + token.getTK_TOKEN());
                    break;
                }
            }
        }
        return successNum;
    }

    public int insertNews(String SITE_ID, String type, String pageTypeID,
                          int orig) {
        int successNum = 0;
        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl();
        JSONArray datatype = new JSONArray();
        /*
         * JSONArray Rank = new JSONArray(); Rank.add(1); Rank.add(2);
         */
        JSONObject sorts = new JSONObject();
        JSONArray site = new JSONArray();
        site.add(SITE_ID);
        JSONObject con = new JSONObject();
        if ("news".equalsIgnoreCase(type)) {
            url = url + "/api/fts/news";
            datatype.add("news");
            con.put("sites", site);
            sorts.put("field", "pubdate");
            sorts.put("order", "desc");
        } else if ("app".equalsIgnoreCase(type)) {
            url = url + "/api/fts/app";
            datatype.add("app");
            con.put("sites", site);
            if (orig != 0) {
                sorts.put("field", "VISITCOUNT");
                sorts.put("order", "desc");
            } else {
                sorts.put("field", "pubdate");
                sorts.put("order", "desc");
            }
        }
        if ("epaper".equalsIgnoreCase(type)) {
            url = url + "/api/fts";
            datatype.add("epaper");
            con.put("sites", site);
            sorts.put("field", "pubdate");
            sorts.put("order", "desc");
        } else if ("weibo".equalsIgnoreCase(type)) {
            url = url + "/api/fts/weibo";
            datatype.add("weibo");
            con.put("weibouid", site);
            if (orig != 0) {
                sorts.put("field", "FORWARDCOUNT");
                sorts.put("order", "desc");
            } else {
                sorts.put("field", "pubdate");
                sorts.put("order", "desc");
            }
        } else if ("wechat".equalsIgnoreCase(type)) {
            url = url + "/api/fts/wechat";
            datatype.add("wechat");
            con.put("wechatuid", site);
            if (orig != 0) {
                sorts.put("field", "VISITCOUNT");
                sorts.put("order", "desc");
            } else {
                sorts.put("field", "pubdate");
                sorts.put("order", "desc");
            }
        }
        Map<String, String> map = new HashMap<String, String>();

        JSONArray dates = null;
        if (orig == 3) {
            dates = getdata(-1);
            map.put("pagesize", HOT_PAGESIZE);
        } else if (orig == 4) {
            dates = getdata(-7);
            map.put("pagesize", HOT_PAGESIZE);
        } else {
            dates = getdata(-30);
            map.put("pagesize", COMMON_PAGESIZE);
        }

        con.put("datatype", datatype);
        con.put("pubtime", dates);

        con.put("duplicate", "1");
        // con.put("sourceRank", Rank);
        // con.put("channelRank",Rank);
        JSONArray sort = new JSONArray();
        sort.add(sorts);

        map.put("access_token", token.getTK_TOKEN());

        map.put("conditions", con.toString());
        map.put("sorts", sort.toString());
        // map.put("pagesize", "18");
        String tokens = httpAPIService.doPost(url, map);

        if (!StringUtils.isBlank(tokens)) {
            JSONObject json = JSONObject.parseObject(tokens);
            JSONArray documents = json.getJSONArray("documents");
            if (documents != null && documents.size() > 0) {
                log.info("站点[" + SITE_ID + "]-普通新闻，类型=" + type + "数据返回结果:"
                        + documents.size());
                for (Object obj : documents) {
                    JSONObject jsonobj = JSONObject.parseObject(obj.toString());
                    if (jsonobj.getString("channel").equals("未知")) {
                        log.info("============= channel 未知 =============== url: " + url + ", condition: " + con.toString());
                    }

                    String id = jsonobj.getString("id");
                    SiteNewsEntity kpiEntity = new SiteNewsEntity();

                    kpiEntity.setSYS_DELETEFLAG(0);
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    String time = format.format(date);
                    kpiEntity.setSN_LASTMODIFIED(time);
                    kpiEntity.setSN_ID(jsonobj.getString("id"));

                    String title = jsonobj.getString("title");
                    if (title == null || "".equals(title)) {
                        continue;
                    }
                    String centent = filterEmoji(title, "*");
                    if (!isChinaWord(centent)) {
                        continue;
                    }
                    title = centent;
                    kpiEntity.setSN_TITLE(title);
                    kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                    kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                    String summary = jsonobj.getString("summary");
                    if (summary != null && !"".equals(summary)) {
                        String summ = filterEmoji(summary, "*");
                        if (!isChinaWord(summ)) {
                            continue;
                        }
                        summary = summ;
                        kpiEntity.setSN_SUMMARY(summary);
                    }
                    String words = jsonobj.getString("keywords");
                    if (words != null && !"".equals(words)) {
                        String keyword = filterEmoji(words, "*");
                        if (!isChinaWord(keyword)) {
                            continue;
                        }
                        if (keyword != null && keyword.length() > 254) {
                            keyword = keyword.substring(0, 254);
                        }
                        kpiEntity.setSN_KEYWORDS(keyword);
                    }
                    if (jsonobj.getString("dataType") != null
                            && "wechat".equals(jsonobj.getString("dataType"))) {
                        kpiEntity.setSN_SOURCE(jsonobj.getString("channel")
                                + "-微信");
                    } else {
                        kpiEntity.setSN_SOURCE(jsonobj.getString("source"));
                    }
                    kpiEntity.setSN_SOURCEID(jsonobj.getString("sourceId"));
                    kpiEntity.setSN_CHANNEL(jsonobj.getString("channel"));
                    kpiEntity.setSN_CHANNELID(jsonobj.getString("channelId"));
                    kpiEntity.setSN_VISITCOUNT(jsonobj
                            .getIntValue("visitCount"));
                    kpiEntity.setSN_REBACKCOUNT(jsonobj
                            .getIntValue("rebackCount"));
                    kpiEntity.setSN_LIKECOUNT(jsonobj.getIntValue("likeCount"));
                    kpiEntity.setSN_FORWARDCOUNT(jsonobj
                            .getIntValue("forwardCount"));
                    kpiEntity.setSN_DATATYPE(jsonobj.getString("dataType"));
                    kpiEntity.setSN_AUTHOR(jsonobj.getString("author"));
                    kpiEntity.setSN_ORIGINALPICURL(jsonobj
                            .getString("originalPicUrl"));

                    kpiEntity.setSN_ORIGINAL(orig);
                    kpiEntity.setSN_ISHOW(1);
                    kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                    kpiEntity.setSN_SITETERM(SITE_ID);
                    String content = getNews(id);
                    if (content != null && !"".equals(content)) {
                        String cent = filterEmoji(content, "*");
                        if (!isChinaWord(cent)) {
                            continue;
                        }
                        content = cent;
                        kpiEntity.setSN_DOCUMENT(content);
                    }
                    int num = siteNewsDao.addSiteNews(kpiEntity);
                    if (1 == num) {
                        dataJson.put(jsonobj.getString("id"),
                                jsonobj.getString("title"));
                        successNum++;
                    } else {
                        log.info("站点[" + SITE_ID + "]-普通新闻，类型=" + type
                                + "添加失败！title=" + kpiEntity.getSN_TITLE());
                    }
                }
                return successNum;
            } else {
                log.info("站点[" + SITE_ID + "]-普通新闻，类型=" + type + "数据返回结果为空:"
                        + tokens + "，当前token=" + token.getTK_TOKEN());
            }
        }
        return successNum;
    }

    public String getNews(String id) {

        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl() + "/api/fts/document";
        String res = null;
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", token.getTK_TOKEN());
        map.put("id", id);
        String tokens = httpAPIService.doGet(url, map);
        // String result = null;
        JSONObject documents = new JSONObject();
        if (tokens != null) {
            JSONObject json = JSONObject.parseObject(tokens);
            String result = json.getString("document");
            if (result != null) {
                documents = JSONObject.parseObject(result);
                res = documents.getString("content");
            }
        }
        return res;
    }

    public JSONArray getdata(int daysum) {
        JSONArray dataobj = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String endtime = sdf.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, daysum);
        date = calendar.getTime();
        String starttime = sdf.format(date);
        dataobj.add(starttime);
        dataobj.add(endtime);
        return dataobj;
    }

    /**
     * emoji
     *
     * @param source
     * @param slipStr emoji
     * @return
     */
    public String filterEmoji(String source, String slipStr) {
        if (!StringUtils.isEmpty(source)) {
            return source.replaceAll(
                    "[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", slipStr);
        } else {
            return source;
        }
    }

    public boolean isChinaWord(String str) {
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str); // p.matcher()
        while (m.find()) {
            return true;
        }
        return false;
    }

}