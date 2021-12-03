package com.ncmem.down2.controller;

import com.ncmem.up6.WebBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jmzy on 2021/1/6.
 */
@Controller
public class DownUi {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HttpServletResponse res;

    @RequestMapping("down2/index")
    public String index(Model model)
    {
        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("down2Header",wb.require( wb.m_path.get("down2") ) );
        return "down2/index";
    }

    @RequestMapping("down2/ligerui")
    public String ligerui(Model model)
    {
        WebBase wb = new WebBase(req,res);
        model.addAttribute("pageParam",wb.paramPage());
        model.addAttribute("ligerui",wb.require( wb.m_path.get("ligerui") ) );
        model.addAttribute("down2Header",wb.require( wb.m_path.get("down2") ) );
        return "down2/ligerui";
    }
}
