package com.ncmem.down2.biz;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.ncmem.up6.sql.SqlExec;
import com.ncmem.up6.sql.SqlParam;

/**
 * 文件夹下载处理
 * @author zysoft 2019-05-30
 *
 */
public class DnFolder {

	public String childs(String pidRoot)
	{
		SqlExec se = new SqlExec();
		JSONArray fs = se.select("up6_files", "f_id,f_nameLoc,f_pathSvr,f_pathRel,f_lenSvr", new SqlParam[] {new SqlParam("f_pidRoot",pidRoot)},"");

		JSONArray childs = new JSONArray();
		for(int i = 0 , l = fs.size() ; i<l;++i)
		{
			JSONObject item = new JSONObject();
			JSONObject f = (JSONObject)fs.get(i);
			item.put("f_id", f.getString("f_id") );
			item.put("nameLoc", f.getString("f_nameLoc"));
			item.put("pathSvr", f.getString("f_pathSvr"));
			item.put("pathRel", f.getString("f_pathRel"));
			item.put("lenSvr", f.getLong("f_lenSvr"));
			childs.add(item);
		}
		return childs.toString();
	}
}
