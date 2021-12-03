package com.ncmem.filemgr.controller;

import com.ncmem.up6.WebBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2021/1/7.
 */
@Controller
public class MgrUi {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HttpServletResponse res;

    @RequestMapping("filemgr/index")
    public String index(Model model)
    {
        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("mgrHeader",wb.require(
                wb.m_path.get("jquery"),
                wb.m_path.get("res")+"filemgr.css",
                wb.m_path.get("bootstrap"),
                wb.m_path.get("layerui"),
                wb.m_path.get("moment"),
                wb.m_path.get("vue"),
                wb.m_path.get("fm-up6"),
                wb.m_path.get("fm-down2"),
                wb.m_path.get("root")+ "static/filemgr/data/vue.up6.js",
                wb.m_path.get("root")+ "static/filemgr/data/vue.down2.js",
                wb.m_path.get("root")+ "static/filemgr/data/vue.index.js"
                ) );
        return "filemgr/index";
    }
}
