package com.ncmem.up6.biz;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.ArrayList;

import com.ncmem.up6.DbHelper;
import com.ncmem.up6.PathTool;
import com.ncmem.up6.model.FileInf;


public class fd_scan 
{
	public FileInf root = null;//根节点
	/// <summary>
    /// 文件列表
    /// </summary>
    protected ArrayList<FileInf> m_files = new ArrayList<FileInf>();
  /// <summary>
    /// 目录列表
    /// </summary>
    protected ArrayList<FileInf> m_folders = new ArrayList<FileInf>();
	
	public fd_scan()
	{
	}
	
	/// <summary>
    /// 覆盖文件
    /// </summary>
    /// <param name="files"></param>
	protected void cover_files(ArrayList<String> files)
	{
		String sql = "update up6_files set f_deleted=1 where f_pathRel=?";
		
		DbHelper db = new DbHelper();
		PreparedStatement cmd = db.GetCommand(sql);
		try {
			cmd.setString(1, "");
			for(String f : files)
	    	{
				cmd.setString(1, f);//id
		        cmd.executeUpdate();
	    	}
			cmd.getConnection().close();
			cmd.close();
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	/// <summary>
    /// 覆盖文件夹
    /// </summary>
    /// <param name="files"></param>
	protected void cover_folders(ArrayList<String> folders)
	{
		String sql = "update up6_folders set f_deleted=1 where f_pathRel=?";
		
		DbHelper db = new DbHelper();
		PreparedStatement cmd = db.GetCommand(sql);
		try {
			cmd.setString(1, "");
			for(String f : folders)
	    	{
				cmd.setString(1, f);//id
		        cmd.executeUpdate();
	    	}
			cmd.getConnection().close();
			cmd.close();
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	protected void GetAllFiles(FileInf parent,String root)
	{
		File dir = new File(parent.pathSvr); 
		File [] allFile = dir.listFiles();
		for(int i = 0; i < allFile.length; i++)
		{
			if(allFile[i].isDirectory())
			{
				FileInf fd = new FileInf();
				String uuid = UUID.randomUUID().toString();
				uuid = uuid.replace("-", "");
				fd.id = uuid;
				fd.pid = parent.id;
				fd.uid = parent.uid;
				fd.pidRoot = this.root.id;
				fd.nameSvr = allFile[i].getName();
				fd.nameLoc = fd.nameSvr;
				fd.pathSvr = allFile[i].getPath();
				fd.pathSvr = fd.pathSvr.replace("\\", "/");
				fd.pathRel = fd.pathSvr.substring(root.length() + 1);
				fd.pathRel = PathTool.combine(parent.pathRel, fd.nameLoc);
				System.out.println("文件夹相对路径：".concat(fd.pathRel));
				fd.perSvr = "100%";
				fd.complete = true;
				this.m_folders.add(fd);
				
				this.GetAllFiles(fd, root);
			}
			else
			{
				FileInf fl = new FileInf();
				String uuid = UUID.randomUUID().toString();
				uuid = uuid.replace("-", "");
				fl.id = uuid;
				fl.pid = parent.id;
				fl.uid = parent.uid;
				fl.pidRoot = this.root.id;
				fl.nameSvr = allFile[i].getName();
				fl.nameLoc = fl.nameSvr;
				fl.pathSvr = allFile[i].getPath();
				fl.pathSvr = fl.pathSvr.replace("\\", "/");
				fl.pathRel = fl.pathSvr.substring(root.length() + 1);
				fl.pathRel = PathTool.combine(parent.pathRel, fl.nameLoc);
				System.out.println("文件相对路径：".concat(fl.pathRel));
				fl.lenSvr = allFile[i].length();
				fl.lenLoc = fl.lenSvr;
				fl.sizeLoc = PathTool.BytesToString(fl.lenLoc);
				fl.perSvr = "100%";
				fl.complete = true;
				this.m_files.add(fl);
			}
		}
	}
	
	/// <summary>
    /// 获取所有文件，并更新相对路径。
    /// 相对路径=父目录.pathRel+f.nameLoc
    /// </summary>
    /// <param name="parent"></param>
    /// <param name="root"></param>
    /// <param name="files"></param>
	protected void getAllFiles(FileInf parent,String root,ArrayList<String> files,ArrayList<String> folders)
	{
		File dir = new File(parent.pathSvr); 
		File [] allFile = dir.listFiles();
		for(int i = 0; i < allFile.length; i++)
		{
			if(allFile[i].isDirectory())
			{
				FileInf fd = new FileInf();
				//String uuid = UUID.randomUUID().toString();
				//uuid = uuid.replace("-", "");
				//fd.id = uuid;
				//fd.pid = parent.id;
				//fd.pidRoot = this.root.id;
				fd.nameSvr = allFile[i].getName();
				fd.nameLoc = fd.nameSvr;
				fd.pathSvr = allFile[i].getPath();
				fd.pathSvr = fd.pathSvr.replace("\\", "/");
				fd.pathRel = fd.pathSvr.substring(root.length() + 1);
				fd.pathRel = PathTool.combine(parent.pathRel, fd.nameLoc);
				System.out.println("文件夹相对路径：".concat(fd.pathRel));
				//fd.perSvr = "100%";
				//fd.complete = true;
				folders.add(fd.pathRel);
				
				this.getAllFiles(fd, root, files, folders);
			}
			else
			{
				FileInf fl = new FileInf();
				//String uuid = UUID.randomUUID().toString();
				//uuid = uuid.replace("-", "");
				//fl.id = uuid;
				//fl.pid = parent.id;
				//fl.pidRoot = this.root.id;
				fl.nameSvr = allFile[i].getName();
				fl.nameLoc = fl.nameSvr;
				fl.pathSvr = allFile[i].getPath();
				fl.pathSvr = fl.pathSvr.replace("\\", "/");
				fl.pathRel = fl.pathSvr.substring(root.length() + 1);
				fl.pathRel = PathTool.combine(parent.pathRel, fl.nameLoc);
				System.out.println("文件相对路径：".concat(fl.pathRel));
				//fl.lenSvr = allFile[i].length();
				//fl.lenLoc = fl.lenSvr;
				//fl.sizeLoc = PathTool.BytesToString(fl.lenLoc);
				//fl.perSvr = "100%";
				//fl.complete = true;
				files.add(fl.pathRel);
			}
		}
	}
	
	/// <summary>
    /// 批量添加文件
    /// </summary>
    /// <param name="con"></param>
	protected void save_files(DbHelper db)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("insert into up6_files (");
		sb.append(" f_id");//1
		sb.append(",f_pid");//2
		sb.append(",f_pidRoot");//3
		sb.append(",f_fdTask");//4
		sb.append(",f_fdChild");//5
		sb.append(",f_uid");//6
		sb.append(",f_nameLoc");//7
		sb.append(",f_nameSvr");//8
		sb.append(",f_pathLoc");//9
		sb.append(",f_pathSvr");//10
		sb.append(",f_pathRel");//11
		sb.append(",f_md5");//12
		sb.append(",f_lenLoc");//13
		sb.append(",f_sizeLoc");//14
		sb.append(",f_lenSvr");//15
		sb.append(",f_perSvr");//16
		sb.append(",f_complete");//17
		
		sb.append(") values(");
		
		sb.append(" ?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(")");

        try {
        	PreparedStatement cmd = db.GetCommand(sb.toString());
			cmd.setString(1, "");//id
			cmd.setString(2, "");//pid
			cmd.setString(3, "");//pidRoot
			cmd.setBoolean(4, false);//fdTask
			cmd.setBoolean(5, true);//f_fdChild
			cmd.setInt(6, 0);//f_uid
			cmd.setString(7, "");//f_nameLoc
			cmd.setString(8, "");//f_nameSvr
			cmd.setString(9, "");//f_pathLoc
			cmd.setString(10, "");//f_pathSvr
			cmd.setString(11, "");//f_pathRel
			cmd.setString(12, "");//f_md5
			cmd.setLong(13, 0);//f_lenLoc
			cmd.setString(14, "");//f_sizeLoc
			cmd.setLong(15, 0);//f_lenSvr	        
			cmd.setString(16, "");//f_perSvr
			cmd.setBoolean(17, true);//f_complete
			
			for(FileInf f : this.m_files)
	    	{
				cmd.setString(1, f.id);//id
		        cmd.setString(2, f.pid);//pid
		        cmd.setString(3, f.pidRoot);//pidRoot
		        cmd.setBoolean(4, false);//fdTask
		        cmd.setBoolean(5, true);//f_fdChild
		        cmd.setInt(6, f.uid);//f_uid
		        cmd.setString(7, f.nameLoc);//f_nameLoc
		        cmd.setString(8, f.nameSvr);//f_nameSvr
		        cmd.setString(9, f.pathLoc);//f_pathLoc
		        cmd.setString(10, f.pathSvr);//f_pathSvr
		        cmd.setString(11, f.pathRel);//f_pathRel
		        cmd.setString(12, f.md5);//f_md5
		        cmd.setLong(13, f.lenLoc);//f_lenLoc
		        cmd.setString(14, f.sizeLoc);//f_sizeLoc
		        cmd.setLong(15, f.lenSvr);//f_lenSvr	        
		        cmd.setString(16, f.perSvr);//f_perSvr
		        cmd.setBoolean(17, true);//f_complete
		        cmd.executeUpdate();
	    	}
			cmd.getConnection().close();
			cmd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/// <summary>
    /// 批量添加目录
    /// </summary>
    /// <param name="con"></param>
	protected void save_folders(DbHelper db)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("insert into up6_folders (");
		sb.append(" f_id");//1
		sb.append(",f_pid");//2
		sb.append(",f_pidRoot");//3
		sb.append(",f_nameLoc");//4
		sb.append(",f_uid");//5
		sb.append(",f_pathLoc");//6
		sb.append(",f_pathSvr");//7
		sb.append(",f_pathRel");//8
		sb.append(",f_complete");//9
		sb.append(") values(");//
		sb.append(" ?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(",?");
		sb.append(")");

        try {
        	PreparedStatement cmd = db.GetCommand(sb.toString());
			cmd.setString(1, "");//id
	        cmd.setString(2, "");//pid
	        cmd.setString(3, "");//pidRoot
	        cmd.setString(4, "");//nameLoc
	        cmd.setInt(5, 0);//f_uid
	        cmd.setString(6, "");//pathLoc
	        cmd.setString(7, "");//pathSvr
	        cmd.setString(8, "");//pathRel
	        cmd.setBoolean(9, true);//complete
	        
	        for(FileInf f : this.m_folders)
	    	{
	        	cmd.setString(1, f.id);//id
		        cmd.setString(2, f.pid);//pid
		        cmd.setString(3, f.pidRoot);//pidRoot
		        cmd.setString(4, f.nameLoc);//nameLoc
		        cmd.setInt(5, f.uid);//f_uid
		        cmd.setString(6, f.pathLoc);//pathLoc
		        cmd.setString(7, f.pathSvr);//pathSvr
		        cmd.setString(8, f.pathRel);//pathRel
		        cmd.setBoolean(9, true);//complete
		        cmd.executeUpdate();
	    	}
	        cmd.getConnection().close();
			cmd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/// <summary>
    /// 覆盖同名文件，更新相对路径
    /// </summary>
    /// <param name="inf"></param>
    /// <param name="pathParent"></param>
	public void cover(FileInf inf,String pathParent) throws SQLException
	{
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<String> folders = new ArrayList<String>();
		this.getAllFiles(inf, pathParent, files, folders);
		this.cover_files(files);
		this.cover_folders(folders);
	}
	
	public void scan(FileInf inf, String root) throws IOException, SQLException
	{
		//扫描文件和目录
		this.GetAllFiles(inf, root);
		
		DbHelper db = new DbHelper();
		this.save_files(db);//保存文件列表
        this.save_folders(db);//保存目录列表
	}
}