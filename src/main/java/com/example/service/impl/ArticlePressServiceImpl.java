package com.example.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.example.bean.ArticleNewsKpiEntity;
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
import com.example.bean.TokenEntity;
import com.example.commons.TokenGet;
import com.example.dao.ArticlePressDao;
import com.example.http.HttpAPIService;
import com.example.service.ArticlePressService;

@Service
public class ArticlePressServiceImpl implements ArticlePressService {
	private final Logger log = LoggerFactory
			.getLogger(ArticlePressServiceImpl.class);
	@Autowired
	private ArticlePressDao articlePress;
	@Autowired
	private TokenGet tokenGet;
	@Autowired
	private BigScreenEntity BSEntity;
	@Resource
	private HttpAPIService httpAPIService;

	private static final String ORIGINAL_PAGESIZE = "20"; // 原创新闻返回数据量
	private static JSONObject dataJson = new JSONObject();

	@Override
	public void addArticlePress(List<String> pageTypeIds, String mediaId) {
		JSONArray isget = new JSONArray();
		String pTypeIds = "63";
		pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
		List<OperationEntity> opList = articlePress.selectArticlePressId(pTypeIds, mediaId);
		if (opList != null && opList.size() > 0) {
			for (OperationEntity oper : opList) {
				//OperationEntity oper = opList.get(0);
				String siteId = oper.getSITE_ID();
				String keyword = oper.getConfigure();
				String mediaIdV = oper.getMEDIA_ID();
				int successNum = 0;
				if (keyword != null && !"无需配置".equals(keyword)
						&& !"".equals(keyword)) {
					String[] words = keyword.split(",");
					for (String word : words) {
						if (!isget.contains(siteId + word)) {
							successNum = insertNews(siteId, word, mediaIdV);
						}
						if (successNum > 0) {
							isget.add(siteId + word);
						}
					}
				} else {
					if (!isget.contains(siteId)) {
						successNum = insertNews(siteId, keyword, mediaIdV);
					}
					if (successNum > 0) {
						isget.add(siteId);
					}
				}

			}
		} else {
			return;
		}
	}

	public int insertNews(String SITE_ID, String keyword, String mediaId) {
		int successNum = 0;
		List<String> issend = new ArrayList<String>();
		TokenEntity token = tokenGet.getToken();
		String url = BSEntity.getRooturl();// /api/transmission/source/original/forward/top
		url = url + "/api/transmission/source/article/original";

		JSONArray dateobj = getdata(-30);

		String datatype = "app";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token.getTK_TOKEN());
		map.put("pagesize", ORIGINAL_PAGESIZE);
		map.put("sourceids", SITE_ID);
		// map.put("mediaid","443");
		if (keyword != null && !"无需配置".equals(keyword)) {
			map.put("keywords", keyword.trim());
		}else{
			datatype = "news";
		}
		map.put("sort","2");
		map.put("datatype", datatype);
		map.put("starttime", dateobj.get(0).toString());
		map.put("endtime", dateobj.get(1).toString());

		String tokens = httpAPIService.doPost(url, map);
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			JSONArray documents = json.getJSONArray("documents");
			if (documents != null && documents.size() > 0) {
				/*if (!mediaId.equals("92")) {
					return 0;
				}*/
				log.info("站点[" + SITE_ID + "]-文章传播分析，数据返回结果:"
						+ documents.size());
				for (Object obj : documents) {
					JSONObject jsonobj = JSONObject.parseObject(obj.toString());
					String title = jsonobj.getString("title").trim();
					if (issend != null && issend.size() > 0
							&& issend.contains(title)) {
						continue;
					}
					if (jsonobj.getIntValue("forwardCount") == 0) {
						continue;
					}
					ArticleNewsKpiEntity articleNews = new ArticleNewsKpiEntity();
					Date date = new Date();
					DateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String time = format.format(date);
					articleNews.setCreatTime(time);
					String id = jsonobj.getString("id");
					articleNews.setId(id);
					log.info("--------------------------------- insertNews start mediaId:{}, keyword: {}, id: {}, title: {}, createTime: {}",
							mediaId, keyword, jsonobj.getString("id"), title, time);
					articleNews.setTitle(title);
					String pubtime = jsonobj.getString("pubdate");
					articleNews.setPubtime(pubtime);
					articleNews.setSource(jsonobj.getString("source"));
					articleNews.setChannel(jsonobj.getString("channel"));
					JSONObject forwards = getForwardCount(id,
							token.getTK_TOKEN(),jsonobj.getString("pubdate"));
					articleNews.setForwardCount(forwards
							.getString("forwardCount"));
					articleNews.setForwardMediaCount(forwards
							.getString("forwardMediaCount"));
					JSONArray medialist = getForwardMediaList(id,
							token.getTK_TOKEN(), jsonobj.getString("pubdate"));
					articleNews.setForwardMediaList(medialist.toString());
					JSONArray newslist = getForwardNewsList(id,
							token.getTK_TOKEN());
					articleNews.setForwardNewsList(newslist.toString());
					JSONObject kiptrend = getForwardKpiTrend(id,
							token.getTK_TOKEN(), pubtime);
					articleNews.setForwardKpiTrend(kiptrend.toString());
					JSONObject press = getPress(id, token.getTK_TOKEN());
					articleNews.setPressTypes(press.getJSONArray("pressTypes")
							.toString());
					articleNews.setPressDistribution(press.getJSONArray(
							"pressDistribution").toString());
					articleNews.setSiteId(SITE_ID);
					if (keyword != null && !"无需配置".equals(keyword)) {
						articleNews.setKeyword(keyword.trim());
					} else {
						articleNews.setKeyword("0");
						keyword = "0";
					}
					/*int m = articlePress.isHave(id, SITE_ID, keyword);
					String mediaId = BSEntity.getMediaId();*/
					/*int m = articlePress.isHave(id, mediaId, keyword);
					if (m != 0) {
						articlePress.deleteArticlePress(mediaId,id,0, null);
					}*/
					articlePress.deleteArticlePress(mediaId,id,0, null);
					articleNews.setMediaId(mediaId);
					int num = articlePress.addArticlePress(articleNews);
					if (num == 1) {
						successNum++;
						issend.add(title);
					} else {
						log.error("文章传播分析 添加失败！title=" + articleNews.getTitle());
					}
					if (successNum > 4) {
						break;
					}
				}
				articlePress.deleteArticlePress(mediaId, null, 1, keyword);
			} else {
				log.info("站点[" + SITE_ID + "]-文章传播分析数据返回结果为空:" + tokens
						+ "，当前token=" + token.getTK_TOKEN());
			}
		}
		return successNum;
	}

	public JSONObject getForwardCount(String id, String token,String pubdate) {
		JSONObject result = new JSONObject();
		String url = BSEntity.getRooturl();
		//JSONArray dateobj = getdata(-30);
		String pubtime = pubdate.substring(0, 10);
		long num = daysOfTwo_2(pubtime);
		JSONArray dateobj = null;
		if (num <= 7) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateobj = getdata(-6, sdf.format(new Date()), 1);
		} else {
			dateobj = getdata(6, pubdate, 2);
		}
		url = url + "/api/transmission/article/forward/kpi";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("id", id);
		map.put("starttime", dateobj.get(0).toString());
		map.put("endtime", dateobj.get(1).toString());

		String tokens = httpAPIService.doPost(url, map);
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			result = json.getJSONObject("result");

		}
		return result;
	}

	public JSONArray getForwardNewsList(String id, String token) {
		String url = BSEntity.getRooturl();
		url = url + "/api/transmission/article/forward/list";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("mediaid", "433");
		map.put("sort", "1");
		map.put("id", id);
		map.put("pagesize", "5");
		String tokens = httpAPIService.doPost(url, map);
		JSONArray pressarray = new JSONArray();
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			JSONArray documents = json.getJSONArray("documents");
			if (documents != null && documents.size() > 0) {
				for (Object obj : documents) {
					JSONObject presstype = new JSONObject();
					JSONObject jsonobj = JSONObject.parseObject(obj.toString());
					presstype.put("title", jsonobj.getString("title"));
					presstype.put("source", jsonobj.getString("source"));
					presstype.put("id", jsonobj.getString("id"));
					presstype.put("dataType", jsonobj.getString("dataType"));
					presstype.put("pubdate", jsonobj.getString("pubdate"));
					pressarray.add(presstype);
				}
			}
		}
		return pressarray;
	}

	public JSONArray getForwardMediaList(String id, String token, String ptime) {
		String url = BSEntity.getRooturl();
		url = url + "/api/transmission/article/forward/source/list";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("id",id);
		map.put("ptime", ptime);
		map.put("pagesize", "10");
		String tokens = httpAPIService.doPost(url, map);
		JSONArray pressdoc = new JSONArray();
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			JSONObject documents = json.getJSONObject("result");
			if (documents != null && documents.size() > 0) {
				pressdoc = documents.getJSONArray("data");
			}
		}

		return pressdoc;
	}

	public JSONObject getForwardKpiTrend(String id, String token, String pubdate) {
		String pubtime = pubdate.substring(0, 10);
		long num = daysOfTwo_2(pubtime);
		JSONArray dateobj = null;
		if (num <= 7) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateobj = getdata(-6, sdf.format(new Date()), 1);
		} else {
			dateobj = getdata(6, pubdate, 2);
		}
		JSONObject forwardTrend = getForwardTrend(id, token, dateobj);
		JSONArray forwards = dataAll(forwardTrend.getJSONArray("dataAll"));
		JSONObject mediaTrend = getMediaTrend(id, token, dateobj);
		JSONArray mediacount = dataAll(mediaTrend.getJSONArray("dataArr"));
		JSONArray time = forwardTrend.getJSONArray("timeArr");
		JSONObject result = new JSONObject();
		result.put("timestamp", time);
		result.put("forwardCount", forwards);
		result.put("forwardMediaCount", mediacount);
		return result;
	}

	public long daysOfTwo_2(String pubtime) {
		// 跨年不会出现问题
		// 如果时间为：2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String endtime = sdf.format(date);
			Date oDate = sdf.parse(endtime);
			Date fDate = sdf.parse(pubtime);
			long days = (oDate.getTime() - fDate.getTime())
					/ (1000 * 3600 * 24);
			System.out.print(days);
			return days;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public JSONArray getdata(int daysum, String time, int type) {
		try {
			JSONArray dataobj = new JSONArray();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = sdf.parse(time);
			String endtime = sdf.format(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, daysum);
			date = calendar.getTime();
			String starttime = sdf.format(date);
			if (type == 1) {
				dataobj.add(starttime);
				dataobj.add(endtime);
			} else {
				dataobj.add(endtime);
				dataobj.add(starttime);
			}
			return dataobj;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public JSONArray dataAll(JSONArray data) {
		JSONArray result = new JSONArray();
		for (Object rul : data) {
			JSONObject sum = JSONObject.parseObject(rul.toString());
			result.add(sum.getIntValue("count"));
		}
		return result;
	}

	public JSONObject getForwardTrend(String id, String token, JSONArray dateobj) {
		JSONObject result = new JSONObject();
		String url = BSEntity.getRooturl();
		url = url + "/api/transmission/article/forward/datatype/trend";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("id", id);
		map.put("starttime", dateobj.get(0).toString());
		map.put("endtime", dateobj.get(1).toString());

		String tokens = httpAPIService.doPost(url, map);
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			result = json.getJSONObject("result");

		}
		return result;
	}

	public JSONObject getMediaTrend(String id, String token, JSONArray dateobj) {
		JSONObject result = new JSONObject();
		String url = BSEntity.getRooturl();
		url = url + "/api/transmission/article/forward/source/trend";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("id", id);
		map.put("starttime", dateobj.get(0).toString());
		map.put("endtime", dateobj.get(1).toString());

		String tokens = httpAPIService.doPost(url, map);
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			result = json.getJSONObject("result");

		}
		return result;
	}

	public JSONObject getPress(String id, String token) {
		JSONObject result = new JSONObject();
		String url = BSEntity.getRooturl();
		url = url + "/api/transmission/article/forward/distribution";
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("id", id);
		String tokens = httpAPIService.doPost(url, map);
		JSONArray pressarray = new JSONArray();
		JSONArray pressdoc = new JSONArray();
		if (!StringUtils.isBlank(tokens)) {
			JSONObject json = JSONObject.parseObject(tokens);
			JSONArray documents = json.getJSONArray("result");
			if (documents != null && documents.size() > 0) {
				for (Object obj : documents) {
					JSONObject presstype = new JSONObject();
					JSONObject jsonobj = JSONObject.parseObject(obj.toString());
					presstype.put("dataType", jsonobj.getString("dataType"));
					presstype.put("forwardCount",
							jsonobj.getString("forwardCount"));
					if ("news".equals(jsonobj.getString("dataType"))) {
						pressdoc = jsonobj.getJSONArray("sourceTypes");
					}
					pressarray.add(presstype);
				}
			}
		}
		result.put("pressTypes", pressarray);
		result.put("pressDistribution", pressdoc);
		return result;
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
	/*
	 * public static void main(String[] args) { JSONObject obj = new
	 * JSONObject(); obj.put("increaseUserCount", 1);//净增用户数
	 * obj.put("userCount", 2);//累计用户数 obj.put("pressCount", 3);//发稿数
	 * obj.put("originalCount", 4);//原创数 System.out.println(obj.toString()); }
	 */
}