package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.example.bean.*;
import com.example.service.SiteMonitorService;
import com.example.util.CommonUtil;
import com.practice.bus.bean.EnumTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.commons.TokenGet;
import com.example.dao.EmotionDao;
import com.example.dao.GetTableIdDao;
import com.example.http.HttpAPIService;
import com.example.service.EmotionService;
@Service
public class EmotionServiceImpl implements EmotionService{
	private final Logger log = LoggerFactory
			.getLogger(EmotionServiceImpl.class);
	@Autowired
	private EmotionDao emotionDao;
	@Autowired
	private GetTableIdDao getTableIdDao;
	@Autowired
	private TokenGet tokenGet;
	@Autowired
	private BigScreenEntity BSEntity;
	@Resource
	private HttpAPIService httpAPIService;
	@Autowired
	private SiteMonitorService siteMonitorService;

	@Override
	public void addEmotion(List<String> pageTypeIds, String mediaId) {
		String pTypeIds = "72";
		pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
		List<OperationEntity> opList = emotionDao.selectEmotionId(pTypeIds, mediaId);
		if (opList != null && opList.size() > 0) {
			/* Map<String, Integer> resultMap = new HashMap<String, Integer>(); */

			for (OperationEntity oper : opList) {
				String pageTypeID = oper.getPageTypeID();
				String mediaIdV = oper.getMEDIA_ID();
				if("72".equals(pageTypeID)) {
					String[] kws = null;
					// 获取关键词
					String config = oper.getConfigure();
					JSONObject extfileds = JSONObject.parseObject(oper
							.getExtfileds());
					if("90".equals(mediaIdV)){
						 String keyword = extfileds.containsKey("KeyWord") ? extfileds
									.getString("KeyWord") : config;
						    kws = keyword.split(",");
						//kws = config.split(",");
					}else{
						if (null != config && !config.isEmpty()) {
							kws = config.split(",");
						}
					}
						if (null != kws && kws.length > 0) {
							
							String fileds = extfileds.containsKey("removeWord") ? extfileds
									.getString("removeWord") : "";
							for (String kw : kws) {
								//if (null != kw && !kwList.contains(kw)) {
									//kwList.add(kw);
									searchFtsAndInsert(kw, mediaIdV,fileds,pageTypeID, oper);
								//}
							}
						}
					
				}
			}
		} else {
			return;
		}
	}
	public void searchFtsAndInsert(String leaderKw,
								   String mediaId,
								   String removeWord, String pageTypeID, OperationEntity oper) {
		JSONArray dates = getdata(-30);
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
		conditions.put("datatype", datatype);
		conditions.put("pubtime", dates);
		conditions.put("title_content", titleJs);
		conditions.put("duplicate", "1");
	
		TokenEntity token = tokenGet.getToken();
		String url = BSEntity.getRooturl() + "/api/fts/statistic/distribution/emotion";
		Map<String, String> map = new HashMap<String, String>();
		//map.put("access_token", token.getTK_TOKEN());		
		map.put("conditions", conditions.toString());
		String tokens = null;
		EnumTask enumTask = EnumTask.EMOTION;
		try {
			if("90".equals(mediaId)){
				tokens = httpAPIService.postMap(url, map,token.getTK_TOKEN());
			}else{
				map.put("access_token", token.getTK_TOKEN());
				tokens = httpAPIService.postMap(url, map,token.getTK_TOKEN());
			}
		} catch (Exception e) {
			siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data error");
			log.error("=============== method: {} fetch data from api, error: {} ============", enumTask, e.getMessage());
			e.printStackTrace();
		}

		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			int code = -1;
			if("90".equals(mediaId)){
				 code = json.getIntValue("status");
			}else{
				code = json.getIntValue("errcode");
			}		    
		    if(code==0){
		    	JSONObject data  = json.getJSONObject("data");
				if (data != null && data.size() > 0) {
					siteMonitorService.parseHandleASync(oper, SiteMonitorEntity.STATUS_START,enumTask);
					log.info("=============== method: {} mysql update ============", enumTask);
					try {
						this.updateEmotion(leaderKw, mediaId, pageTypeID, data);
					} catch (Exception e) {
						siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "update to mysql exception");
						log.error("=============== method: {} mysql update, error: {} ============", enumTask, e.getMessage());
						e.printStackTrace();
					}
					siteMonitorService.parseHandle(oper, SiteMonitorEntity.STATUS_COMPLETE,enumTask, null);
					log.info("=============== method: {} mysql update complete ============", enumTask);
				}
		    }else {
				log.info("站点[" + titleJs.toString() + "]-关键词新闻，数据返回结果为空:"
						+ tokens + "，当前token=" + token.getTK_TOKEN());
				siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data empty");
				log.error("=============== method: {} fetch data from api, error: {} ============", enumTask);
			}
		} else {
			siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data failure");
			log.error("=============== method: {} fetch data from api, failture: {} ============", enumTask);
		}
		//return successNum;
	}

	private void updateEmotion(String leaderKw, String mediaId, String pageTypeID, JSONObject data) {
		if ("90".equals(mediaId)) {
			leaderKw = "yqrd" + mediaId + pageTypeID;
		}
		int ishave = emotionDao.isHave(leaderKw, mediaId);
		if (ishave > 0) {
			emotionDao.updateEmotion(leaderKw, mediaId, data.toString());
		} else {
			EmotionEntity emotion = new EmotionEntity();
			emotion.setSiteItem(leaderKw);
			emotion.setEmotion(data.toString());
			emotion.setMediaId(mediaId);
			Date date1 = new Date();
			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time1 = format1.format(date1);
			emotion.setCreateTime(time1);
			emotionDao.addEmotion(emotion);
		}
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
}
