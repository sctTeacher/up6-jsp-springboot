package com.ncmem.up6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ncmem.up6.model.FileInf;

public class DBFileOdbc extends DBFile{
	
	/// <summary>
	/// 根据文件MD5获取文件信息
	/// 取已上传完的文件
	/// </summary>
	/// <param name="md5"></param>
	/// <param name="inf"></param>
	/// <returns></returns>
	public boolean exist_file(String md5,/*out*/FileInf fileSvr)
	{
		boolean ret = false;
		StringBuilder sb = new StringBuilder();
		sb.append("select");
		sb.append(" f_id");
		sb.append(",f_uid");
		sb.append(",f_nameLoc");
		sb.append(",f_nameSvr");
		sb.append(",f_pathLoc");
		sb.append(",f_pathSvr");
		sb.append(",f_pathRel");
		sb.append(",f_md5");
		sb.append(",f_lenLoc");
		sb.append(",f_sizeLoc");
		sb.append(",f_pos");
		sb.append(",f_lenSvr");
		sb.append(",f_perSvr");
		sb.append(",f_complete");
		sb.append(",f_time");
		sb.append(",f_deleted");
		sb.append(" from up6_files where f_md5=? and f_complete=True order by f_lenSvr DESC limit 0,1");

		DbHelper db = new DbHelper();
		PreparedStatement cmd = db.GetCommand(sb.toString());
		try {
			cmd.setString(1, md5);
			ResultSet r = db.ExecuteDataSet(cmd);
			if (r.next())
			{
				fileSvr.id 			= r.getString(1);
				fileSvr.uid 		= r.getInt(2);
				fileSvr.nameLoc 	= r.getString(3);
				fileSvr.nameSvr 	= r.getString(4);
				fileSvr.pathLoc 	= r.getString(5);
				fileSvr.pathSvr 	= r.getString(6);
				fileSvr.pathRel 	= r.getString(7);
				fileSvr.md5 		= r.getString(8);
				fileSvr.lenLoc 		= r.getLong(9);
				fileSvr.sizeLoc 	= r.getString(10);
				fileSvr.offset 		= r.getLong(11);
				fileSvr.lenSvr 		= r.getLong(12);
				fileSvr.perSvr 		= r.getString(13);
				fileSvr.complete 	= r.getBoolean(14);
				fileSvr.PostedTime 	= r.getDate(15);
				fileSvr.deleted 	= r.getBoolean(16);
				ret = true;
			}
			cmd.getConnection().close();
			cmd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	
	/**
	 * @param f_uid
	 * @param f_id
	 */
	public void fd_complete(String f_id, String uid)
	{
		DbHelper db = new DbHelper();
		Connection con = db.GetCon();
		
		try {
			con.setAutoCommit(false);
			Statement stmt = con.createStatement();
			stmt.addBatch("update up6_files set f_perSvr='100%' ,f_lenSvr=f_lenLoc,f_complete=True where f_id='" + f_id+"'");
			stmt.addBatch("update up6_files set f_perSvr='100%' ,f_lenSvr=f_lenLoc,f_complete=True where f_pidRoot='" + f_id+"'");
			stmt.addBatch("update up6_folders set f_complete=True where f_id='" + f_id + "' and f_uid=" + uid);
			stmt.executeBatch();
			con.commit();
			stmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/// <summary>
	/// 上传完成。将所有相同MD5文件进度都设为100%
	/// </summary>
	public void UploadComplete(String md5)
	{
		String sql = "update up6_files set f_lenSvr=f_lenLoc,f_perSvr='100%',f_complete=True where f_md5=?";
		DbHelper db = new DbHelper();
		PreparedStatement cmd = db.GetCommand(sql);
		
		try 
		{
			cmd.setString(1, md5);
			db.ExecuteNonQuery(cmd);//在部分环境中测试发现执行后没有效果。
		} catch (SQLException e) {e.printStackTrace();}
	}

	public void delFolder(String id,int uid)
	{
		DbHelper db = new DbHelper();
		Connection con = db.GetCon();
		
		try {
			con.setAutoCommit(false);
			Statement stmt = con.createStatement();
			stmt.addBatch("update up6_files set f_deleted=True where f_id='" + id + "' and f_uid=" + uid);
			stmt.addBatch("update up6_files set f_deleted=True where f_pidRoot='" + id + "' and f_uid=" + uid);
			stmt.addBatch("update up6_folders set f_deleted=True where f_id='" + id + "' and f_uid=" + uid);
			stmt.executeBatch();
			con.commit();
			stmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
