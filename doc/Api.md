http://115.29.214.129:8019/

### 1. Api list
#### request
uri: /api/list
method: get

#### response
```
{
    "status": 0,
    "message": "请求成功",
    "data": {
        # 大屏mediaId
        "medias": [
            14,
            15,
            ...
            109,
            110,
            111
        ],
        "apis": [
            {
                # 定时任务名称
                "c_name": "站点新闻更新",
                "params": [
                    {
                        # 值：id数组
                        "input": [
                            14,
                            15,
                            16,
                            17,
                            83
                        ],
                        # 参数名： 页面ids
                        "name": "pageTypeIds"
                    },
                    {
                        # 值： mediaId
                        "input": 110,
                        # 参数名： 大屏Id
                        "name": "mediaId"
                    }
                ],
                # 接口uri: /api/siteNews
                "uri": "siteNews"
            },
            {
                "c_name": "关键词kpi更新",
                "params": [
                    {
                        "input": [
                            74,
                            75
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "keyWordKpi"
            },
            {
                "c_name": "站点关键词新闻更新",
                "params": [
                    {
                        "input": [
                            39,
                            45,
                            72
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "keyWordNews"
            },
            {
                "c_name": "站点热点新闻更新",
                "params": [
                    {
                        "input": [
                            14,
                            15,
                            16,
                            17
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "hotNews"
            },
            {
                "c_name": "原创新闻更新",
                "params": [
                    {
                        "input": [
                            14,
                            15,
                            16,
                            17
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "addOriginalNews"
            },
            {
                "c_name": "情感分析更新",
                "params": [
                    {
                        "input": [
                            72
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "emotion"
            },
            {
                "c_name": "站点关键词新闻更新",
                "params": [
                    {
                        "input": [
                            45,
                            68,
                            69,
                            70
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "zlmtNews"
            },
            {
                "c_name": "文章传播分析数据更新",
                "params": [
                    {
                        "input": [
                            63
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "articlePress"
            },
            {
                "c_name": "事件追踪处理更新",
                "params": null,
                "uri": "eventTrack"
            },
            {
                "c_name": "传播地图更新",
                "params": [
                    {
                        "input": [
                            13
                        ],
                        "name": "pageTypeIds"
                    },
                    {
                        "input": 110,
                        "name": "mediaId"
                    }
                ],
                "uri": "spreadMap"
            }
        ]
    }
}
```


### 2. Api siteNews
 #### request
 uri: /api/list
 method: post
 param:
    mediaId=110
    pageTypeIds=14,15,18,19
    
 #### response
 ```
 # sucess
{
    "status": 0,
    "message": "请求成功, 完成更新的页面[14,15,18,19]",
    "data": {}
}

# fail
{
    "status": -1,
    "message": "对已更新过的大屏页面不要频繁操作，3小时后执行有效！",
    "data": null
}

 ```
 