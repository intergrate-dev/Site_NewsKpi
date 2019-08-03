package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.bean.BigScreenEntity;
import com.example.bean.LiangHuiKpiTrendEntity;
import com.example.bean.LiangHuiNewsEntity;
import com.example.bean.OperationEntity;
import com.example.bean.TokenEntity;
import com.example.commons.TokenGet;
import com.example.dao.GetTableIdDao;
import com.example.dao.LiangHuiNewsDao;
import com.example.http.HttpAPIService;
import com.example.service.LiangHuiNewsService;

@Service
public class LiangHuiNewsServiceImpl implements LiangHuiNewsService {
	private final Logger log = LoggerFactory
			.getLogger(LiangHuiNewsServiceImpl.class);
	@Autowired
	private LiangHuiNewsDao liangHuiNewsDao;
	@Autowired
	private GetTableIdDao getTableIdDao;
	@Autowired
	private TokenGet tokenGet;

	@Autowired
	private BigScreenEntity BSEntity;
	@Resource
	private HttpAPIService httpAPIService;

	@Override
	public void addLHNews() {
		String ids = "";
		List<String> channelids = liangHuiNewsDao.queryChannelId();
		if (channelids != null && channelids.size() > 0) {
			for (String id : channelids) {
				ids = ids + id + ",";
			}
		}
		if ("".equals(ids)) {
			return;
		}
		TokenEntity token = tokenGet.getToken();
		String url = BSEntity.getRooturl();
		url = url + "/api/special/query";
		Map<String, String> map = new HashMap<String, String>();
		map.put("page", "1");
		map.put("pagesize", "20");
		map.put("channelIds", ids.substring(0, ids.length() - 1));
		map.put("sortFlag", "-1");
		String tokens = httpAPIService.doPost(url, map, token.getTK_TOKEN());
		if (!StringUtils.isEmpty((tokens))) {
			JSONObject result = JSONObject.parseObject(tokens);

			if (result.containsKey("status")
					&& "0".equals(result.getString("status"))) {
				JSONArray res = result.getJSONObject("data").getJSONArray("result");
				for (Object obj : res) {
					JSONObject art = JSONObject.parseObject(obj.toString());
					String id = art.getString("id");
					LiangHuiNewsEntity lhnews = new LiangHuiNewsEntity();
					lhnews.setTid(id);
					lhnews.setKeyWord("lianghui");
					lhnews.setSiteId("0");
					int isHave = liangHuiNewsDao.isExist(lhnews);
					if (isHave != 0) {
						continue;
					}
					lhnews.setTitle(art.getString("title"));
					;
					lhnews.setPubdate(art.getString("pubdate"));
					lhnews.setSource(art.getString("source"));
					lhnews.setChannel(art.getString("channel"));
					lhnews.setAuthor(art.getString("author"));
					lhnews.setDataType(art.getString("dataType"));
					String content = getLHNews(id);
					lhnews.setContent(content);
					lhnews.setLocation(art.getString("location"));

					Date date = new Date();
					DateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String time = format.format(date);
					lhnews.setLastModified(time);
					int num = liangHuiNewsDao.addLiangHuiNews(lhnews);
					if (1 == num) {
						// dataJson.put(jsonobj.getString("id"),
						// jsonobj.getString("title"));

					} else {
						log.info("两会新闻，添加失败！title=" + art.getString("title"));
					}
				}
			}
		}
	}

	@Override
	public void addXinMTNews() {
		List<OperationEntity> opList = liangHuiNewsDao
				.queryLiangHuiByConfig(59);
		JSONArray isGeten = new JSONArray();
		TokenEntity token = tokenGet.getToken();
		JSONArray dates = getdata(-30);
		if (opList != null && opList.size() > 0) {
			String[] types = { "app", "wechat", "weibo" };
			for (OperationEntity oper : opList) {
				String config = oper.getConfigure();
				if (isGeten.contains(config)) {
					continue;
				}
				String title = "";
				JSONArray titleJs = new JSONArray();
				if (config.contains("and")) {
					title = config.replaceAll("and", "&");
					titleJs.add(title);
				} else {
					if (config.contains("|")) {
						String[] nam = config.split("\\|");
						for (String n : nam) {
							titleJs.add(n);
						}
					} else {
						titleJs.add(config);
					}
				}
				for (String type : types) {
					String url = BSEntity.getRooturl();
					JSONArray datatype = new JSONArray();
					JSONObject sorts = new JSONObject();
					sorts.put("field", "pubdate");
					sorts.put("order", "desc");
					JSONObject con = new JSONObject();
					if ("app".equalsIgnoreCase(type)) {
						url = url + "/api/fts/app";
						datatype.add("app");
					} else if ("weibo".equalsIgnoreCase(type)) {
						url = url + "/api/fts/weibo";
						datatype.add("weibo");
					} else if ("wechat".equalsIgnoreCase(type)) {
						url = url + "/api/fts/wechat";
						datatype.add("wechat");
					}
					Map<String, String> map = new HashMap<String, String>();
					con.put("datatype", datatype);
					con.put("pubtime", dates);
					con.put("title", titleJs);
					con.put("duplicate", "1");
					JSONArray sort = new JSONArray();
					sort.add(sorts);
					map.put("access_token", token.getTK_TOKEN());
					map.put("conditions", con.toString());
					map.put("sorts", sort.toString());
					map.put("pagesize", "20");
					String tokens = httpAPIService.doPost(url, map);

					if (!StringUtils.isEmpty((tokens))) {
						JSONObject json = JSONObject.parseObject(tokens);
						JSONArray documents = json.getJSONArray("documents");
						if (documents != null && documents.size() > 0) {
							for (Object obj : documents) {
								JSONObject jsonobj = JSONObject.parseObject(obj
										.toString());

								String id = jsonobj.getString("id");
								LiangHuiNewsEntity lhnews = new LiangHuiNewsEntity();
								lhnews.setTid(id);
								lhnews.setKeyWord(config);
								lhnews.setSiteId("0");
								int isHave = liangHuiNewsDao.isExist(lhnews);
								if (isHave != 0) {
									continue;
								}
								String tit = jsonobj.getString("title");
								if (tit == null || "".equals(tit)) {
									continue;
								}
								String centent = filterEmoji(tit, "*");
								if (!isChinaWord(centent)) {
									continue;
								}
								title = centent;
								lhnews.setTitle(title);
								;
								lhnews.setPubdate(jsonobj.getString("pubdate"));
								lhnews.setSource(jsonobj.getString("source"));
								lhnews.setChannel(jsonobj.getString("channel"));
								lhnews.setAuthor(jsonobj.getString("author"));
								lhnews.setDataType(jsonobj
										.getString("dataType"));
								String content = getNews(id);
								if(content!=null && !"".equals(content)){
									String cent = filterEmoji(content,"*");
									if(!isChinaWord(cent)){
										continue;
									}
									content = cent;						
									lhnews.setContent(content);
								}
								lhnews.setLocation(jsonobj
										.getString("location"));

								Date date = new Date();
								DateFormat format = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								String time = format.format(date);
								lhnews.setLastModified(time);
								int num = liangHuiNewsDao
										.addLiangHuiNews(lhnews);
								if (1 == num) {
									// dataJson.put(jsonobj.getString("id"),
									// jsonobj.getString("title"));

								} else {
									log.info("两会新闻，添加失败！title=" + title);
								}
							}
						}
					}
					isGeten.add(config);
				}
			}
		}
	}

	@Override
	public void addLHKeyWordKpi() {
		List<OperationEntity> opList = liangHuiNewsDao
				.queryLiangHuiByConfig(60);
		JSONArray isGeten = new JSONArray();
		TokenEntity token = tokenGet.getToken();
		if (opList != null && opList.size() > 0) {
			for (OperationEntity oper : opList) {
				// String SITE_NAME = oper.getSITE_NAME();
				String config = oper.getConfigure();
				if (isGeten.contains(config)) {
					continue;
				}
				String title = "";
				JSONArray titleJs = new JSONArray();
				if (config.contains("and")) {
					title = config.replaceAll("and", "&");
					titleJs.add(title);
				} else {
					if (config.contains("|")) {
						String[] nam = config.split("\\|");
						for (String n : nam) {
							titleJs.add(n);
						}
					} else {
						titleJs.add(config);
					}
				}

				/*
				 * JSONArray datatype = new JSONArray(); datatype.add("news");
				 * datatype.add("app"); datatype.add("wechat");
				 * datatype.add("weibo"); datatype.add("epaper")
				 */;
				JSONArray dates = getdata(-1);
				JSONObject sorts = new JSONObject();
				sorts.put("field", "pubdate");
				sorts.put("order", "desc");
				JSONArray sort = new JSONArray();
				sort.add(sorts);
				// conditions.put("datatype", datatype);

				String[] countType = { "1", "2", "3", "4", "5" };
				for (String count : countType) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("access_token", token.getTK_TOKEN());
					map.put("sorts", sort.toString());
					String url = BSEntity.getRooturl();
					JSONObject conditions = new JSONObject();
					conditions.put("pubtime", dates);
					conditions.put("title", titleJs);
					conditions.put("duplicate", "1");
					if ("1".equals(count)) {
						url = url + "/api/fts/statistic/trend/datatype";
					} else if ("2".equals(count)) {
						String  types= "{"+"\"news\": 119767,"+"\"app\": 29415,"+"\"weibo\": 4910,"+"\"paper\": 4929,"+"\"wechat\": 2145}"; 
						LiangHuiKpiTrendEntity lhKpi = new LiangHuiKpiTrendEntity();
						lhKpi.setKeyWord(config);
						lhKpi.setDeleteFlag(0);
						Date date = new Date();
						DateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String time = format.format(date);
						lhKpi.setLastModified(time);					
						lhKpi.setDatatype(types);
						lhKpi.setCountType(2);
						int ishave = liangHuiNewsDao.isExistKpi(lhKpi);
						if (ishave == 0) {
							liangHuiNewsDao.addLiangHuiKpiTrend(lhKpi);
						}
						continue;
						//url = url + "/api/fts/statistic/distribution/datatype";
					} else if ("3".equals(count)) {
						String  types= "{"+"\"部委网站\": 104,"+"\"商业门户网站\": 7,"+"\"地市级新闻网站\": 13,"+"\"垂直门户\": 379,"+"\"中央级新闻网站\": 13,"+"\"地方政府网站\": 15,"+"\"其他\": 0,"+"\"省级新闻网站\": 55}";
						LiangHuiKpiTrendEntity lhKpi = new LiangHuiKpiTrendEntity();
						lhKpi.setKeyWord(config);
						lhKpi.setDeleteFlag(0);
						Date date = new Date();
						DateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String time = format.format(date);
						lhKpi.setLastModified(time);
						lhKpi.setSourcetype(types);
						lhKpi.setCountType(3);
						int ishave = liangHuiNewsDao.isExistKpi(lhKpi);
						if (ishave == 0) {
							liangHuiNewsDao.addLiangHuiKpiTrend(lhKpi);
						}
						continue;
						
						/*map.put("level", "1");
						map.put("statlevel", "1");
						url = url
								+ "/api/fts/statistic/distribution/sourcetype";*/
					} else if ("4".equals(count)) {
						url = url + "/api/fts/statistic/rank/source/all";
					} else if ("5".equals(count)) {
						url = url + "/api/fts/statistic/rank/source/all";
						conditions.put("isOriginal", "1");
					}
					map.put("conditions", conditions.toString());
					String tokens = httpAPIService.doPost(url, map);
					if (!StringUtils.isEmpty((tokens))) {
						JSONObject json = JSONObject.parseObject(tokens);
						int code = json.containsKey("errcode") ? json
								.getIntValue("errcode") : -1;
						if (code == 0) {
							JSONObject jsonobj = json.getJSONObject("data");
							if (jsonobj != null && jsonobj.size() > 0) {
								LiangHuiKpiTrendEntity lhKpi = new LiangHuiKpiTrendEntity();
								lhKpi.setKeyWord(config);
								lhKpi.setDeleteFlag(0);
								Date date = new Date();
								DateFormat format = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								String time = format.format(date);
								lhKpi.setLastModified(time);
								if ("1".equals(count)) {
									lhKpi.setDatatrend(jsonobj.toString());
									lhKpi.setCountType(1);
								} else if ("2".equals(count)) {
									lhKpi.setDatatype(jsonobj.toString());
									lhKpi.setCountType(2);
								} else if ("3".equals(count)) {
									lhKpi.setSourcetype(jsonobj.toString());
									lhKpi.setCountType(3);
								} else if ("4".equals(count)) {
									lhKpi.setSourcerank(jsonobj.toString());
									lhKpi.setCountType(4);
								} else if ("5".equals(count)) {
									lhKpi.setOrigrank(jsonobj.toString());
									lhKpi.setCountType(5);
								}
								int ishave = liangHuiNewsDao.isExistKpi(lhKpi);
								if (ishave == 0) {
									liangHuiNewsDao.addLiangHuiKpiTrend(lhKpi);
								} else {
									liangHuiNewsDao.updateLHKpi(lhKpi);
								}
							}
						}
					}
				}
				isGeten.add(config);
			}
		}
	}
	@Override
	public void addSourceNews() {
		List<OperationEntity> opList = liangHuiNewsDao
				.queryLiangHuiByConfig(62);
		JSONArray isGeten = new JSONArray();
		TokenEntity token = tokenGet.getToken();
		JSONArray dates = getdata(-30);
		if (opList != null && opList.size() > 0) {
			for (OperationEntity oper : opList) {
				// String SITE_NAME = oper.getSITE_NAME();
				String SITE_ID = oper.getSITE_ID();
				String SITE_TYPE = oper.getSITE_TYPE();
				String config = oper.getConfigure();
				if (isGeten.contains(SITE_ID + config)) {
					continue;
				}
				JSONArray sites = new JSONArray();
				JSONArray weibouid = new JSONArray();
				JSONArray wechatuid = new JSONArray();
				String[] sidArr = SITE_ID.split(",");
				// String[] snameArr = SITE_NAME.split(",");
				String[] typeArr = SITE_TYPE.split(",");
				JSONArray datatype = new JSONArray();
				for (int i = 0; i < typeArr.length; i++) {
					if ("news".equalsIgnoreCase(typeArr[i])
							|| "app".equalsIgnoreCase(typeArr[i])) {
						sites.add(sidArr[i]);
						datatype.add("news");
						datatype.add("app");
					} else if ("wechat".equalsIgnoreCase(typeArr[i])) {
						wechatuid.add(sidArr[i]);
						datatype.add("wechat");
					} else if ("weibo".equalsIgnoreCase(typeArr[i])) {
						weibouid.add(sidArr[i]);
						datatype.add("weibo");
					}
				}
				String title = "";
				JSONArray titleJs = new JSONArray();
				if (config.contains("and")) {
					title = config.replaceAll("and", "&");
					titleJs.add(title);
				} else {
					if (config.contains("|")) {
						String[] nam = config.split("\\|");
						for (String n : nam) {
							titleJs.add(n);
						}
					} else {
						titleJs.add(config);
					}
				}
				String url = BSEntity.getRooturl();

				JSONObject sorts = new JSONObject();
				sorts.put("field", "pubdate");
				sorts.put("order", "desc");
				JSONObject con = new JSONObject();
				url = url + "/api/fts";
				Map<String, String> map = new HashMap<String, String>();
				con.put("datatype", datatype);
				con.put("pubtime", dates);
				con.put("title", titleJs);
				con.put("duplicate", "1");
				con.put("sites", sites);
				con.put("weibouid", weibouid);
				con.put("wechatuid", wechatuid);
				JSONArray sort = new JSONArray();
				sort.add(sorts);
				map.put("access_token", token.getTK_TOKEN());
				map.put("conditions", con.toString());
				map.put("sorts", sort.toString());
				map.put("pagesize", "20");
				String tokens = httpAPIService.doPost(url, map);

				if (!StringUtils.isEmpty((tokens))) {
					JSONObject json = JSONObject.parseObject(tokens);
					JSONArray documents = json.getJSONArray("documents");
					if (documents != null && documents.size() > 0) {
						for (Object obj : documents) {
							JSONObject jsonobj = JSONObject.parseObject(obj
									.toString());

							String id = jsonobj.getString("id");
							LiangHuiNewsEntity lhnews = new LiangHuiNewsEntity();
							lhnews.setTid(id);
							lhnews.setKeyWord(config);
							lhnews.setSiteId(jsonobj.getString("sourceId"));
							int isHave = liangHuiNewsDao.isExist(lhnews);
							if (isHave != 0) {
								continue;
							}
							String tit = jsonobj.getString("title");
							if (tit == null || "".equals(tit)) {
								continue;
							}
							String centent = filterEmoji(tit, "*");
							if (!isChinaWord(centent)) {
								continue;
							}
							title = centent;
							lhnews.setTitle(title);
							;
							lhnews.setPubdate(jsonobj.getString("pubdate"));
							lhnews.setSource(jsonobj.getString("source"));
							lhnews.setChannel(jsonobj.getString("channel"));
							lhnews.setAuthor(jsonobj.getString("author"));
							lhnews.setDataType(jsonobj.getString("dataType"));
							String content = getNews(id);
							if(content!=null && !"".equals(content)){
								String cent = filterEmoji(content,"*");
								if(!isChinaWord(cent)){
									continue;
								}
								content = cent;						
								lhnews.setContent(content);
							}
							
							lhnews.setLocation(jsonobj.getString("location"));

							Date date = new Date();
							DateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							String time = format.format(date);
							lhnews.setLastModified(time);
							int num = liangHuiNewsDao.addLiangHuiNews(lhnews);
							if (1 == num) {
								// dataJson.put(jsonobj.getString("id"),
								// jsonobj.getString("title"));

							} else {
								log.info("两会新闻，添加失败！title=" + title);
							}
						}
					}
					isGeten.add(SITE_ID + config);
				}
			}
		}
	}

	public String getLHNews(String id) {

		TokenEntity token = tokenGet.getToken();
		String url = BSEntity.getRooturl() + "/api/special/document";
		// String res = null;
		Map<String, String> map = new HashMap<String, String>();
		// map.put("access_token", token.getTK_TOKEN());
		map.put("id", id);
		String tokens = httpAPIService.doPost(url, map, token.getTK_TOKEN());
		String result = "";
		// String result = null;
		// JSONObject documents = new JSONObject();
		if (tokens != null) {
			JSONObject json = JSONObject.parseObject(tokens);
			result = json.getString("content");
			/*
			 * if (result != null) { documents = JSONObject.parseObject(result);
			 * res = documents.getString("content"); }
			 */
		}
		return result;
	}

	public String getNews(String id) {
		// TODO
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
	 * @param slipStr
	 *            emoji
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
