package com.ncmem.down2.biz;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.*;

import com.ncmem.down2.model.DnFileInf;
import com.ncmem.up6.DbHelper;

public class DnFileOdbc extends DnFile
{

    /// <summary>
    /// 获取所有未下载完的文件列表
    /// </summary>
    /// <returns></returns>
    public static String all_uncmp(int uid)
    {
    	StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(" f_id");
        sb.append(",f_nameLoc");
        sb.append(",f_pathLoc");
        sb.append(",f_perLoc");
        sb.append(",f_sizeSvr");
        sb.append(",f_fdTask");
        sb.append(" from down_files");
        sb.append(" where f_uid=? and f_complete=FALSE");

        ArrayList<DnFileInf> files = new ArrayList<DnFileInf>();
		DbHelper db = new DbHelper();
		PreparedStatement cmd = db.GetCommand(sb.toString());
		try
		{
			cmd.setInt(1,uid);
			ResultSet r = db.ExecuteDataSet(cmd);
			while (r.next())
			{
				DnFileInf f		= new DnFileInf();
				f.id			= r.getString(1);
				f.nameLoc		= r.getString(2);
				f.pathLoc		= r.getString(3);
				f.perLoc		= r.getString(4);
				f.sizeSvr		= r.getString(5);
				f.fdTask		= r.getBoolean(6);
			    
				files.add(f);
			}
			cmd.getConnection().close();
			cmd.close();//auto close ResultSet
		}
		catch (SQLException e){e.printStackTrace();}

        Gson g = new Gson();
	    return g.toJson( files );
	}
    
    /**
     * 从up6_files表中获取已经上传完的数据
     * @param uid
     * @return
     */
    public String all_complete(int uid)
    {
    	ArrayList<DnFileInf> files = new ArrayList<DnFileInf>();
    	StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(" f_id");//0
        sb.append(",f_fdTask");//1
        sb.append(",f_nameLoc");//2
        sb.append(",f_sizeLoc");//3
        sb.append(",f_lenSvr");//4
        sb.append(",f_pathSvr");//5
        sb.append(" from up6_files ");
        //
        sb.append(" where f_uid=? and f_deleted=False and f_complete=True and f_fdChild=False and f_scan=True");
		DbHelper db = new DbHelper();
		PreparedStatement cmd = db.GetCommand(sb.toString());
		try
		{
			cmd.setInt(1,uid);
			ResultSet r = db.ExecuteDataSet(cmd);
			while (r.next())
			{
				DnFileInf f		= new DnFileInf();
				String uuid = UUID.randomUUID().toString();
				uuid = uuid.replace("-", "");

				f.id			= uuid;
				f.f_id			= r.getString(1);
				f.fdTask		= r.getBoolean(2);
				f.nameLoc		= r.getString(3);
				f.sizeSvr		= r.getString(4);
				f.lenSvr		= r.getLong(5);
				f.pathSvr		= r.getString(6);
			    
				files.add(f);
			}
			cmd.getConnection().close();
			cmd.close();//auto close ResultSet
		}
		catch (SQLException e){e.printStackTrace();}

        Gson g = new Gson();
	    return g.toJson( files );
    	
    }
}
