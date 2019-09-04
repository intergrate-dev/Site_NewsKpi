package com.example.dao;

import java.util.List;

import com.example.bean.TokenEntity;

public interface GetTokenDao {
     public List<TokenEntity> getToken(String soure);
     public int updateToken(TokenEntity token) ;
     public int insertToken(TokenEntity token) ;
}
