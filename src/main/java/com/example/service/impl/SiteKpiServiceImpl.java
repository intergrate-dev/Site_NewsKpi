package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sun.applet.Main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.bean.BigScreenEntity;
import com.example.bean.OperationEntity;
import com.example.bean.SiteKpiEntity;
import com.example.bean.TokenEntity;
import com.example.commons.DateHelper;
import com.example.commons.TokenGet;
import com.example.dao.GetTableIdDao;
import com.example.dao.SiteKpiDao;
import com.example.http.HttpAPIService;
import com.example.service.SiteKpiService;

@Service
public class SiteKpiServiceImpl implements SiteKpiService{
	
	private Logger log = LoggerFactory.getLogger(SiteKpiServiceImpl.class);
	
	@Autowired
	private SiteKpiDao siteKpiDao;
	@Autowired
	private GetTableIdDao getTableIdDao;
	@Autowired
	private TokenGet tokenGet;
	
	@Autowired
	private BigScreenEntity BSEntity;
	@Resource
    private HttpAPIService httpAPIService;
	
	@Override
	public void addWebWord() {
		
		//String url = BSEntity.getRooturl()+ "/api/external/hotlist/baidu/buzz";
		//相关页面站点的配置
		List<OperationEntity> opList = siteKpiDao.selectSiteKpiId();
		JSONArray kpijson = new JSONArray();
		JSONArray trendjson = new JSONArray();
		
		if(opList != null && opList.size()>0){
			//根据配置配置条数循环
			for(OperationEntity oper :opList){
				String pageTypeID = oper.getPageTypeID();
				String SITE_NAME = oper.getSITE_NAME();
				String SITE_ID = oper.getSITE_ID();
				String SITE_TYPE = oper.getSITE_TYPE();
				String sArr = oper.getUSER_BEHAVIOR_ID();
				String configure = "";
				if("64".equals(pageTypeID)||"65".equals(pageTypeID)){
					configure = "3D";
				}else{
					 configure = oper.getConfigure();
				}
				
				String[] sidArr = SITE_ID.split(",");
				String[] snameArr = SITE_NAME.split(",");
				String[] typeArr = SITE_TYPE.split(",");
				String[] sArrs = sArr.split(",");
				
				// 以配置的站点id的个数为基准，作为循环遍历的次数
				int std_num = sidArr.length;
				if(std_num!=snameArr.length || std_num!=typeArr.length || std_num!=sArrs.length){
					log.error("[传播分析]配置错误，参数配置个数不一致，站点名=" + SITE_NAME + "，媒体ID=" + oper.getMEDIA_ID());
					continue;
				}
				
				for(int i=0;i<sidArr.length;i++){
					if("10".equals(pageTypeID)){
						if(!trendjson.contains(sidArr[i]+configure)){							
							int getkpiTrend = getKpiTrend(sidArr[i],typeArr[i],snameArr[i],sArrs[i],configure);
							if(getkpiTrend>0) 
								trendjson.add(sidArr[i]+configure);
						}						
					}else if("11".equals(pageTypeID)||"49".equals(pageTypeID)||"78".equals(pageTypeID)){
						if(!trendjson.contains(sidArr[i]+configure)){
							int getkpiTrend = getKpiTrend(sidArr[i],typeArr[i],snameArr[i],sArrs[i],configure);
							if(getkpiTrend>0) 
								trendjson.add(sidArr[i]+configure);
						}
						if(!kpijson.contains(sidArr[i]+configure)){
							int getkpi = getkpi(sidArr[i],typeArr[i],snameArr[i],configure);
							if(getkpi>0) 
								kpijson.add(sidArr[i]+configure);
						}
					}else if("18".equals(pageTypeID)||"20".equals(pageTypeID)||"23".equals(pageTypeID)||"24".equals(pageTypeID)||"64".equals(pageTypeID)||"65".equals(pageTypeID)||"77".equals(pageTypeID)){	
						if(sidArr.length>4){
							continue;
						}
						if(!kpijson.contains(sidArr[i]+configure)){
							int getkpi = getkpi(sidArr[i],typeArr[i],snameArr[i],configure);
							if(getkpi>0)
								kpijson.add(sidArr[i]+configure);
						}
					}
				}				
			}
		}else{
			return;
		}		
	}
	
	public int getkpi(String sid,String type,String name,String datetype){

		int success = 0;
		TokenEntity entoken = tokenGet.getToken();
		JSONObject definedtime = new JSONObject();
		Map<String,String> map = new HashMap<String,String>();
		if(datetype.equals("24H")){
			definedtime.put("value", 24);
			definedtime.put("type", "H");
			map.put("definedtime", definedtime.toString());
		}else if(datetype.equals("30D")){
			definedtime.put("value", 30);
			definedtime.put("type", "D");
			map.put("definedtime", definedtime.toString());
		}else if(datetype.equals("3D")){
			JSONArray datas = getdata(3);
			map.put("starttime", datas.getString(0));
			map.put("endtime", datas.getString(0));
		}else{ //默认为七天的数据
			definedtime.put("value", 7);
			definedtime.put("type", "D");
			map.put("definedtime", definedtime.toString());
		}
		try {
				String url = BSEntity.getRooturl();
				
				//sJSONObject objres = new JSONObject();
				if ("news".equalsIgnoreCase(type)) {
//					url = url + "/api/transmission/news/kpi";
					url = url + "/api/transmission/source/kpi"; //4.8.31的接口地址（v3.2）
					map.put("sourceids", sid); //4.8.31的接口地址（v3.2）
					map.put("datatype", "news"); //4.8.31的接口地址（v3.2）
				} else if ("app".equalsIgnoreCase(type)) {
//					url = url + "/api/transmission/app/kpi";
					url = url + "/api/transmission/source/kpi"; //4.8.31的接口地址（v3.2）
					map.put("sourceids", sid); //4.8.31的接口地址（v3.2）
					map.put("datatype", "app"); //4.8.31的接口地址（v3.2）
				} else if ("wechat".equalsIgnoreCase(type)) {
//					url = url + "/api/transmission/wechat/kpi";
					url = url + "/api/transmission/source/kpi"; //4.8.31的接口地址（v3.2）
					map.put("sourceids", sid); //4.8.31的接口地址（v3.2）
					map.put("datatype", "wechat"); //4.8.31的接口地址（v3.2）
				} else if ("weibo".equalsIgnoreCase(type)) {
					url = url + "/api/transmission/weibo/kpi";
					map.put("sourceid", sid);
				} else {
					return success;
				}
				map.put("access_token", entoken.getTK_TOKEN());
				map.put("mediaid", "433");
				
				String tokens = httpAPIService.doPost(url, map);
				if (!StringUtils.isBlank(tokens)) {
					JSONObject json = JSONObject.parseObject(tokens);
					int code = json.getIntValue("errcode");
					if (code == 0) {
						JSONObject result = json.getJSONObject("result");
						if("-1064261452".equals(sid)||"1930689975".equals(sid)||"1009493132".equals(sid)
								||"-1565173071".equals(sid)||"-1644145835".equals(sid)||"206775614".equals(sid)){
							log.info(name+sid+":"+datetype+"--"+result);
						}
						
						int pressCount = result.containsKey("pressCount")?result.getIntValue("pressCount"):0;
						int forwardMediaCount = result.containsKey("forwardMediaCount")?result.getIntValue("forwardMediaCount"):0;
						int originalCount = result.containsKey("originalCount")?result.getIntValue("originalCount"):0;
						int forwardCount = result.containsKey("forwardCount")?result.getIntValue("forwardCount"):0;
						int visitCount = result.containsKey("visitCount")?result.getIntValue("visitCount"):0;
						int likeCount = result.containsKey("likeCount")?result.getIntValue("likeCount"):0;
						int rebackCount = result.containsKey("rebackCount")?result.getIntValue("rebackCount"):0;
						int icForwardCount = result.containsKey("icForwardCount")?result.getIntValue("icForwardCount"):0;
						if("weibo".equalsIgnoreCase(type)){
							int nonOriginalCount = result.containsKey("nonOriginalCount")?result.getIntValue("nonOriginalCount"):0;
							pressCount = originalCount + nonOriginalCount;
						}
						SiteKpiEntity  kpiEntity = new SiteKpiEntity();
						kpiEntity.setSYS_DELETEFLAG(0);
						kpiEntity.setST_ID(sid);
						kpiEntity.setST_NAME(name);
						kpiEntity.setST_TYPE(type);
						kpiEntity.setST_PRESSCOUNT(pressCount);
						kpiEntity.setST_FORWARDCOUNT(forwardCount);
						kpiEntity.setST_ORIGINALCOUNT(originalCount);
						kpiEntity.setST_FORWARDMEDIACOUNT(forwardMediaCount);
						kpiEntity.setST_REBACKCOUNT(rebackCount);
						kpiEntity.setST_VISITCOUNT(visitCount);
						kpiEntity.setST_LIKECOUNT(likeCount);
						kpiEntity.setST_FORWARDBYICCOUNT(icForwardCount);
						if(datetype.equals("3天")){
							kpiEntity.setST_COUNTTYPE(4);
						}else{
							kpiEntity.setST_COUNTTYPE(0);
						}
						
						kpiEntity.setST_HOURSITE(null);
						Date date = new Date();
						DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						String time = format.format(date);
						kpiEntity.setST_DAYSITE(time);
						Date date1 = new Date();
						DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String time1 = format1.format(date1);
						kpiEntity.setST_CREATETIME(time1);
						kpiEntity.setST_UPDATETIME(time1);
						int isSend = siteKpiDao.isHave(sid,time,0);
						if(isSend==0){
							success = siteKpiDao.addSiteKpi(kpiEntity);
						}else if(isSend>0){
							success = siteKpiDao.updateSiteKpi(kpiEntity);
						}
					}
				}
				return success;
		} catch (Exception e) {
			return success;
		}	
	}
	public int getKpiTrend(String sid,String type,String name,String sArr,String datetype) {
		//JSONArray resultjsons = new JSONArray();
		int success = 0;
		try {
				// String _source = (String) sources.get(j); //来源
				//JSONObject objjson = new JSONObject();				
				TokenEntity entoken = tokenGet.getToken(); 
				String access_token = entoken.getTK_TOKEN();
				//JSONArray resjson = new JSONArray(); // 最终结果json数据
				JSONObject definedtime = new JSONObject();
				//4.8.31的接口地址（v3.2），必须要传开始日期和截止日期s
				String fmt = "yyyy-MM-dd HH:mm:ss";
				String starttime = "";
				String endtime = DateHelper.getCdateFormat2Str(fmt); //结束时间为当天
				int countType = 1;
				if(datetype.equals("24H")){
					definedtime.put("value", 24);
					definedtime.put("type", "H");
					starttime = DateHelper.date2String(DateHelper.getDateBefore(DateHelper.string2Date(endtime,fmt),1),fmt);
					countType = 1;
					
				}else if(datetype.equals("30D")){
					definedtime.put("value", 30);
					definedtime.put("type", "D");
					starttime = DateHelper.date2String(DateHelper.getDateBefore(DateHelper.string2Date(endtime,fmt),30),fmt);
					countType = 3;
				}else{ //默认为7天的数据
					definedtime.put("value", 7);
					definedtime.put("type", "D");
					starttime = DateHelper.date2String(DateHelper.getDateBefore(DateHelper.string2Date(endtime,fmt),7),fmt);
					countType = 2;
				}
							
				Date date = new Date();
				DateFormat format = new SimpleDateFormat("yyyy");//-MM-dd HH:mm:ss
				String year = format.format(date);
				//String sArr = acArr[i]; //用户行为平台id
				String url = BSEntity.getRooturl();
				String forwardUrl = url + "/api/transmission/source/kpi/forward/trend"; //转载趋势接口  4.8.31的接口地址（v3.2）
				//从"原创发稿趋势"接口获取发稿数、原创数存入json中
				String pressUrl = url + "/api/transmission/source/kpi/press/trend"; //发稿量接口  4.8.31的接口地址（v3.2）
				String datatype = "";
					
				//JSONObject objres = new JSONObject();
				if ("news".equalsIgnoreCase(type)) {
					datatype = "news";
//					url = url + "/api/transmission/news/kpiTrend";
				} else if ("app".equalsIgnoreCase(type)) {
					datatype = "app";
//						url = url + "/api/transmission/app/kpiTrend";
				} else if ("wechat".equalsIgnoreCase(type)) {
					datatype = "wechat";
					//String typ = definedtime.getString("type");
					/*if (typ.equals("H")) {
						log.info("------ 来源为" + _source
									+ "，查询微信传播分析KPI趋势(wechat)数据：开始 ------");
							// 写入数据之前，需要先读取newskpiTrend.json中wechat相关数据，将其数据中数组的第一项去掉，计算最新数据和并放入数组的最后一项
							// 需要的参数列表：access_token、mediaid、sourceid、definedtime
							// url = url + "/api/transmission/wechat/kpiTrend";
							Map<String, String> wechatMap = new HashMap<String, String>();
							wechatMap.put("access_token", access_token);
							wechatMap.put("mediaid", "433");
//							wechatMap.put("sourceid", sid);
							wechatMap.put("sourceids", sid); //4.8.31的接口地址查询类型（v3.2）
							wechatMap.put("starttime", starttime); //4.8.31的接口需要添加上开始时间
							wechatMap.put("endtime", endtime); //4.8.31的接口需要添加上结束时间
							wechatMap.put("definedtime", definedtime.toString());
							wechatMap.put("datatype",datatype); //4.8.31的接口需要添加上访问类型
							String  fid = sid.replace("=", "");
							JSONObject WxKpiTrendObj = getWxKpiTrend(wechatMap,fid,_source);
							//log.info("wechat返回值：WxKpiTrendObj=" + WxKpiTrendObj);
							//log.info("------ 来源为" + _source+ "，查询微信传播分析KPI趋势(wechat)数据：开结束------");
							//从原创发稿趋势接口获取发稿数和原创数
							String pressDatas = httpAPIService.doPost(pressUrl, wechatMap); //获取发稿数、原创数的接口数据
							if(StringUtil.isEmpty(pressDatas)) {
								System.out.println(datatype+"类型的"+sid+"站点返回数据为空！");
								continue;
							}
							JSONObject pressDatasObj = JSONObject.parseObject(pressDatas);
							int errcode_wx = pressDatasObj.getIntValue("errcode"); //返回code码
//							String errmsg_wx = pressDatasObj.getString("errmsg"); //返回结果
							if(errcode_wx == 0) { //返回正确结果
								String result = pressDatasObj.getString("result"); //返回的结果
								JSONObject resultObj = JSONObject.parseObject(result);
								Object pressCount = resultObj.get("pressCount");
								Object originalCount = resultObj.get("originalCount");
								WxKpiTrendObj.put("pressCount", pressCount);
								WxKpiTrendObj.put("originalCount", originalCount);
							}else {
								System.out.println("查询结果错误："+pressDatasObj.toJSONString());
								continue;
							}
							
							objres.put("id", sid);
							objres.put("type", stype);
							objres.put("result", WxKpiTrendObj);
							resjson.add(objres);
							continue;
						} else {*/
							datatype = "wechat";
//							url = url + "/api/transmission/wechat/kpiTrend";
						//}
					} else if ("weibo".equalsIgnoreCase(type)) {
						JSONObject resultjson1 = getweiboKpiTrend("433", sid,
								definedtime.toString(), access_token);
						JSONArray timestamp = resultjson1.getJSONArray("timestamp");
						JSONArray forwardCount= resultjson1.getJSONArray("forwardCount");
						JSONArray rebackCount= resultjson1.getJSONArray("rebackCount");
						JSONArray pressCount= resultjson1.getJSONArray("pressCount");
						JSONArray originalCount= resultjson1.getJSONArray("originalCount"); 
						
						for(int i =0;i<timestamp.size();i++){
							if(timestamp.get(i)==null){
								continue;
							}
							int press =0;							
							int original = 0;
							int forward =0;
							int reback =0;
						    press =pressCount!=null&&pressCount.size()>i?pressCount.getIntValue(i):0;							
						    original = originalCount!=null&&originalCount.size()>i?originalCount.getIntValue(i):0;
						    forward = forwardCount!=null&&forwardCount.size()>i?forwardCount.getIntValue(i):0;
						    reback = rebackCount!=null&&rebackCount.size()>i?rebackCount.getIntValue(i):0;
						    SiteKpiEntity  kpiEntity = new SiteKpiEntity();
							kpiEntity.setSYS_DELETEFLAG(0);
							kpiEntity.setST_ID(sid);
							kpiEntity.setST_NAME(name);
							kpiEntity.setST_TYPE(type);
							kpiEntity.setST_PRESSCOUNT(press);
							kpiEntity.setST_FORWARDCOUNT(forward);
							kpiEntity.setST_ORIGINALCOUNT(original);
							kpiEntity.setST_REBACKCOUNT(reback);
							Date date1 = new Date();
							DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String time = format1.format(date1);
							kpiEntity.setST_CREATETIME(time);
							kpiEntity.setST_UPDATETIME(time);
							kpiEntity.setST_COUNTTYPE(countType);
							//String counttime = "";
							if(countType==1){
								kpiEntity.setST_HOURSITE(year+"-"+timestamp.get(i).toString());
								//counttime = year+"-"+timestamp.get(i).toString();
							}else{
								kpiEntity.setST_DAYSITE(year+"-"+timestamp.get(i).toString());
								//counttime = timestamp.get(i).toString();
							}																					
							int isSend = siteKpiDao.isHave(sid,year+"-"+timestamp.get(i).toString(),countType);
							if(isSend==0){
								success = siteKpiDao.addSiteKpi(kpiEntity);
							}else if(isSend>0){
								success = siteKpiDao.updateSiteKpi(kpiEntity);
							}
							success++;
						}												
						return success;
					}
					Map<String, String> map = new HashMap<String, String>();
					map.put("access_token", access_token);
//					map.put("sourceid", sid);
					map.put("sourceids", sid);//4.8.31的接口地址查询类型（v3.2）
					map.put("mediaid", "433");
					map.put("datatype", datatype); //4.8.31的接口地址查询类型（v3.2）
					map.put("starttime", starttime); //4.8.31的接口需要添加上开始时间
					map.put("endtime", endtime); //4.8.31的接口需要添加上结束时间
					map.put("definedtime", definedtime.toString());

					JSONObject resObj = new JSONObject(); //返回的result结果
					int errcode = 0;
					String errmsg = "";
					String forwardDatas = httpAPIService.doPost(forwardUrl, map); //获取转载量、转载媒体数、重点栏目转载数接口数据
					if(StringUtils.isEmpty(forwardDatas)) {
						System.out.println(datatype+"类型的"+sid+"站点返回数据为空！");
						return success;
					}
					JSONObject forwardDatasObj = JSONObject.parseObject(forwardDatas);
					errcode = forwardDatasObj.getIntValue("errcode"); //返回code码
					errmsg = forwardDatasObj.getString("errmsg"); //返回结果
					if(errcode == 0) { //返回正确结果
						String result = forwardDatasObj.getString("result"); //返回的结果
						JSONObject resultObj = JSONObject.parseObject(result);
						if(resultObj.containsKey("timestampWith7")) { //将带有timestampWith7的时间数据修改为timestamp
							Object timestampWith7Val = resultObj.get("timestampWith7");
							resultObj.put("timestamp", timestampWith7Val);
							resultObj.remove("timestampWith7");
						}
						resObj = resultObj;
					}else {
						log.warn("传播分析kpi查询结果错误："+errmsg + "，返回结果=" + forwardDatasObj.toJSONString());
						return success;
					}
					//获取发稿数和原创数的数据
					String pressDatas = httpAPIService.doPost(pressUrl, map); //获取发稿数、原创数的接口数据
					if(StringUtils.isEmpty(pressDatas)) {
						System.out.println(datatype+"类型的"+sid+"站点返回数据为空！");
						return success;
					}
					JSONObject pressDatasObj = JSONObject.parseObject(pressDatas);
					errcode = pressDatasObj.getIntValue("errcode"); //返回code码
					errmsg = pressDatasObj.getString("errmsg"); //返回结果
					if(errcode == 0) { //返回正确结果
						String result = pressDatasObj.getString("result"); //返回的结果
						JSONObject resultObj = JSONObject.parseObject(result);
						//resObj.put("pressCount", pressCount);
						//resObj.put("originalCount", originalCount);
						JSONArray visitCounts = new JSONArray(); //行为平台的访问量数据
						if(!"0".equals(sArr)){
							TokenEntity actoken = tokenGet.getActToken(); 
							String action_token = actoken.getTK_TOKEN();
							String statUrl = BSEntity.getActUrl()+ "/api/findRealTimeColumnInfo";							
							Map<String, String> statMap = new HashMap<String, String>();
							statMap.put("appKey", BSEntity.getActKey());
							statMap.put("access_token", action_token);
							statMap.put("appid", sArr);
							statMap.put("definedtime", definedtime.toString());
							String statDatas = httpAPIService.doPost(statUrl, statMap);
							if (StringUtils.isEmpty(statDatas)) {
								//log.info("阅读量接口返回为空，请查看与测试接口");
								String pressCounts = resultObj.getString("pressCount"); //从原创文章接口获取发稿量数据
								String[] pressCountsArr = pressCounts.split(",");
								for (String _pressCount : pressCountsArr) {
									int visitCount = Integer.valueOf(_pressCount) * 1534;
									visitCounts.add(visitCount);
								}
							} else {
								JSONObject statDatasObj = JSONObject.parseObject(statDatas);
								int codes = statDatasObj.getIntValue("code");
								if (codes == 1) {
									JSONObject _resObj = statDatasObj.getJSONObject("result");
									JSONArray nums = _resObj.getJSONArray("nums");
									visitCounts =  nums ; 
								} else {
									//log.error("阅读量接口没有正确返回数据");
								}
							}
						}
						//resObj.put("visitCount", visitCounts);
						JSONArray pressCount = resultObj.getJSONArray("pressCount");
						JSONArray originalCount = resultObj.getJSONArray("originalCount");
						JSONArray timestamp = resObj.getJSONArray("timestamp");
						JSONArray forwardMediaCount = resObj.getJSONArray("forwardMediaCount");
						JSONArray forwardByICCount = resObj.getJSONArray("forwardByICCount");
						JSONArray forwardCount = resObj.getJSONArray("forwardCount");
						JSONArray readCount = resObj.getJSONArray("readCount");
						JSONArray likeCount = resObj.getJSONArray("likeCount");
						if(visitCounts.size()>0 && visitCounts.size()+1==pressCount.size()){
							visitCounts.add(0, 0);
						}
						for(int i =0;i<timestamp.size();i++){
							if(timestamp.get(i)==null){
								continue;
							}
							int press = pressCount!=null&&pressCount.size()>i?pressCount.getIntValue(i):0;							
							int original = originalCount!=null&&originalCount.size()>i?originalCount.getIntValue(i):0;
							int forward =forwardMediaCount!=null&&forwardCount.size()>i?forwardCount.getIntValue(i):0;
							int forwardMedia = forwardMediaCount!=null&&forwardMediaCount.size()>i?forwardMediaCount.getIntValue(i):0;
							int forwardByIC = forwardByICCount!=null&&forwardByICCount.size()>i?forwardByICCount.getIntValue(i):0;
							int visitCount = visitCounts!=null&&visitCounts.size()>i?visitCounts.getIntValue(i):0;
							int readNum = readCount!=null&&readCount.size()>i?readCount.getIntValue(i):0;
							int likeNum = likeCount!=null&&likeCount.size()>i?likeCount.getIntValue(i):0;
							SiteKpiEntity  kpiEntity = new SiteKpiEntity();
							kpiEntity.setSYS_DELETEFLAG(0);
							kpiEntity.setST_ID(sid);
							kpiEntity.setST_NAME(name);
							kpiEntity.setST_TYPE(type);
							kpiEntity.setST_PRESSCOUNT(press);
							kpiEntity.setST_FORWARDCOUNT(forward);
							kpiEntity.setST_ORIGINALCOUNT(original);
							kpiEntity.setST_FORWARDMEDIACOUNT(forwardMedia);
							if("wechat".equalsIgnoreCase(type)){
								kpiEntity.setST_VISITCOUNT(readNum);
							}else{
								kpiEntity.setST_VISITCOUNT(visitCount);
							}
							
							kpiEntity.setST_FORWARDBYICCOUNT(forwardByIC);
							kpiEntity.setST_READCOUNT(readNum);
							kpiEntity.setST_LIKECOUNT(likeNum);
							Date date1 = new Date();
							DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String time = format1.format(date1);
							kpiEntity.setST_CREATETIME(time);
							kpiEntity.setST_UPDATETIME(time);
							kpiEntity.setST_COUNTTYPE(countType);
							//String counttime = "";
							if(countType==1){
								kpiEntity.setST_HOURSITE(year+"-"+timestamp.get(i).toString());
								//counttime = year+"-"+timestamp.get(i).toString();
							}else{
								kpiEntity.setST_DAYSITE(year+"-"+timestamp.get(i).toString());
								//counttime = timestamp.get(i).toString();
							}				
														
							int isSend = siteKpiDao.isHave(sid,year+"-"+timestamp.get(i).toString(),countType);
							if(isSend==0){
								success = siteKpiDao.addSiteKpi(kpiEntity);
							}else if(isSend>0){
								success = siteKpiDao.updateSiteKpi(kpiEntity);
							}
							success++;
						}
						return success;
					}else {
						System.out.println("查询结果错误："+pressDatasObj.toJSONString());
						return success;
					}					
		} catch (Exception e) {
			//e.printStackTrace();
			return success;
		}
	}

	/**
	 * 获取微博KPI趋势。（内容大数据4.8.31的接口地址（v3.2）没有变动）
	 * @param mediaid
	 * @param sourceid
	 * @param definedtime
	 * @param token
	 * @return
	 */
	public JSONObject getweiboKpiTrend(String mediaid, String sourceid,
			String definedtime, String token) {

		String url =  BSEntity.getRooturl();

		String url1 = url + "/api/transmission/weibo/kpiTrend/pressAndOriginal";
		String url2 = url + "/api/transmission/weibo/kpiTrend/forwardAndReback";

		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", token);
		map.put("sourceid", sourceid);
		map.put("mediaid", mediaid);
		map.put("definedtime", definedtime);

		String token1 = httpAPIService.doPost(url1, map);
		String token2 = httpAPIService.doPost(url2, map);
		JSONObject resultjson1 = new JSONObject();
		JSONObject resultjson2 = new JSONObject();
		if (!StringUtils.isEmpty(token1)) {
			JSONObject json = JSONObject.parseObject(token1);
			int code = json.getIntValue("errcode");
			if (code == 0) {
				String result1 = json.getString("result");
				resultjson1 = JSONObject.parseObject(result1);
			}
		}
		if (!StringUtils.isEmpty(token2)) {
			JSONObject json = JSONObject.parseObject(token2);
			int code = json.getIntValue("errcode");
			if (code == 0) {
				String result2 = json.getString("result");
				resultjson2 = JSONObject.parseObject(result2);
			}
		}
		if (resultjson1 != null ) {
			if(resultjson1.size()<1){
				resultjson1.put("timestamp", resultjson2.get("timestamp"));
			}
			resultjson1.put("forwardCount", resultjson2.get("forwardCount"));
			resultjson1.put("rebackCount", resultjson2.get("rebackCount"));
		}
		return resultjson1;
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