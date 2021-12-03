package com.ncmem.up6;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import net.sf.json.JSONObject;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Created by jmzy on 2021/1/5.
 */
public class ConfigReader {
    public JSONObject m_files;
    public PathTool m_pt;
    public ReadContext m_jp;

    public ConfigReader(){
        this.m_pt = new PathTool();
        ClassPathResource res = new ClassPathResource("config/config.json");
        try {
            String json = FileTool.readAll(res.getFile());
            this.m_files = JSONObject.fromObject(json);
            this.m_jp = JsonPath.parse(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将配置加载成一个json对象
     * @param name
     * @return
     */
    public JSONObject module(String name)
    {
        String path = this.m_jp.read(name);
        ClassPathResource res = new ClassPathResource(path);
        String data = null;
        try {
            data = FileTool.readAll(res.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.fromObject(data);
    }

    public JSONObject read(String name)
    {
        Object o = this.m_jp.read(name);
        return JSONObject.fromObject(o);
    }

    public String readString(String name)
    {
        Object o = this.m_jp.read(name);
        return o.toString();
    }
}
