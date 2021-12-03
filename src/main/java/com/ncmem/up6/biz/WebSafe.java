package com.ncmem.up6.biz;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;
import com.ncmem.up6.ConfigReader;
import com.ncmem.up6.CryptoTool;
import com.ncmem.up6.model.FileInf;


public class WebSafe {
	
	public WebSafe(){}
	
	/// <summary>
    /// 验证token
    /// </summary>
    /// <param name="token"></param>
    /// <param name="f"></param>
    /// <returns></returns>
	public boolean validToken(String token, FileInf f)
	{
		String action = "init";
		//加密
		ConfigReader cr = new ConfigReader();
		JSONObject sec = cr.module("path");
		JSONObject security = sec.getJSONObject("security");
		boolean encrypt = security.getBoolean("token"); 
		if (encrypt)
		{
			if(StringUtils.isBlank(token.trim())) return false;
			CryptoTool ct = new CryptoTool();
			return StringUtils.equals(ct.token(f,action), token);
		}
		return true;
	}

	/// <summary>
    /// 验证token
    /// </summary>
    /// <param name="token"></param>
    /// <param name="f"></param>
    /// <returns></returns>
	public boolean validToken(String token, FileInf f,String action)
	{
		//加密
		ConfigReader cr = new ConfigReader();
		JSONObject sec = cr.module("path");
		JSONObject security = sec.getJSONObject("security");
		boolean encrypt = security.getBoolean("token"); 
		if (encrypt)
		{
			if(StringUtils.isBlank(token.trim())) return false;
			CryptoTool ct = new CryptoTool();
			return StringUtils.equals(ct.token(f,action), token);
		}
		return true;
	}
}
