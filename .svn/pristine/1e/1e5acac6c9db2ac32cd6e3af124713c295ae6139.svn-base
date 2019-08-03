package com.example.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.bean.BigScreenEntity;
import com.example.bean.EventTrackEntity;
import com.example.bean.OperationEntity;
import com.example.bean.TokenEntity;
import com.example.commons.DateHelper;
import com.example.commons.TokenGet;
import com.example.commons.enumeration.ApiTypes;
import com.example.dao.EventTrackDao;
import com.example.http.HttpAPIService;
import com.example.service.EventTrackService;

@Service
public class EventTrackServiceImpl implements EventTrackService {
	
	private final Logger log = LoggerFactory.getLogger(EventTrackServiceImpl.class);
	
	@Autowired
	private EventTrackDao eventTrackDao;
	@Autowired
	private BigScreenEntity BSEntity;
	@Autowired
	private TokenGet tokenGet;
	@Resource
    private HttpAPIService httpAPIService;

	@Override
	public void saveTopicEvolution() { // 话题演化
		handleData(ApiTypes.TOPICEVOLUTION.type(), ApiTypes.TOPICEVOLUTION.url());
	}

	@Override
	public void saveHotWordsRec() {  // 热词推荐
		handleData(ApiTypes.HOTWORDREC.type(), ApiTypes.HOTWORDREC.url());
	}

	@Override
	public void saveTodayTopics() { // 今日话题
		handleData(ApiTypes.TODAYTOPICS.type(), ApiTypes.TODAYTOPICS.url());
	}

	@Override
	public void saveEmotionAnalysis() { // 情感分析
		handleData(ApiTypes.EMOTIONANALYSIS.type(), ApiTypes.EMOTIONANALYSIS.url());
	}

	@Override
	public void saveChannelType() {  // 渠道分析
		handleData(ApiTypes.CHANNELTYPE.type(), ApiTypes.CHANNELTYPE.url());
	}

	@Override
	public void saveReportTrend() { // 报道趋势
		handleData(ApiTypes.REPORTTREND.type(), ApiTypes.REPORTTREND.url());
	}
	
	@Override
	public void saveReportsRank() { // 媒体报道量排行
		handleData(ApiTypes.REPORTSRANK.type(), ApiTypes.REPORTSRANK.url());
	}
	
	@Override
	public void getEvenIdByMcode() {//根据媒体获取事件id
		
		List<OperationEntity>  entity = eventTrackDao.queryEventIdsByMcode("80");
		if(entity!=null && entity.size()>0){
			for(int i =0;i<entity.size();i++){
				OperationEntity oper = entity.get(i);
				String acode = oper.getConfigure();
			    String Ext  = oper.getExtfileds();
			    JSONObject fileds = JSONObject.parseObject(Ext);
				JSONArray evenJson = getEvenIdByAcode(acode);
				fileds.put("mcodes", evenJson);
				oper.setExtfileds(fileds.toString());
				eventTrackDao.addEventIDs(oper);
				}
			}
		
	}
	
	/**
	 * 根据接口类型进行数据处理
	 * @return
	 */
	private void handleData(int aType, String url){
		
		log.info("[事件追踪],apiType="+aType+"，开始数据写入...");
		// 获取当前的时间，作为删除旧数据的时间节点
		String currentTime = DateHelper.parse2String(new Date(), DateHelper.DateFormat.YMDHMS);
		log.info("[事件追踪],apiType="+aType+",删除数据时间节点：" + currentTime);
		
		// 从数据库配置表中查询需要同步数据的事件ID
		List<String> eventIds = getSyncEventIds();
		if(null!=eventIds && eventIds.size()>0){
			log.info("[事件追踪],已配置的事件ID="+ eventIds);
			for(String eventId : eventIds){
				if(1==aType||2==aType||3==aType){
					mergeMultipleObject(eventId, aType, url, currentTime);
				}else if(4==aType||5==aType||6==aType){
					mergeOneObject(eventId, aType, url, currentTime);
				}else if(7==aType){
					mergeMediaReportsRank(eventId, aType, url, currentTime);
				}
			}
		}else{
			log.info("[事件追踪],未查询到事件ID的配置数据！");
		}
		log.info("[事件追踪],apiType="+aType+"，结束数据写入...");
	}
	
	/**
	 * 处理话题演化、热词推荐、今日话题接口返回的json数组数据
	 * @return
	 */
	private void mergeMultipleObject(String eventId ,int aType, String url, String timeNode){
		
		List<EventTrackEntity> etList = new ArrayList<EventTrackEntity>();
		JSONObject etJsonObj = this.sendPostRequest(aType, url, eventId, null);
		if(null != etJsonObj){
			JSONArray data = new JSONArray();
			if(3==aType){
				data = etJsonObj.getJSONArray("documents");
			}else{
				data = etJsonObj.getJSONArray("data");
			}
			if(null!=data && data.size()>0){
				for(int i=0; i<data.size();i++){
					JSONObject subJson = data.getJSONObject(i);
					try {
						etList.add(jsonConvertToObject(subJson, eventId, aType, 
								etJsonObj.getString("lastmodified")));
					} catch (ParseException e) {
						e.printStackTrace();
					} 
				}
				// 开始更新数据
				if(etList.size()>0){
					int resultNum = multipleDataToDB(etList);
					
					// 删除条件
					if((1==aType && resultNum<Integer.parseInt((ApiTypes.TOPICEVOLUTION.shownum())))
						|| (2==aType && resultNum<Integer.parseInt((ApiTypes.HOTWORDREC.shownum())))
						|| (3==aType && resultNum<Integer.parseInt((ApiTypes.TODAYTOPICS.shownum())))){
						log.info("[事件追踪],新增数据不满足数量要求，不执行删除操作！事件ID="+eventId+",apiType="+aType
								+",新增数据："+resultNum);
						return;
					}
					// 开始删除旧数据
					int delNum = eventTrackDao.deleteEventTrack(Integer.parseInt(eventId), aType, timeNode);
					log.info("清理[事件追踪]数据,事件ID="+eventId+",apiType="+aType
							+",新增数据："+resultNum+",删除数据：" + delNum);
				}
			}
		}else{
			log.error("[事件追踪],事件ID="+eventId+",apiType="+aType+ ",数据获取失败！！！");
		}
	}
	
	/**
	 * 处理情感分析、渠道类型、报道趋势接口返回的json对象数据
	 * @return
	 */
	private void mergeOneObject(String eventId ,int aType, String url, String timeNode){
		JSONObject etJsonObj = this.sendPostRequest(aType, url, eventId, null);
		if(null != etJsonObj){
			JSONObject data = etJsonObj.getJSONObject("data");
			if(null!=data){
				EventTrackEntity et = null;
				try {
					et = jsonConvertToObject(data, eventId, aType, 
							etJsonObj.getString("lastmodified"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				// 开始更新数据
				if(null!=et){
					int resultNum = singleDataToDB(et);
					// 开始删除旧数据
					if(resultNum>0){
						int delNum = eventTrackDao.deleteEventTrack(Integer.parseInt(eventId), aType, timeNode);
						log.info("清理[事件追踪]数据,事件ID="+eventId+",apiType="+aType
								+",新增数据："+resultNum+",删除数据：" + delNum);
					}
				}
			}
		}else{
			log.error("[事件追踪],事件ID="+eventId+",apiType="+aType+ ",数据获取失败！！！");
		}
	}
	
	/**
	 * 处理媒体报道量排行接口返回的json对象数据
	 * @return
	 */
	private void mergeMediaReportsRank(String eventId ,int aType, String url, String timeNode){
		
		JSONObject rankJson = new JSONObject();
		// 先获取当天的报道量排行，并降序排序，存入json对象
		rankJson = reportsDataRank(eventId, aType, url, 1, rankJson);
		// 再获取7天内的报道量排行，并降序排序，存入json对象
		rankJson = reportsDataRank(eventId, aType, url, 7, rankJson);
		
		int todayNum = rankJson.getIntValue("todaynum");
		int day7Num = rankJson.getIntValue("day7num");
		int limitNum = Integer.parseInt(ApiTypes.REPORTSRANK.limit());
		log.info("[事件追踪]，媒体报道量排行结果：" + rankJson.toString());
		
		rankJson.remove("todaynum");
		rankJson.remove("day7num");
		
		// 当返回数据中今日排行和一周排行为空时，通过旧数据进行补充
		if(rankJson.size()<2){
			JSONObject oldJson = eventTrackDao.queryReportRankByEventId(Integer.parseInt(eventId));
			if(rankJson.getJSONObject("today")==null || rankJson.getJSONObject("today").size()==0){
				rankJson.put("today", oldJson.getJSONObject("today"));
			}
			if(rankJson.getJSONObject("day7")==null || rankJson.getJSONObject("day7").size()==0){
				rankJson.put("day7", oldJson.getJSONObject("day7"));
			}
		}
		
		EventTrackEntity et = null;
		try {
			et = jsonConvertToObject(rankJson, eventId, aType, null);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// 开始更新数据
		if(null!=et){
			int resultNum = singleDataToDB(et);
			// 开始删除旧数据，注意删除条件
			if(resultNum>0 /*&& day7Num>=limitNum && todayNum>=limitNum*/){
				int delNum = eventTrackDao.deleteEventTrack(Integer.parseInt(eventId), aType, timeNode);
				log.info("清理[事件追踪]数据,事件ID="+eventId+",apiType="+aType
						+",新增数据："+resultNum+",删除数据：" + delNum);
			}
		}
	}
	
	/**
	 * 调用接口对数据进行排行
	 * @return
	 */
	private JSONObject reportsDataRank(String eventId ,int aType, String url, int days, JSONObject sortJson){
		
		JSONObject paramsJson = new JSONObject();
		
		/*TokenEntity token = tokenGet.getToken();
		paramsJson.put("token", token.getTK_TOKEN());*/
		if(1==days){  // 当天日期
			paramsJson.put("starttime", DateHelper.getCurrentDate() + " 00:00:00");
		}else{ // 获取6天前零点时间
			paramsJson.put("starttime", DateHelper.date2String(DateHelper.getDateBefore(new Date(), 6)) + " 00:00:00");
		}
		paramsJson.put("endtime", DateHelper.date2String(new Date(), DateHelper.YMDHMS));
		paramsJson.put("limit", ApiTypes.REPORTSRANK.limit());
		
		JSONObject etJsonObj = this.sendPostRequest(aType, url, eventId, paramsJson);
		if(null != etJsonObj){
			JSONObject data = etJsonObj.getJSONObject("data");
			log.info("[事件追踪]，媒体声量排行接口参数=" + paramsJson.toString() + "，接口返回结果：" + data.toString());
			if(null!=data && data.size()>0){
				sortJson = getRankData(data, sortJson , days);
			}
		}else{
			log.error("[事件追踪],事件ID="+eventId+",apiType="+aType+ ",数据获取失败！！！");
		}
		
		return sortJson;
	}
	
	/**
	 * 对结果进行排序
	 * @return
	 */
	private JSONObject getRankData(JSONObject jsonData, JSONObject rankResult, int type){
		
		Map<String, Integer> rankMap = new HashMap<String, Integer>();
		
		// 遍历json对象
		Set<String> mediaType = jsonData.keySet();
		for(String jsonKey : mediaType){
			JSONObject media = jsonData.getJSONObject(jsonKey);
			if(null!=media && media.size()>0){
				JSONArray nameArray = media.getJSONArray("names");
				JSONArray dcidArray = media.getJSONArray("dcids");
				JSONArray countArray = media.getJSONArray("counts");
				if(null == dcidArray || dcidArray.size()<=0){
					continue;
				}
				int mediaNum = dcidArray.size();
				// 媒体名数组、媒体ID数组、报道量数组，只要长度不一致，则视为异常数据，不进行处理
				if(mediaNum != nameArray.size() || mediaNum != countArray.size()){
					continue;
				}
				
				String mapKey = "";
				// 以"dcids"数组为标准进行遍历
				for(int i=0;i<mediaNum;i++){
					mapKey = nameArray.getString(i) + "_" + dcidArray.getString(i)+"_"+jsonKey;
					rankMap.put(mapKey, countArray.getInteger(i));
				}
			}
		}
		
		if(rankMap.size()>0){ 
			// 开始对所有媒体进行排行,并取前20条数据
			Map<String, Integer> resultMap = sortByValueDescending(rankMap);
			JSONArray mediaName = new JSONArray();
			JSONArray mediaCount = new JSONArray();
			JSONArray mediatypes = new JSONArray();
			int count=0;
			int rankNum = Integer.parseInt(ApiTypes.REPORTSRANK.limit());
			for(Map.Entry<String, Integer> entry : resultMap.entrySet()){
				// 只取排行前20
				if(count >= rankNum) break;
				String[] res = entry.getKey().split("_");
				String name = res[0];
				String mtype  = res[res.length-1];
				if(null!=entry.getValue() && null!=name && !name.isEmpty()){
					mediaName.add(name);
					mediaCount.add(entry.getValue());
					mediatypes.add(mtype);
					count++;
				}
			}
			
			JSONObject rankJson = new JSONObject();
			rankJson.put("names", mediaName);
			rankJson.put("counts", mediaCount);
			rankJson.put("types", mediatypes);
			if(1==type){ // 当天
				rankResult.put("today", rankJson);
				rankResult.put("todaynum", mediaCount.size());
			}else{ // 7天内
				rankResult.put("day7", rankJson);
				rankResult.put("day7num", mediaCount.size());
			}
		}
		return rankResult;
	}
	
	/**
	 * 更新数据库中的多条数据
	 * 主要用于：话题演化、热词推荐、今日话题
	 * @param etList aType
	 * @return
	 */
	private int multipleDataToDB(List<EventTrackEntity> etList){
		
		int count = 0;
		if(null == etList || etList.size()<=0){
			return count;
		}
		for(EventTrackEntity et : etList){ // 循环插入	
			if(singleDataToDB(et)>0){ // 写入成功
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 更新数据库中的一条数据
	 * 主要用于：情感分析、渠道类型分析、报道趋势
	 * @param et
	 * @return
	 */
	private int singleDataToDB(EventTrackEntity et){
		return eventTrackDao.addEventTrack(et);
	}
	
	/**
	 * 将返回的json值封装为事件追踪对象
	 * @return
	 * @throws ParseException 
	 */
	private EventTrackEntity jsonConvertToObject(JSONObject jo, String eventId, int aType,
			String apiLastmodified) throws ParseException{
		
		EventTrackEntity et = new EventTrackEntity();
		
		et.setEventId(Integer.parseInt(eventId));
		et.setDataType(aType);
		et.setDeleteFlag(0);
		
		// 时间的处理放在一起
		long currentTimeStamp = System.currentTimeMillis();
		et.setLastModifiedTime(DateHelper.timeStamp2Date(currentTimeStamp));
		if(null!=apiLastmodified && !apiLastmodified.isEmpty()){
			et.setApiLastModifiedTime(DateHelper.string2Date(apiLastmodified, "yyyy-MM-dd HH:mm:ss"));
		}
			
		// 其他属于接口的字段赋值
		if(jo.containsKey("title") || jo.containsKey("keyword")){
			if(1==aType||3==aType){
				et.setContent(jo.getString("title"));
			}else if(2==aType){
				et.setContent(jo.getString("keyword"));
			}
		}		
		if(jo.containsKey("count") || jo.containsKey("frequency")){
			if(1==aType||3==aType){
				et.setCount(jo.getInteger("count"));
			}else if(2==aType){
				et.setCount(jo.getInteger("frequency"));
			}
		}
		if(3==aType){
			if(jo.containsKey("pubdate")){
				if(null!=jo.get("pubdate")){
					et.setClusterTime(DateHelper.timeStamp2Date(jo.getLongValue("pubdate")));
				}
			}
		}
		if(jo.containsKey("clusterTime")){
			if(null!=jo.get("clusterTime")){
				et.setClusterTime(DateHelper.timeStamp2Date(jo.getLongValue("clusterTime")));
			}
		}
		
		if(4==aType){ // 情感分析
			et.setEmotionAnalysis(jo.toJSONString());
		}else if(5==aType){ // 渠道类型
			et.setChannelType(jo.toJSONString());
		}else if(6==aType){ // 报道趋势
			et.setReportTrend(jo.toJSONString());
		}else if(7==aType){ // 媒体报道量排行
			et.setReportsRank(jo.toJSONString());
		}
		return et;
	}
	
	/**
	 * 发送POST请求获取数据，返回JSON对象
	 * @return
	 */
	private JSONObject sendPostRequest(int aType, String url, String eventId, JSONObject rpsBank){
		
		Map<String, String> params = new HashMap<String, String>();
		// 每个事件追踪接口都需要token
		TokenEntity token = tokenGet.getToken();
		params.put("access_token", token.getTK_TOKEN());
		if(2==aType){ // 热词推荐
			params.put("limit", ApiTypes.HOTWORDREC.limit());
		}
		if(3==aType){ // 今日话题
			//params.put("limit", ApiTypes.TODAYTOPICS.limit());
			params.put("pagesize", ApiTypes.TODAYTOPICS.limit());
		}
		if(7==aType){ // 媒体报道量排行
			//params.put("access_token", rpsBank.getString("token"));
			params.put("starttime", rpsBank.getString("starttime"));
			params.put("endtime",rpsBank.getString("endtime"));
			params.put("limit", rpsBank.getString("limit"));
		}
		params.put("id", eventId);
		
		String apiURL = BSEntity.getRooturl() + url;
		
		JSONObject etJson = null;
		String resultData = httpAPIService.doPost(apiURL, params);
		if(StringUtils.isBlank(resultData)){
			log.info("[事件追踪]接口返回结果为空,事件ID=" + eventId + "调用接口地址：" + apiURL);
		}else{
			etJson = JSONObject.parseObject(resultData);
			if(etJson.containsKey("errcode")){
				if(0 != etJson.getIntValue("errcode")){ // 返回异常结果
					log.error("[事件追踪]查询结果错误,事件ID=" + eventId + ",返回消息：" + etJson.getString("errmsg"));
					return null;
				}
			}else{
				if(0 != etJson.getIntValue("status")){ // 返回异常结果
					log.error("[事件追踪]查询结果错误,事件ID=" + eventId + ",返回消息：" + etJson.getString("errmsg"));
					return null;
				}
			}
			
		}
		params = null;
		return etJson;
	}
	
	/**
	 * 获取需要同步数据的事件ID
	 * @return
	 */
	public List<String> getSyncEventIds(){
		String eventIdStr = "";
		
		List<OperationEntity>  entity = eventTrackDao.queryEventIdsByMcode("46,80");
		if(entity!=null && entity.size()>0){
			for(int i =0;i<entity.size();i++){
				String pageid = entity.get(i).getPageTypeID();
				if("80".equals(pageid)){
					String acode = entity.get(i).getExtfileds();
					JSONObject  ext = JSONObject.parseObject(acode)	;
					JSONArray mcodes =ext.containsKey("mcodes")? ext.getJSONArray("mcodes"):null;
					if(mcodes!=null&&mcodes.size()>0){
						for(Object obj :mcodes){
							JSONObject mcode = JSONObject.parseObject(obj.toString());
							eventIdStr += mcode.getIntValue("id") + ",";
						}
					}
				}else{
					String Configure = entity.get(i).getConfigure();
					if(null!=Configure){
						eventIdStr += Configure + ",";
					}
				}
			}
		}
		//String eventIdStr = eventTrackDao.queryEventIdsByConfig();
		List<String> eventIdList = new ArrayList<String>();
		if(!eventIdStr.isEmpty()){
			String[] etIds = eventIdStr.split(",");
			for(String etId : etIds){
				if(!etId.isEmpty() && !eventIdList.contains(etId)){
					eventIdList.add(etId);
				}
			}
		}
		return eventIdList;
	}
	public JSONArray getEvenIdByAcode(String acode){
		TokenEntity entoken = tokenGet.getToken();
		String url = BSEntity.getRooturl() + "/api/event/list";
		JSONArray  mcodes = new JSONArray();
		String pscode =getmcode(acode);
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", pscode+"."+entoken.getTK_TOKEN());
		//System.out.println(pscode+"."+token.getString("access_token"));
		map.put("system", "1");
		map.put("enabletrace", "2");
		map.put("order", "1");
		try {
			String tokens = httpAPIService.doPost(url, map);
			if (tokens != null && !"".equals(tokens)) {
				JSONObject json = JSONObject.parseObject(tokens);
				int status = json.getIntValue("errcode");
				if (status == 0) {
					//JSONObject docs = json.getJSONObject("data");
					JSONArray jsons  = json.getJSONArray("events");
					if (jsons.size() > 0) {
						for(Object doc : jsons){
							JSONObject  res = new JSONObject();
							JSONObject  codes = JSONObject.parseObject(doc.toString());
							res.put("id", codes.getIntValue("id"));
							res.put("name", codes.getString("name"));
							res.put("remark", codes.getString("remark"));
							mcodes.add(res);
							if(mcodes.size()>2){
								break;
							}
						}
					} 
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			log.error("运行异常:", e);
			return null;
		}
		return mcodes;
	}
	public String getmcode(String code){
		String uids = "mbd";
		JSONObject mcode = new JSONObject();
		mcode.put("mcode", code);
		mcode.put("ucode", uids);
		 String text = mcode.toString();
		 byte[] textByte;
		try {
			textByte = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		//编码
	    Base64.Encoder encoder = Base64.getEncoder();
	    String encodedText = encoder.encodeToString(textByte);
		//System.out.println(encodedText);
	    String pscode = URLEncoder.encode(encodedText);
		return pscode;
	}
	/**
	 * 对集合MAP根据值的大小进行降序排列
	 * @param jsonData
	 * @return
	 */
    public <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return -compare;
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }



}
