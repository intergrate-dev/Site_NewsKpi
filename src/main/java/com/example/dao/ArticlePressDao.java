package com.example.dao;

import java.util.List;

import com.example.bean.ArticleNewsKpiEntity;
import com.example.bean.OperationEntity;

public interface ArticlePressDao {

	public int addArticlePress(ArticleNewsKpiEntity articlePress);
	
	public List<OperationEntity> selectArticlePressId(String pTypeIds, String mediaId);
	
	public int updateArticlePress( ArticleNewsKpiEntity articlePress);

	public int deleteArticlePress( String mediaId, String id, int type,String keyword);

	public int isHave(String mediaId, String time,String keyword);

	public int getcount(String siteId);
}
