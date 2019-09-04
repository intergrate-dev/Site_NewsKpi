package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.example.dao.GetTableIdDao;
import com.example.dao.SpreadMapDao;
import com.example.http.HttpAPIService;
import com.example.service.SpreadMapService;

@Service
public class SpreadMapServiceImpl implements  SpreadMapService{
	private static Logger logger = LoggerFactory.getLogger(SpreadMapServiceImpl.class);

	@Autowired
	private SpreadMapDao spreadMapDao;
	@Autowired
	private GetTableIdDao getTableIdDao;
	@Autowired
	private TokenGet tokenGet;
	
	@Autowired
	private BigScreenEntity BSEntity;
	@Resource
    private HttpAPIService httpAPIService;
	@Autowired
	SiteMonitorService siteMonitorService;

	@Override
	public void addSpreadMap(List<String> pageTypeIds, String mediaId){
		TokenEntity entoken = tokenGet.getToken();
		List<GetTableIdEntity> idList = getTableIdDao.getTableIdList("ucspreadmap");
		if(idList == null || idList.size()<1 ){
			return;
		}
		int idSum = idList.get(0).getE5VALUE()-1;
		int doclibid = idList.get(0).getDOCLIBID();
		int foldid = idList.get(0).getFOLDERID();
		String pTypeIds = "13";
		pTypeIds = CommonUtil.setPageTypeIds(pageTypeIds, pTypeIds);
		List<OperationEntity> opList = spreadMapDao.selectSiteId(pTypeIds, mediaId);
		if (opList == null) {
			return;
		}
		//JSONArray kpijson = new JSONArray();
		//JSONArray trendjson = new JSONArray();
		String url = BSEntity.getRooturl();
		url = url + "/api/transmission/source/forward/map";
		JSONObject definedtime = new JSONObject();
		definedtime.put("value",7);
		definedtime.put("type", "D");
		//if(datetype==24){
			//definedtime.put("value",24);
			//definedtime.put("type", "H");
		/*}else if(datetype==7){
			definedtime.put("value",datetype );
			definedtime.put("type", "D");
		}else if(datetype==30){
			definedtime.put("value",datetype );
			definedtime.put("type", "D");
		}else{
			return idSum;
		}*/
		if(opList != null && opList.size()>0){
			for(OperationEntity oper :opList){
				//String pageTypeID = oper.getPageTypeID();
				//String SITE_NAME = oper.getSITE_NAME();
				String SITE_ID = oper.getSITE_ID();
				//String SITE_TYPE = oper.getSITE_TYPE();
				String mediaid = oper.getMEDIA_ID();
				
				Map map = new HashMap();
				map.put("access_token", entoken.getTK_TOKEN());
				map.put("sourceid", SITE_ID);
				//map.put("mediaid", "433");
				map.put("definedtime", definedtime.toString());
				map.put("datatype", "news");
				String tokens = null;
				EnumTask enumTask = EnumTask.SPREADMAP;
				try {
					tokens = httpAPIService.doPost(url, map);
				} catch (Exception e) {
					siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data error");
					logger.error("=============== method: {} fetch data from api, error: {} ============", enumTask, e.getMessage());
					e.printStackTrace();
				}
				if (!StringUtils.isBlank(tokens)) {
					JSONObject json = JSONObject.parseObject(tokens);
					String code = json.getString("errcode");
					if (code != null && "0".equals(code)) {
						JSONObject result = json.getJSONObject("result");
						if (result.size() > 0) {
							JSONObject source = result.getJSONObject("source");
							JSONArray target = result.getJSONArray("target");
							if (target != null && target.size() > 0) {
								StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
								siteMonitorService.parseHandleASync(oper, SiteMonitorEntity.STATUS_START, EnumTask.SPREADMAP);
								logger.info("=============== method: {} mysql update ============", enumTask);
								//spreadMapDao.delectSpreadMap(mediaid,SITE_ID);
								try {
									idSum = this.updateSpreadMap(idSum, doclibid, foldid, SITE_ID, mediaid, source, target);
								} catch (Exception e) {
									siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "update to mysql exception");
									logger.error("=============== method: {} mysql update, error: {} ============", enumTask, e.getMessage());
									e.printStackTrace();
								}
								siteMonitorService.parseHandle(oper, SiteMonitorEntity.STATUS_COMPLETE, EnumTask.SPREADMAP, null);
								logger.info("=============== method: {} mysql update complete ============", enumTask);
							}
						} else {
							siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data empty");
							logger.error("=============== method: {} fetch data from api, error: {} ============", enumTask);
						}
					} else {
						siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data failure");
						logger.error("=============== method: {} fetch data from api, failture: {} ============", enumTask);
					}
				} else {
					siteMonitorService.processFail(oper, SiteMonitorEntity.STATUS_FAIL, enumTask, "fetch data failure");
					logger.error("=============== method: {} fetch data from api, failture: {} ============", enumTask);
				}
			}
		}else{
			return;
		}		
		int maxId= getTableIdDao.selectMaxId("ucspreadmap");
		if(maxId>0){
			getTableIdDao.updateTableId("ucspreadmap", maxId + 1);
		}else{
			getTableIdDao.updateTableId("ucspreadmap", idSum + 1);
		}
	}

	private int updateSpreadMap(int idSum, int doclibid, int foldid, String SITE_ID, String mediaid, JSONObject source, JSONArray target) {
		String MP_SOURCE = source.getString("area");
		for(Object tar : target){
			JSONObject areas = JSONObject.parseObject(tar.toString());
			String area = areas.getString("sourceProvince");
			int  count = areas.getIntValue("forwardCount");
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(date);

			SpreadMapEntity spreadMap = new SpreadMapEntity();
			spreadMap.setSYS_DOCUMENTID(idSum + 1);
			spreadMap.setSYS_DOCLIBID(doclibid);
			spreadMap.setSYS_FOLDERID(foldid);
			spreadMap.setSYS_DELETEFLAG(0);
			spreadMap.setMP_SITEID(SITE_ID);
			spreadMap.setMP_AREA(area);
			spreadMap.setMP_COUNT(count);
			spreadMap.setMP_MADIAID(Integer.valueOf(mediaid));
			spreadMap.setMP_CREATETIME(time);
			spreadMap.setMP_SOURCE(MP_SOURCE);
			int num = spreadMapDao.addSpreadMap(spreadMap);
			if(num==1){
				idSum = idSum+1;
			}
		}
		return idSum;
	}


}
