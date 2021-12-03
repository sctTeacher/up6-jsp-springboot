package com.ncmem.up6.controller;

import com.ncmem.up6.WebBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2021/1/6.
 */
@Controller
public class Up6Ui {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HttpServletResponse res;

    @RequestMapping("up6/index")
    public String up6(Model model)
    {
        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("up6Header",wb.require( wb.m_path.get("up6") ) );
        return "up6/index";
    }

    /**
     * 单面板示例
     * @param model
     * @return
     */
    @RequestMapping("up6/panel")
    public String panel(Model model) {

        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("up6Header",wb.require( wb.m_path.get("up6-panel") ) );
        return "up6/panel";
    }

    @RequestMapping("up6/single")
    public String single(Model model)
    {
        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("up6Header",wb.require( wb.m_path.get("up6-single") ) );
        return "up6/single";
    }

    @RequestMapping("up6/vue")
    public String vue(Model model) {
        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("up6Header",wb.require( wb.m_path.get("up6") ) );
        return "up6/vue";
    }

    @RequestMapping("up6/upload")
    public String upload() {
        return "up6/test";
    }
}
