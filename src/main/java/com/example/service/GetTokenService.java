package com.example.service;

import java.util.List;

import com.example.bean.TokenEntity;

public interface GetTokenService {
	public List<TokenEntity> getToken() ;
    public int SaveToken(int i);
    public List<TokenEntity> getActToken();
    public int SaveActToken(int i);
}
