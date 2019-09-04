package com.example.configure;

import com.example.commons.AppConfig;
import com.example.redis.AppPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AppConfigFactory {
    @Autowired
    AppConfig appConfig;

    @Bean
    public AppPrefix initAppPrefix(){
        AppPrefix.appPrefix = AppPrefix.getInstance();
        AppPrefix.appPrefix.setExpireSeconds(Integer.parseInt(appConfig.getExpire()));
        AppPrefix.appPrefix.setPrefix(appConfig.getIdentify() + "-" + appConfig.getPrefix() + ":");
        return AppPrefix.appPrefix;
    }

}
