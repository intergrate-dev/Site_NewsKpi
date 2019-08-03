package com.example.dao;

import java.util.List;

import com.example.bean.EmotionEntity;
import com.example.bean.OperationEntity;

public interface EmotionDao {

	public int addEmotion(EmotionEntity emotion);
	
	public int updateEmotion(String siteItem,String mediaId,String emotion);
	
	public int isHave(String siteItem,String mediaId);
	public List<OperationEntity> selectEmotionId(String pageTypeIds, String mediaId);
}