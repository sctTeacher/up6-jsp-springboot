package com.ncmem.up6.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ncmem.up6.DBConfig;
import com.ncmem.up6.DbHelper;
import org.apache.commons.lang.StringUtils;

public class SqlWhereMerge {
	Map<String, String> m_cds;
	String m_boolTrue="1";//bool值，默认1,kingbase中True
	String m_boolFalse="0";//
	
	public SqlWhereMerge() 
	{
		this.m_cds = new HashMap<String, String>();
		DBConfig cfg = new DBConfig();
		if(cfg.m_isOdbc)
		{
			this.m_boolTrue="True";
			this.m_boolFalse="False";
		}
	}
	
	public void equal(String n,String v)
	{
		this.m_cds.put(n, String.format("%s='%s'", n,v));
	}
	
	public void equal(String n,int v)
	{
		this.m_cds.put(n, String.format("%s=%d", n,v));
	}
	public void equal(String n,Boolean v)
	{
		String bv = this.m_boolFalse;
		if(v) bv = this.m_boolTrue;
		this.m_cds.put(n,String.format("%s=%s",n,bv));
	}

	public void add(String n,String v)
	{
		if(this.m_cds.containsKey(n)) this.m_cds.remove(n);
		this.m_cds.put(n, v);
	}

	public void charindex(String n,String column)
	{
		String sql = String.format("charindex('%s',%s)=1",n,column);
		this.m_cds.put("", sql);
	}

	public void instr(String n,String column)
	{
		String sql = String.format("instr(%s,'%s')=1",column,n);
		this.m_cds.put("", sql);
	}

	public void like(String n,String v)
	{
		this.m_cds.put(n, String.format("%s like '%%%s%%'", n,v));
	}
	
	public void clear() {this.m_cds.clear();}
	public void del(String n) { this.m_cds.remove(n); }
	
	public String to_sql()
    {
        ArrayList<String> cs = new ArrayList<String>();
		for(Object obj : this.m_cds.keySet())
		{
			Object value = this.m_cds.get(obj );
			cs.add(value.toString());
		}
		
		String[] s = cs.toArray(new String[cs.size()]);

		return StringUtils.join(s, " and ");        
    }
}
