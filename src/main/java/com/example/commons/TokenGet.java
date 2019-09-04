package com.example.commons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.bean.TokenEntity;
import com.example.service.GetTokenService;

@Component
public class TokenGet {
	@Autowired
	private GetTokenService TokenService;

	public TokenEntity getToken(){
		//从授权表中查询授权
		List<TokenEntity> list = TokenService.getToken();
		//判断库中是否存在授权
		if(list==null){
			//若不存在调取接口获取储存
			TokenService.SaveToken(0);			
		}else{
			//判断获取的token是否过期
			String expireTime = list.get(0).getTK_EXPIRETIME();
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(date);
			if (expireTime != null) {
				int res = time.compareTo(expireTime);
				if (res >= 0) {
					//若授权已失效，重新获取储存
					TokenService.SaveToken(1);
				}
			} else {
				
				//返回授权				
				return (TokenEntity)list.get(0);
			}
		}
		//返回授权
		return TokenService.getToken().get(0);
	}
	
	public TokenEntity getActToken(){
		List<TokenEntity> Actlist = TokenService.getActToken();	
		if(Actlist==null){
			TokenService.SaveActToken(0);			
		}else{
			String expireTime = Actlist.get(0).getTK_EXPIRETIME();
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(date);
			if (expireTime != null) {
				int res = time.compareTo(expireTime);
				if (res >= 0) {
					TokenService.SaveActToken(1);
				}
			} else {
				return (TokenEntity)Actlist.get(0);
			}
		}
		return TokenService.getActToken().get(0);
	}
}
