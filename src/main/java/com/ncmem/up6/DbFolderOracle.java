package com.ncmem.up6;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.ncmem.up6.model.FileInf;
import com.ncmem.up6.sql.SqlExec;
import com.ncmem.up6.sql.SqlParam;
import com.ncmem.up6.sql.SqlWhereMerge;

public class DbFolderOracle extends DbFolder
{
	
	/**
	 * 取同名目录信息
	 * @param pathRel
	 * @param pid
	 * @return
	 */
	public FileInf read(String pathRel, String pid, String id) {
        SqlExec se = new SqlExec();
        if(StringUtils.isBlank(pid)) pid=" ";
        String sql = String.format("select f_id,f_pid,f_pidRoot,f_pathSvr,f_pathRel from up6_files where nvl(f_pid,' ')='%s' and f_pathRel='%s' and f_deleted=0 and f_id!='%s' union select f_id,f_pid,f_pidRoot,f_pathSvr,f_pathRel from up6_folders where nvl(f_pid,' ')='%s' and f_pathRel='%s' and f_deleted=0 and f_id!='%s'", pid,pathRel,id,pid,pathRel,id);
        JSONArray data = se.exec("up6_files", sql, "f_id,f_pid,f_pidRoot,f_pathSvr,f_pathRel","");
        if(data.size() < 1) return null;
        
        JSONObject o = (JSONObject)data.get(0);

        FileInf file = new FileInf();
        file.id = o.getString("f_id").trim();
        file.pid = o.getString("f_pid").trim();
        file.pidRoot = o.getString("f_pidRoot").trim();
        file.pathSvr = o.getString("f_pathSvr").trim();
        file.pathRel = o.getString("f_pathRel").trim();
        return file;
    }
	
	/**
	 * 检查是否存在同名目录
	 * @param name
	 * @param pid
	 * @return
	 */
	public Boolean exist_same_folder(String name,String pid) 
	{
        SqlWhereMerge swm = new SqlWhereMerge();
        swm.equal("f_nameLoc", name.trim());
        swm.equal("f_deleted", 0);
        if(StringUtils.isBlank(pid)) pid=" ";
        swm.equal("nvl(f_pid,' ')", pid);
        String where = swm.to_sql();

        String sql = String.format("(select f_id from up6_files where %s ) union (select f_id from up6_folders where %s)", where,where);

        SqlExec se = new SqlExec();
        JSONArray fid = se.exec("up6_files", sql, "f_id", "");
        return fid.size() > 0;		
	}
	
	public boolean existSameFolder(String name,String pid)
	{
		SqlExec se = new SqlExec();
		
		//子目录
		if(!StringUtils.isEmpty(pid))
		{
			return se.count("up6_folders",new SqlParam[] {
					new SqlParam("f_nameLoc",name),
					new SqlParam("f_pid",pid),
					new SqlParam("f_deleted",false)
			})>0;
		}//根目录
		else
		{
			SqlWhereMerge swm = new SqlWhereMerge();
	        swm.equal("f_nameLoc", name);
	        swm.equal("f_deleted", 0);
	        if(StringUtils.isBlank(pid)) pid=" ";
	        swm.equal("nvl(f_pid,' ')", pid);
	        String where = swm.to_sql();

	        String sql = String.format("select f_id from up6_files where %s", where);

	        JSONArray fid = se.exec("up6_files", sql, "f_id", "");
	        return fid.size() > 0;			
		}
	}
	
	/**
	 * 更新子文件路径
	 * @param pathRelOld
	 * @param pathRelNew
	 */
	public void updatePathRel(String pathRelOld,String pathRelNew)
	{
        //更新子文件路径
        String sql = String.format("update up6_folders set f_pathRel=REPLACE(f_pathRel,'%s/','%s/') where instr(f_pathRel,'%s/')>0",
            pathRelOld,
            pathRelNew,
            pathRelOld
            );
        
        SqlExec se = new SqlExec();
        se.exec(sql);		
	}
}
