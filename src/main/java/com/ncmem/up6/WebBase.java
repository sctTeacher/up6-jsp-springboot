package com.ncmem.up6;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WebBase {
	public JSONObject m_cofig;
	public ConfigReader m_webCfg;
	public JSONObject param;
	public PathTool m_pt;
	public JSONObject m_path;
	private HttpServletRequest m_req;
	private HttpServletResponse m_res;

	public WebBase(HttpServletRequest req,HttpServletResponse res) {
		this.m_req = req;
		this.m_res = res;
		this.param = new JSONObject();
		this.m_pt = new PathTool();
		this.m_webCfg = new ConfigReader();
		this.m_path = this.m_webCfg.module("path");
		this.regParamRequest();
		this.regParamPath();
	}
	
	public String paramPage() {
        String v = String.format("<script>var page=%s;</script>", this.param.toString());        
        return v;
	}

    public void regParamPath()
    {
        JSONObject o = this.m_webCfg.module("path");
        this.param.put("path", o);
    }
    
    public void regParamRequest() 
    {
    	JSONObject o = new JSONObject();
    	HttpServletRequest req = this.m_req;

    	Enumeration paramNames = req.getParameterNames();
    	
    	  while (paramNames.hasMoreElements()) {
    	   String paramName = (String) paramNames.nextElement();
    	   
    	   String[] paramValues = req.getParameterValues(paramName);
    	   if (paramValues.length == 1) {
    	    String paramValue = paramValues[0];
    	    if (paramValue.length() != 0) {
    	     //System.out.println("参数：" + paramName + "=" + paramValue);
    	     o.put(paramName, paramValue);
    	    }
    	   }
    	  }

    	
    	this.param.put("query", o);

    }
    
    public JSONObject request_to_json() 
    {
    	JSONObject o = new JSONObject();
		HttpServletRequest req = this.m_req;
    	Enumeration paramNames = req.getParameterNames();
    	
    	  while (paramNames.hasMoreElements()) {
    	   String paramName = (String) paramNames.nextElement();
    	   
    	   String[] paramValues = req.getParameterValues(paramName);
    	   if (paramValues.length == 1) {
    	    String paramValue = paramValues[0];
    	    if (paramValue.length() != 0) {
    	     //System.out.println("参数：" + paramName + "=" + paramValue);
    	    paramValue = paramValue.trim();
    	    	o.put(paramName, paramValue);
    	    }
    	   }
    	  }
    	  
    	return o;
    }
    
    public String reqToString(String name) 
    {
    	String v = this.queryString(name);
    	return v;
    }

	public String reqStringDecode(String name)
	{
		String v = this.queryString(name);
		return PathTool.url_decode(v);
	}
    
    public boolean reqToBool(String name)
    {
    	String v = this.queryString(name);
    	return v.equalsIgnoreCase("true");
    }
    
    public int reqToInt(String name)
    {
    	String v = this.queryString(name);
    	if(StringUtils.isBlank(v)) return 0;
    	return Integer.parseInt(v);
    }
    
    public Object path(String name)
    {
    	return this.m_path.get(name);
    }
    
    public String toInclude(String file)
    {
        Boolean css = file.toLowerCase().endsWith("css");
        if (css)
        {
            return String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\" />"
                , file);
        }
        else
        {
            return String.format("<script type=\"text/javascript\" src=\"%s\" charset=\"%s\"></script>"
                , file
                , "utf-8");
        }
    }

    public String require(Object ... ps)
    {
        StringBuilder sb = new StringBuilder();
        for (Object f : ps)
        {
            //字符串
            if (f instanceof String)
            {
                sb.append( toInclude(f.toString()));
            }
            else if(f instanceof JSONArray)
            {
            	JSONArray arr = JSONArray.fromObject(f);
            	for(int i = 0 ; i<arr.size();++i)
            	{
            		String obj = arr.get(i).toString();
            		sb.append(this.toInclude(obj));
            	}
            	
            }//json object
            else if(f instanceof  JSONObject)
            {
            	sb.append(this.toInclude(f.toString()));                
            }
        }
        return sb.toString();
    }
    
    /// <summary>
    /// 加载模板文件,
    /// </summary>
    /// <param name="file">模板文件相对路径,/data/tmp.html</param>
    /// <returns></returns>
    public String template(String file)
    {
        HtmlTemplate ht = new HtmlTemplate();
        ht.setFile(file);
        return ht.toString();
    }
    
    public String queryString(String n) 
    {
    	String v = this.m_req.getParameter(n);
    	if(StringUtils.isBlank(v)) v = "";
    	v = v.trim();
    	return v;
    }
    
    /**
     * 向页面输出数据
     */
    public void toContent(JSONObject o) 
    {
		try {
			this.m_res.getOutputStream().print(o.toString());
			this.m_res.getOutputStream().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    
    public void toContent(JSONArray o)
    {
		try 
		{
			this.m_res.getOutputStream().print(o.toString());
			this.m_res.getOutputStream().close();
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
    
    public void toContent(String o)
    {
		try {
			this.m_res.getOutputStream().print(o.toString());
			this.m_res.getOutputStream().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      	
    }
}