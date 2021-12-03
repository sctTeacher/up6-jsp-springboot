package com.ncmem.up6;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2021/1/5.
 */
@RestController
public class IndexController {
    @RequestMapping("/")
    public String inex(){
        return  "hello,up6";
    }
}
