package com.example.test.controller;

import com.example.test.service.DocService;
import com.practice.bus.bean.DocInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/doc")
public class DocController {
    private static Logger logger = LoggerFactory.getLogger(DocController.class);

    @Autowired
    DocService docService;

    /**
     * 添加文档
     */
    @RequestMapping("/createDoc")
    @ResponseBody
    public String createDoc() {
        DocInfo docInfo = null;
        for(long i =21;i<23;i++) {
            docInfo = new DocInfo();
            docInfo.setDocId(i);
            docService.createDoc(docInfo);
        }
        return "add success";
    }

    /**
     * 修改文档
     */
    @RequestMapping("/modifyDoc")
    @ResponseBody
    public String modifyDoc() {
        DocInfo docInfo = null;
        for(long i = 21;i<23;i++) {
            docInfo = new DocInfo();
            docInfo.setDocId(i);
            docInfo.setDocName("文档--" + i + ".doc");
            docService.modifyDoc(docInfo);
        }
        return "modify success";
    }

    /**
     * 删除文档
     */
    @RequestMapping("/deleteDoc")
    @ResponseBody
    public String deleteDoc() {
        DocInfo docInfo = new DocInfo();
        docInfo.setDocId(11L);
        docService.deleteDoc(docInfo);
        return "delete success";
    }


}
