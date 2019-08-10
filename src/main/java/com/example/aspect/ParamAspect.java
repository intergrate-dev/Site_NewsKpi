package com.example.aspect;

import com.example.redis.AppPrefix;
import com.example.redis.RedisService;
import com.example.util.DateParseUtil;
import com.founder.ark.common.utils.bean.ResponseObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Aspect
@Component
public class ParamAspect {
    private static final Logger logger = LoggerFactory.getLogger(ParamAspect.class);

    @Autowired
    RedisService redisService;

    @Pointcut("@annotation(com.example.anotation.ApiCheck)")
    public void log() {
    }

    //@Before("log()")
    public ResponseObject doBefore(JoinPoint joinPoint) {
        return null;
    }

    @Around("log()")
    public Object doAround(ProceedingJoinPoint proJoinPoint) throws Throwable {
        String api = "/api/".concat(proJoinPoint.getSignature().getName());
        long time_1 = System.currentTimeMillis();
        logger.info("--------------------- request api: {}, exec start time at {}", api, DateParseUtil.dateTimeToString(new Date()));
        Object[] args = proJoinPoint.getArgs();
        String mediaId = null;
        if (args.length > 0) {
            mediaId = (String) args[0];
            logger.info("================ parame mediaId: {} ==================", mediaId);
        }
        if (args.length > 1) {
            logger.info("================ parame pageTypeIds: {} ==================", args[1]);
            List<String> pageTypeIds = (List<String>) args[1];
            List<String> tmp = new ArrayList<>();
            for (String ptId : pageTypeIds) {
                String key = api.concat("-").concat(mediaId).concat("-").concat(ptId);
                if (redisService.exists(AppPrefix.appPrefix, key)) {
                    tmp.add(ptId);
                } else {
                    redisService.set(AppPrefix.appPrefix, key, 0);
                }
            }
            logger.info("--------------- ptId: {} be removed from pageTypeIds -------------", tmp.toString());
            for (String s : tmp) {
                pageTypeIds.remove(s);
            }
            logger.info("================ parame pageTypeIds remained: {} ==================", args[1]);
            if (pageTypeIds.size() == 0) {
                logger.info("================ pageTypeIds is empty, then terminal ================");
                return ResponseObject.newErrorResponseObject(-1, "对已更新过的大屏页面不要频繁操作，3小时后执行有效！");
            }
        }

        Object result = proJoinPoint.proceed();
        long t = (System.currentTimeMillis() - time_1) / 1000;
        logger.info("--------------------- request api: {}, exec end time at {}, take time in total: {}小时{}分{}秒", api,
                DateParseUtil.dateTimeToString(new Date()), t / 3600, t % 3600 / 60, t % 60);
        return result;
    }

    //@After("")
    public void doAfter(JoinPoint joinPoint) {
        logger.info("******拦截后的逻辑******");
    }


}
