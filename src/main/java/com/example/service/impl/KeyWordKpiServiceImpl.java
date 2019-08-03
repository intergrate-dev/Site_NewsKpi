package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.example.service.KeyWordKpiService;
import com.example.dao.KeyWordKpiDao;

@Service
public class KeyWordKpiServiceImpl implements KeyWordKpiService {
    private final Logger log = LoggerFactory
            .getLogger(KeyWordKpiServiceImpl.class);
    @Autowired
    private KeyWordKpiDao KeyWordKpiDao;
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

    private static JSONObject dataJson = new JSONObject();

    @Override
    public void addKeyWordKpi(List<String> pageTypeIds, String mediaId) {
        // 获取当前的时间，作为删除旧数据的时间节点
        String currentTime = DateHelper.parse2String(new Date(),
                DateHelper.DateFormat.YMDHMS);
        String pTypeIds = "74,75";
        pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
        List<OperationEntity> opList = KeyWordKpiDao.selectKeyWordKpiId(pTypeIds, mediaId);
        if (opList != null && opList.size() > 0) {
            for (OperationEntity oper : opList) {
                String pageTypeID = oper.getPageTypeID();
                String mediaIdV = oper.getMEDIA_ID();
                if ("74".equals(pageTypeID) || "75".equals(pageTypeID)) {
                    // 获取关键词
                    JSONObject extfileds = JSONObject.parseObject(oper
                            .getExtfileds());
                    if (null != extfileds && !extfileds.isEmpty()) {
                        String leaderSel = extfileds.containsKey("leaderSel") ? extfileds
                                .getString("leaderSel") : null;
                        String selTime = extfileds.containsKey("selTime") ? extfileds
                                .getString("selTime") : null;
                        String[] kws = extfileds.getString("KeyWord").split(",");
                        String[] sws = extfileds.getString("ShowWord").split(",");
                        if (null != kws && kws.length > 0) {
                            String fileds = extfileds.containsKey("removeWord") ? extfileds
                                    .getString("removeWord") : "";
                            for (int i = 0; i < kws.length; i++) {
                                searchKeywordKpi(kws[i], sws[i], mediaIdV, fileds, pageTypeID, currentTime, leaderSel, selTime);
                            }
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    public void searchKeywordKpi(String leaderKw, String ShowKw, String mediaId,
                                 String removeWord, String pageTypeID, String currentTime, String leaderSel, String selTime) {
        int forwardCount = searchFtsAndInsert(leaderKw, mediaId, removeWord, null, pageTypeID, currentTime, leaderSel, selTime);
        JSONArray yj = new JSONArray();
        yj.add(7);
        yj.add(104);
        int centerCount = searchFtsAndInsert(leaderKw, mediaId, removeWord, yj, pageTypeID, currentTime, leaderSel, selTime);
        JSONArray sj = new JSONArray();
        sj.add(8);
        sj.add(71);
        sj.add(105);
        sj.add(106);
        int provinceCount = searchFtsAndInsert(leaderKw, mediaId, removeWord, sj, pageTypeID, currentTime, leaderSel, selTime);
        JSONArray shij = new JSONArray();
        shij.add(101);
        shij.add(107);
        int cityCount = searchFtsAndInsert(leaderKw, mediaId, removeWord, shij, pageTypeID, currentTime, leaderSel, selTime);
        JSONArray xj = new JSONArray();
        xj.add(102);
        xj.add(108);
        int countyCount = searchFtsAndInsert(leaderKw, mediaId, removeWord, xj, pageTypeID, currentTime, leaderSel, selTime);
        int count = centerCount * 10 + provinceCount * 8 + cityCount * 6 + countyCount * 5;
        int ishave = KeyWordKpiDao.isHave(ShowKw, mediaId);
        if (ishave > 0) {
            KeyWordKpiDao.updateKeyWordKpi(ShowKw, mediaId, forwardCount, centerCount, provinceCount + cityCount, countyCount, count);
        } else {
            KeyWordKpiDao.addKeyWordKpi(ShowKw, mediaId, forwardCount, centerCount, provinceCount + cityCount, countyCount, count);
        }
    }

    public int searchFtsAndInsert(String leaderKw,
                                  String mediaId,
                                  String removeWord, JSONArray sourceTypeId, String pageTypeID, String currentTime, String leaderSel, String selTime) {
        JSONArray dates = null;
        if (selTime != null && !"".equals(selTime) && "7D".equals(selTime)) {
            dates = getdata(-7);
        } else {
            dates = getdata(-30);
        }
        String keyWord = "";
        String outWord = "";

        if (!"".endsWith(removeWord)) {
            outWord = removeWord;
        }
        keyWord = leaderKw;
        JSONObject conditions = new JSONObject();
        if (null == leaderKw) {
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
            titleJs.add(title);
        }
        JSONArray datatype = new JSONArray();
        datatype.add("news");
        datatype.add("app");
        datatype.add("wechat");
        datatype.add("epaper");
        if (sourceTypeId != null && sourceTypeId.size() > 0) {
            conditions.put("sourceTypeId", sourceTypeId);
        }
        conditions.put("datatype", datatype);
        conditions.put("pubtime", dates);
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
        //conditions.put("duplicate", "1");

        TokenEntity token = tokenGet.getToken();
        String url = BSEntity.getRooturl() + "/api/fts";
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", token.getTK_TOKEN());
        map.put("conditions", conditions.toString());
        map.put("pagesize", "30");
        String tokens = httpAPIService.doPost(url, map);

        if (!StringUtils.isBlank(tokens)) {
            JSONObject json = JSONObject.parseObject(tokens);
            int code = json.getIntValue("errcode");
            if (code == 0) {
                if (sourceTypeId != null && sourceTypeId.size() > 0) {

                } else {
                    JSONArray documents = json.getJSONArray("documents");
                    int successNum = saveNews(documents, pageTypeID, leaderKw);
                    clearData(leaderKw, 0, currentTime, successNum);
                    System.out.println("插入成功：" + successNum);
                    System.out.println("总数：" + json.getIntValue("total"));
                }
                return json.getIntValue("total");
            } else {
                log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果为空:"
                        + tokens + "，当前token=" + token.getTK_TOKEN());
            }
        }
        return 0;
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

    public int saveNews(JSONArray documents, String pageTypeID, String keyWord) {
        //
        JSONArray issend = new JSONArray();
        int successNum = 0;
        if (documents != null && documents.size() > 0) {
            for (Object obj : documents) {
                JSONObject jsonobj = JSONObject.parseObject(obj.toString());
                String id = jsonobj.getString("id");
                String titles = jsonobj.getString("title").trim();
                if (titles == null || "".equals(titles)) {
                    continue;
                }
                if (issend != null && issend.contains(titles)) {
                    continue;
                }
                SiteNewsEntity kpiEntity = new SiteNewsEntity();
                kpiEntity.setSYS_DELETEFLAG(0);
                Date date = new Date();
                DateFormat format = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                String time = format.format(date);
                kpiEntity.setSN_LASTMODIFIED(time);

                kpiEntity.setSN_ID(id);


                kpiEntity.setSN_TITLE(titles);
                kpiEntity.setSN_PUBDATE(jsonobj.getString("pubdate"));
                kpiEntity.setSN_LOCATION(jsonobj.getString("location"));
                String summary = jsonobj.getString("summary");

                kpiEntity.setSN_SUMMARY(summary);

                String words = jsonobj.getString("keywords");

                kpiEntity.setSN_KEYWORDS(words);

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
                kpiEntity.setSN_ORIGINAL(0);
                kpiEntity.setSN_ISHOW(1);
                kpiEntity.setSN_PAGETYPEID(Integer.valueOf(pageTypeID));
                kpiEntity.setSN_SITETERM(keyWord);

                String con = getNews(jsonobj.getString("id"));
                kpiEntity.setSN_DOCUMENT(con);
                int sum = siteNewsDao.addSiteNews(kpiEntity);
                if (sum == 1) {
                    issend.add(titles);
                    dataJson.put(id, titles);
                    successNum++;
                } else {
                    log.error("站点[" + keyWord
                            + "]-关键词新闻，添加失败！title="
                            + kpiEntity.getSN_TITLE());
                }
                if (successNum > 5) {
                    break;
                }
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

        remainNum = "6";
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
}
