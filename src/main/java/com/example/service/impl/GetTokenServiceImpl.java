package com.example.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.BigScreenEntity;
import com.example.bean.TokenEntity;
import com.example.dao.GetTokenDao;
import com.example.http.HttpAPIService;
import com.example.service.GetTokenService;
@Service
public class GetTokenServiceImpl implements GetTokenService{
	@Autowired
	private GetTokenDao TokenDao;
	@Autowired
	private BigScreenEntity BSEntity;
	@Resource
    private HttpAPIService httpAPIService;
	
	/*
	 * 获取大数据内容平台接口授权
	 */
	@Override
	public List<TokenEntity> getToken() {		
		return TokenDao.getToken("sitekpi");
	}
	/*
	 * 保存/修改大数据内容平台授权
	 */
	@Override
	public int SaveToken(int i) {
		String url = BSEntity.getRooturl()+"/api/token";
		//组装参数
		Map<String,String> map = new HashMap<String,String>();
		map.put("appid", BSEntity.getAPPID());
		map.put("secret", BSEntity.getAPPSECRET());
		String tokenRes = httpAPIService.doGet(url,map);
		System.out.println(tokenRes);
		if(tokenRes!=null){
			JSONObject	tokjson = JSONObject.parseObject(tokenRes);
			int code  = tokjson.getIntValue("errcode");
			if(code ==0){
				TokenEntity token  = new TokenEntity();
				token.setTK_TOKEN(tokjson.getString("access_token"));
				DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				//计算接口返回token授权过期时间
				long timeLose=System.currentTimeMillis()+2*60*60*1000;
				String time=format.format(new Date(timeLose));
				token.setTK_EXPIRETIME(time);
				token.setTK_SOURE("sitekpi");
				//保存授权到数据库
				if(i==0){
					return TokenDao.insertToken(token);
				}else{
					return TokenDao.updateToken(token);
				}
			}
		}		
		return 0;
	}
	//获取行为平台授权
	@Override
	public List<TokenEntity> getActToken() {
		return TokenDao.getToken("action");
	}

	@Override
	public int SaveActToken(int i) {
		String url = BSEntity.getActUrl()+"/api/token";
		//组装参数
		Map<String,String> map = new HashMap<String,String>();
		map.put("appKey", BSEntity.getActKey());
		map.put("appSecret", BSEntity.getActSecret());
		String tokenRes = httpAPIService.doPost(url,map);

		if(!StringUtils.isBlank(tokenRes)){
			JSONObject	tokjson = JSONObject.parseObject(tokenRes);
			int code  = tokjson.getIntValue("errcode");
			if(code ==0){
				TokenEntity token  = new TokenEntity();
				token.setTK_TOKEN(tokjson.getString("access_token"));
				int expires_in = tokjson.getIntValue("expires_in");
				//计算授权过期时间
				DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long timeLose=System.currentTimeMillis() + expires_in*1000;
				String time=format.format(new Date(timeLose));
				token.setTK_EXPIRETIME(time);
				token.setTK_SOURE("action");
				if(i==0){
					return TokenDao.insertToken(token);
				}else{
					return TokenDao.updateToken(token);
				}
			}
		}		
		return 0;
	}	
}
