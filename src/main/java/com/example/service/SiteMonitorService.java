package com.example.service;

import com.example.bean.OperationEntity;
import com.example.bean.SiteMonitorEntity;
import com.practice.bus.bean.EnumTask;

import java.util.List;

public interface SiteMonitorService {
    void parseHandleASync(OperationEntity oper, String status, EnumTask task);

    void parseHandle(OperationEntity oper, String status, EnumTask task, String info);

    void processFail(OperationEntity oper, String statusFail, EnumTask keywordnews, String fetch_data_failure);
}
