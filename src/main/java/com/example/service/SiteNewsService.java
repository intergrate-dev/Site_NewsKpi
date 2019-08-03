package com.example.service;

import java.util.List;

public interface SiteNewsService {
	
	public void addSiteNews(List<String> pageTypeIds, String mediaId); // 站点新闻
	
    void addOriginalNews(List<String> pageTypeIds, String mediaId);

	public void addKeyWordNews(List<String> pageTypeIds, String mediaId); // 站点关键词新闻
	
	public void addZlmtNews(List<String> pageTypeIds, String mediaId);//主流媒体报道

    List<Integer> queryAllMedias();

    void addHotNews(List<String> pageTypeIds, String mediaId);
}
