package com.ncmem.up6.biz;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

import com.ncmem.up6.*;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.ncmem.up6.model.FileInf;
import com.ncmem.up6.sql.SqlExec;
import com.ncmem.up6.sql.SqlParam;
import com.ncmem.up6.sql.SqlWhereMerge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 文件管理器页面逻辑
 * @author zysoft
 *
 */
public class PageFileMgr {
	WebBase m_wb;
	private HttpServletRequest m_req;
	private HttpServletResponse m_res;

	public PageFileMgr(HttpServletRequest req,HttpServletResponse res)
	{
		this.m_req = req;
		this.m_res = res;
		this.m_wb = new WebBase(req,res);

		String op = req.getParameter("op");
		if(StringUtils.equals("data", op)) this.load_data();
		else if(StringUtils.equals("data", op)) this.load_data();
		else if(StringUtils.equals("search", op)) this.search();
		else if(StringUtils.equals("rename", op)) this.rename();
		else if(StringUtils.equals("del", op)) this.del();
		else if(StringUtils.equals("del-batch", op)) this.del_batch();
		else if(StringUtils.equals("path", op)) this.build_path();
		else if(StringUtils.equals("mk-folder", op)) this.mk_folder();
		else if(StringUtils.equals("uncomp", op)) this.uncomp();
		else if(StringUtils.equals("uncmp-down", op)) this.uncmp_down();
		else if(StringUtils.equals("tree", op)) this.load_tree();
		else if(StringUtils.equals("f_create", op)) this.f_create();
		else if(StringUtils.equals("fd_create", op)) this.fd_create();
		else if(StringUtils.equals("fd_data", op)) this.fd_data();
	}

	void fd_data()
	{
		String id		= this.m_wb.queryString("id");
		String callback = this.m_wb.queryString("callback");
		
		if (StringUtils.isBlank(id))
		{
			this.m_wb.toContent(callback + "({\"value\":null})");	
			return;
		}
		FolderBuilder fb = new FolderBuilder();
		Gson gson = new Gson();
		String json = gson.toJson(fb.build(id));
		
		try {
			json = URLEncoder.encode(json,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//编码，防止中文乱码
		json = json.replace("+","%20");
		json = callback + "({\"value\":\"" + json + "\"})";//返回jsonp格式数据。
		this.m_wb.toContent(json);
	}
	
	void f_create() 
	{
		String pid      = this.m_wb.queryString("pid");
		String pidRoot  = this.m_wb.queryString("pidRoot");
		if(StringUtils.isBlank(pidRoot)) pidRoot = pid;//当前文件夹是根目录
		String id		= this.m_wb.queryString("id");
		String md5 		= this.m_wb.queryString("md5");
		String uid 		= this.m_wb.queryString("uid");
		String lenLoc 	= this.m_wb.queryString("lenLoc");//数字化的文件大小。12021
		String sizeLoc 	= this.m_wb.queryString("sizeLoc");//格式化的文件大小。10MB
		String callback = this.m_wb.queryString("callback");
		String pathLoc	= this.m_wb.queryString("pathLoc");
		pathLoc			= PathTool.url_decode(pathLoc);
		String pathRel  = this.m_wb.queryString("pathRel");
		pathRel = PathTool.url_decode(pathRel);
		
		//参数为空
		if (	StringUtils.isBlank(md5)
			&& StringUtils.isBlank(uid)
			&& StringUtils.isBlank(sizeLoc))
		{
			this.m_wb.toContent(callback + "({\"value\":null})");	
			return;
		}
		
		FileInf fileSvr= new FileInf();
		fileSvr.id = id;
		fileSvr.pid = pid;
		fileSvr.pidRoot = pidRoot;
		fileSvr.fdChild = !StringUtils.isBlank(pid);
		fileSvr.uid = Integer.parseInt(uid);
		fileSvr.nameLoc = PathTool.getName(pathLoc);
		fileSvr.pathLoc = pathLoc;
		fileSvr.pathRel = PathTool.combine(pathRel, fileSvr.nameLoc);
		fileSvr.lenLoc = Long.parseLong(lenLoc);
		fileSvr.sizeLoc = sizeLoc;
		fileSvr.deleted = false;
		fileSvr.md5 = md5;
		fileSvr.nameSvr = fileSvr.nameLoc;
		
		//所有单个文件均以uuid/file方式存储
		PathBuilderUuid pb = new PathBuilderUuid();
		try {
			fileSvr.pathSvr = pb.genFile(fileSvr.uid,fileSvr);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		fileSvr.pathSvr = fileSvr.pathSvr.replace("\\","/");
		
		
		//同名文件检测
		/*DbFolder df = new DbFolder();
		if (df.exist_same_file(fileSvr.nameLoc,pid))
		{
		    String data = callback + "({'value':'','ret':false,'code':'101'})";    
		    this.m_wb.toContent(data);
		    return;
		}*/
		
		DBConfig cfg = new DBConfig();
		DBFile db = cfg.db();
		FileInf fileExist = new FileInf();
			
		boolean exist = db.exist_file(md5,fileExist);
		//数据库已存在相同文件，且有上传进度，则直接使用此信息
		if(exist && fileExist.lenSvr > 1)
		{
			fileSvr.nameSvr			= fileExist.nameSvr;
			fileSvr.pathSvr 		= fileExist.pathSvr;
			fileSvr.perSvr 			= fileExist.perSvr;
			fileSvr.lenSvr 			= fileExist.lenSvr;
			fileSvr.complete		= fileExist.complete;
			db.Add(fileSvr);
			
			//触发事件
		    up6_biz_event.file_create_same(fileSvr);
		}//此文件不存在
		else
		{
			db.Add(fileSvr);
			//触发事件
		    up6_biz_event.file_create(fileSvr);
			
			FileBlockWriter fr = new FileBlockWriter();
			fr.CreateFile(fileSvr.pathSvr,fileSvr.lenLoc);
		}
		
		//加密
		ConfigReader cr = new ConfigReader();
		JSONObject sec = cr.module("path");
		JSONObject security = sec.getJSONObject("security");
		boolean encrypt = security.getBoolean("encrypt"); 
		if (encrypt)
		{
			CryptoTool ct   = new CryptoTool();
		    try{
		    	fileSvr.pathSvr = ct.encrypt(fileSvr.pathSvr.getBytes("UTF-8"));
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(fileSvr);
		
		try {
			json = URLEncoder.encode(json,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//编码，防止中文乱码
		json = json.replace("+","%20");
		json = callback + "({\"value\":\"" + json + "\",\"ret\":true})";//返回jsonp格式数据。
		this.m_wb.toContent(json);
		
	}
	void fd_create() {
		String id       = this.m_wb.queryString("id");
		String pid      = this.m_wb.queryString("pid");
		String pidRoot  = this.m_wb.queryString("pidRoot");
		if( StringUtils.isBlank(pidRoot)) pidRoot = pid;//父目录是根目录
		String uid      = this.m_wb.queryString("uid");
		String lenLoc   = this.m_wb.queryString("lenLoc");
		String sizeLoc  = this.m_wb.queryString("sizeLoc");
		String pathLoc  = this.m_wb.queryString("pathLoc");
		String pathRel  = this.m_wb.queryString("pathRel");
		pathRel 		= PathTool.url_decode(pathRel);
		pathLoc			= PathTool.url_decode(pathLoc);		
		String callback = this.m_wb.queryString("callback");//jsonp
		
		
		//参数为空
		if (StringUtils.isBlank(id)
			|| StringUtils.isBlank(uid)
			|| StringUtils.isBlank(pathLoc))
		{
			this.m_wb.toContent(callback + "({\"value\":null})");
			return;
		}
		
		FileInf fileSvr = new FileInf();
		fileSvr.id      = id;
		fileSvr.pid     = pid;
		fileSvr.pidRoot = pidRoot;
		fileSvr.fdChild = false;
		fileSvr.fdTask  = true;
		fileSvr.uid     = Integer.parseInt(uid);
		fileSvr.nameLoc = PathTool.getName(pathLoc);
		fileSvr.pathLoc = pathLoc;
		fileSvr.pathRel = PathTool.combine(pathRel, fileSvr.nameLoc);
		fileSvr.lenLoc  = Long.parseLong(lenLoc);
		fileSvr.sizeLoc = sizeLoc;
		fileSvr.deleted = false;
		fileSvr.nameSvr = fileSvr.nameLoc;
		
		//检查同名目录
		/*DbFolder df = new DbFolder();
		if (df.exist_same_folder(fileSvr.nameLoc, pid))
		{
			JSONObject o = new JSONObject();
			o.put("value","");
			o.put("ret", false);
			o.put("code", "102");    
		    String js = callback + String.format("(%s)", o.toString());    
		    this.m_wb.toContent(js);
		    return;
		}*/
		
		//生成存储路径
		PathBuilderUuid pb = new PathBuilderUuid();
		try {
			fileSvr.pathSvr = pb.genFolder(fileSvr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileSvr.pathSvr = fileSvr.pathSvr.replace("\\","/");
		PathTool.createDirectory(fileSvr.pathSvr);
		
		//添加到数据表
		DBConfig cfg = new DBConfig();
		DBFile db = cfg.db();
		if(StringUtils.isBlank(pid)) db.Add(fileSvr);
		else db.addFolderChild(fileSvr);
		
		//加密
		ConfigReader cr = new ConfigReader();
		JSONObject sec = cr.module("path");
		JSONObject security = sec.getJSONObject("security");
		boolean encrypt = security.getBoolean("encrypt"); 
		if (encrypt)
		{
			CryptoTool ct   = new CryptoTool();
			try{
				fileSvr.pathSvr = ct.encrypt(fileSvr.pathSvr.getBytes("UTF-8"));
			}
			catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
		up6_biz_event.folder_create(fileSvr);
		
		Gson g = new Gson();
		String json = g.toJson(fileSvr);
		try {
			json = URLEncoder.encode(json,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		json = json.replace("+","%20");
		
		JSONObject ret = new JSONObject();
		ret.put("value",json);
		ret.put("ret",true);
		json = callback + String.format("(%s)",ret.toString());//返回jsonp格式数据。
		this.m_wb.toContent(json);
	}

    void mk_folder()
    {
        String name = this.m_wb.reqToString("f_nameLoc");
        name = PathTool.url_decode(name);
        String pid = this.m_wb.reqToString("f_pid");
        int uid = this.m_wb.reqToInt("uid");
        String pidRoot = this.m_wb.reqToString("f_pidRoot");
        String pathRel = this.m_wb.reqToString("f_pathRel");
        pathRel = PathTool.url_decode(pathRel);
        pathRel = PathTool.combine(pathRel, name);
        
        DBConfig cfg = new DBConfig();
        DbFolder df = cfg.folder();
        if (df.exist_same_folder(name, pid))
        {
        	JSONObject ret = new JSONObject();
        	ret.put("ret", false);
        	ret.put("msg", "已存在同名目录");            
            this.m_wb.toContent(ret);
            return;
        }

        SqlExec se = new SqlExec();
    	String guid = PathTool.guid();
    	
        //根目录
        if ( StringUtils.isBlank(pid) )
        {
            se.insert("up6_files", new SqlParam[] {
                new SqlParam("f_id",guid)
                ,new SqlParam("f_pid",pid )
                ,new SqlParam("f_uid",uid )
                ,new SqlParam("f_pidRoot",pidRoot )
                ,new SqlParam("f_nameLoc",name )
                ,new SqlParam("f_complete",true)
                ,new SqlParam("f_fdTask",true)
                ,new SqlParam("f_pathRel",pathRel)
            });
        }//子目录
        else
        {
            se.insert("up6_folders"
                , new SqlParam[] {
                new SqlParam("f_id",guid)
                ,new SqlParam("f_pid",pid)
                ,new SqlParam("f_uid",uid)
                ,new SqlParam("f_pidRoot",pidRoot )
                ,new SqlParam("f_nameLoc",name )
                ,new SqlParam("f_complete",true)
                ,new SqlParam("f_pathRel",pathRel)
                });
        }

        JSONObject ret = new JSONObject();
        ret.put("ret", true);
        ret.put("f_id", guid);
        ret.put("f_pid", pid);
        ret.put("f_uid", uid);
        ret.put("f_pidRoot", pidRoot);
        ret.put("f_nameLoc", name);
        ret.put("f_pathRel", pathRel);
        this.m_wb.toContent(ret);
    }
    
    void build_path()
    {
        String data = this.m_wb.queryString("data");
        data = PathTool.url_decode(data);
        JSONObject fd = JSONObject.fromObject(data);

        DbFolder df = new DbFolder();

        this.m_wb.toContent( df.build_path(fd) );
    }
    
    void load_tree() {
        String pid = this.m_req.getParameter("pid");
        SqlWhereMerge swm = new SqlWhereMerge();
        swm.equal("f_fdChild", 0);
        swm.equal("f_fdTask", 1);
        swm.equal("f_deleted", 0);
        if (!StringUtils.isBlank(pid)) swm.equal("f_pid", pid);

        SqlExec se = new SqlExec();
        JSONArray arr = new JSONArray();
        JSONArray data = se.select("up6_files"
            , "f_id,f_pid,f_pidRoot,f_nameLoc"
            , swm.to_sql()
            ,"");

        //查子目录
        if (!StringUtils.isBlank(pid))
        {
            data = se.select("up6_folders"
                , "f_id,f_pid,f_pidRoot,f_nameLoc"
                , new SqlParam[] {
                    new SqlParam("f_pid", pid)
                    ,new SqlParam("f_deleted", false)
                },"");
        }

        for(int i = 0 , l = data.size() ; i<l;++i)
        {
            JSONObject item = new JSONObject();
            JSONObject f = (JSONObject)data.get(i);
            item.put("id", f.getString("f_id") );
            item.put("text", f.getString("f_nameLoc"));
            item.put("parent", "#");
            item.put("nodeSvr", f);
            arr.add(item);
        }
        this.m_wb.toContent(arr);
    }
	
	public void load_data() {
		String pid = this.m_wb.queryString("pid");
		String pageSize = this.m_wb.queryString("limit");
		String pageIndex = this.m_wb.queryString("page");
		if(StringUtils.isBlank(pageSize)) pageSize = "20";
		if(StringUtils.isBlank(pageIndex)) pageIndex = "1";
		String pathRel = this.m_wb.reqStringDecode("pathRel");
		pathRel += '/';
		
		SqlWhereMerge swm = new SqlWhereMerge();
		DBConfig cfg = new DBConfig();
		//if( !StringUtils.isBlank(pid)) swm.equal("f_pid", pid);
		if( !StringUtils.isBlank(pid)) 
		{
	    	if(StringUtils.equals(cfg.m_db, "sql")) 
	    	{
	    		swm.add("f_pathRel", String.format("f_pathRel='%s'+f_nameLoc",pathRel));
	    	}
	    	else
	    	{
	    		swm.add("f_pathRel", String.format("f_pathRel=CONCAT('%s',f_nameLoc)",pathRel));
	    	}
		}
			
		swm.equal("f_uid", this.m_wb.reqToInt("uid"));
		swm.equal("f_complete", 1);
		swm.equal("f_deleted", 0);
		swm.equal("f_fdChild", 1);
		
		Boolean isRoot = StringUtils.isBlank(pid);
		if(isRoot) swm.equal("f_fdChild", 0);		
		
		String where = swm.to_sql();
		
		//加载文件列表
		SqlExec se = new SqlExec();
		JSONArray files = se.page("up6_files"
				,"f_id"
				, "f_id,f_pid,f_nameLoc,f_sizeLoc,f_lenLoc,f_time,f_pidRoot,f_fdTask,f_pathSvr,f_pathRel"
				, Integer.parseInt( pageSize )
				, Integer.parseInt( pageIndex )
				, where
				, "f_fdTask desc,f_time desc");
		
		//根目录不加载up6_folders表
		JSONArray folders = new JSONArray();
		if(!isRoot)
		{
			swm.del("f_fdChild");

            where = swm.to_sql();
            folders = se.page("up6_folders"
                , "f_id"
                , "f_id,f_nameLoc,f_pid,f_sizeLoc,f_time,f_pidRoot,f_pathRel"
                ,Integer.parseInt(pageSize)
                ,Integer.parseInt(pageIndex)
                , where
                , "f_time desc");

            for(int i = 0 , l = folders.size();i<l;++i)
            {
            	JSONObject o = folders.getJSONObject(i);
            	o.put("f_fdTask", true);
            	o.put("f_fdChild", false);
            	o.put("f_pathSvr", "");
            }
		}
		
		//添加文件
		for(int i = 0 , l = files.size();i<l;++i) folders.add(files.getJSONObject(i));
		
		int count = se.count("up6_files", where);
		if(!isRoot)
		{
			count += se.count("up6_folders", where);
		}
		
		JSONObject o = new JSONObject();
		o.put("count", count);
		o.put("code", 0);
		o.put("msg", "");
		o.put("data", folders);
		
		this.m_wb.toContent(o);
	}
	public void search() {
		String pid = this.m_wb.queryString("pid");
		String pageSize = this.m_wb.queryString("limit");
		String pageIndex = this.m_wb.queryString("page");
		if(StringUtils.isBlank(pageSize)) pageSize = "20";
		if(StringUtils.isBlank(pageIndex)) pageIndex = "1";
		String pathRel = this.m_wb.reqStringDecode("pathRel");
		pathRel += '/';
		String key = this.m_wb.reqStringDecode("key");
		
		SqlWhereMerge swm = new SqlWhereMerge();
		DBConfig cfg = new DBConfig();
		if( !StringUtils.isBlank(pid))
		{
			if(StringUtils.equals(cfg.m_db, "sql")) 
	    	{
	    		swm.add("f_pathRel", String.format("f_pathRel='%s'+f_nameLoc",pathRel));
	    	}
	    	else
	    	{
	    		swm.add("f_pathRel", String.format("f_pathRel=CONCAT('%s',f_nameLoc)",pathRel));
	    	}
		}
			
		swm.equal("f_uid", this.m_wb.reqToInt("uid"));
		swm.equal("f_complete", 1);
		swm.equal("f_deleted", 0);
		
		if(!StringUtils.isBlank(key)) swm.add("key", String.format("f_nameLoc like '%%%s%%'",key));
		
		String where = swm.to_sql();
		
		//加载文件列表
		SqlExec se = new SqlExec();
		JSONArray files = se.page("up6_files"
				,"f_id"
				, "f_id,f_pid,f_nameLoc,f_sizeLoc,f_lenLoc,f_time,f_pidRoot,f_fdTask,f_pathSvr,f_pathRel"
				, Integer.parseInt( pageSize )
				, Integer.parseInt( pageIndex )
				, where
				, "f_fdTask desc,f_time desc");
		
		//根目录不加载up6_folders表
		JSONArray folders = new JSONArray();
		//目录表
        folders = se.page("up6_folders"
            , "f_id"
            , "f_id,f_nameLoc,f_pid,f_sizeLoc,f_time,f_pidRoot,f_pathRel"
            ,Integer.parseInt(pageSize)
            ,Integer.parseInt(pageIndex)
            , where
            , "f_time desc");

        for(int i = 0 , l = folders.size();i<l;++i)
        {
        	JSONObject o = folders.getJSONObject(i);
        	o.put("f_fdTask", true);
        	o.put("f_fdChild", false);
        	o.put("f_pathSvr", "");
        }
		
		//添加文件
		for(int i = 0 , l = files.size();i<l;++i) folders.add(files.getJSONObject(i));
		
		JSONObject o = new JSONObject();
		o.put("count", folders.size());
		o.put("code", 0);
		o.put("msg", "");
		o.put("data", folders);
		
		this.m_wb.toContent(o);
	}
	public void rename() {

        String pid     = this.m_wb.reqToString("f_pid");
        String id      = this.m_wb.reqToString("f_id");
        boolean folder = this.m_wb.reqToBool("f_fdTask");
        String nameNew = this.m_wb.reqToString("f_nameLoc");
        nameNew 	   = PathTool.url_decode(nameNew);
        
        if (folder)
            this.rename_folder(pid,id,nameNew);
        else this.rename_file(pid,id, nameNew);   
	}
	
	public void rename_file(String pid,String id,String name)
	{
        DBConfig cfg = new DBConfig();
		boolean ret = cfg.db().existSameFile(name, pid);
        if (ret)
        {
        	JSONObject v = new JSONObject();
        	v.put("state", false);
        	v.put("msg", "存在同名文件");
        	v.put("code", "102");
            //var v = new JObject { { "state",false},{ "msg","存在同名文件"},{ "code","102"} };
            this.m_wb.toContent(v);
        }
        else
        {
            SqlExec se = new SqlExec();
            //更新相对路径：/test/test.jpg
            JSONObject info = se.read("up6_files", "f_pathRel", new SqlParam[] {new SqlParam("f_id",id)});
            String pathRel = info.getString("f_pathRel");
            Integer pos = pathRel.lastIndexOf('/');
            pathRel = pathRel.substring(0, pos+1);
            pathRel = pathRel.concat(name);
            
            //更新名称
            se.update("up6_files",
                new SqlParam[] {
                    new SqlParam("f_nameLoc",name),
                    new SqlParam("f_nameSvr",name),
                    new SqlParam("f_pathRel",pathRel)
                },
                new SqlParam[] {
                    new SqlParam("f_id",id)
                }
                );

        	JSONObject v = new JSONObject();
        	v.put("state", true);
            this.m_wb.toContent(v);
        }		
	}
	
	public void rename_folder(String  pid,String id,String name)
	{
        DBConfig cfg = new DBConfig();
		boolean ext = cfg.folder().existSameFolder(name, pid);
        if(ext)
        {
        	JSONObject v = new JSONObject();
        	v.put("state", false);
        	v.put("msg", "存在同名目录");
        	v.put("code", "102");
            this.m_wb.toContent(v);
        }
        else
        {
            //取pathRoot=/test，并构造新路径
            String pathRelNew = DbFolder.getPathRel(id);
            String pathRelOld = pathRelNew;//旧的路径,pathRoot=/test
            Integer index = pathRelNew.lastIndexOf('/');
            pathRelNew = pathRelNew.substring(0, index + 1);
            pathRelNew = pathRelNew + name;

            //更新文件名称
            SqlExec se = new SqlExec();
            if(StringUtils.isEmpty(pid) )//根目录
            {
                se.update("up6_files",
                    new SqlParam[] {
                    new SqlParam("f_nameLoc",name),
                    new SqlParam("f_nameSvr",name)
                    },
                    new SqlParam[] {
                    new SqlParam("f_id",id)
                    }
                    );

                //更新文件夹路径
                se.update("up6_files", new SqlParam[] {
                    new SqlParam("f_pathRel",pathRelNew)
                },
                new SqlParam[] {
                    new SqlParam("f_id",id)
                }
                );
            }
            else
            {
                se.update("up6_folders",
                    new SqlParam[] {
                    new SqlParam("f_nameLoc",name),
                    },
                    new SqlParam[] {
                    new SqlParam("f_id",id)
                    }
                    );

                //更新文件夹路径
                se.update("up6_folders", new SqlParam[] {
                    new SqlParam("f_pathRel",pathRelNew)
                },
                new SqlParam[] {
                    new SqlParam("f_id",id)
                }
                );
            }
            
            DBConfig dc = new DBConfig();
            dc.db().updatePathRel(pathRelOld, pathRelNew);
            dc.folder().updatePathRel(pathRelOld, pathRelNew);

            JSONObject v = new JSONObject();
            v.put("state", true);
            this.m_wb.toContent(v);
        }		
	}
	
	public void del() {
		String id = this.m_wb.queryString("id");
		String pathRel = this.m_wb.reqStringDecode("pathRel");
		pathRel += '/';
		
		SqlWhereMerge swm = new SqlWhereMerge();
		DBConfig cfg = new DBConfig();
		if(StringUtils.equals(cfg.m_db, "sql")) 
    	{
			swm.charindex(pathRel,"f_pathRel");
    	}
    	else
    	{
    		swm.instr(pathRel,"f_pathRel");
    	}
		String where = swm.to_sql();
		
		SqlExec se = new SqlExec();
		se.update("up6_files", new SqlParam[] {new SqlParam("f_deleted",true)}, where);
		se.update("up6_files"
				, new SqlParam[] {new SqlParam("f_deleted",true)}
				, new SqlParam[] {new SqlParam("f_id",id)}
				);
		se.update("up6_folders", new SqlParam[] {new SqlParam("f_deleted",true)}, where);
		se.update("up6_folders"
				, new SqlParam[] {new SqlParam("f_deleted",true)}
				, new SqlParam[] {new SqlParam("f_id",id)}
				);
		
		JSONObject ret = new JSONObject();
		ret.put("ret", 1);
		this.m_wb.toContent(ret);		
		
	}
	public void del_batch() {
		
		String data = this.m_req.getParameter("data");
		data = PathTool.url_decode(data);
		JSONArray o = JSONArray.fromObject(data);
		
		SqlExec se = new SqlExec();
		se.exec_batch("up6_files", "update up6_files set f_deleted=1 where f_id=?", "", "f_id", o);
		se.exec_batch("up6_folders", "update up6_folders set f_deleted=1 where f_id=?", "", "f_id", o);
		
		JSONObject ret = new JSONObject();
		ret.put("ret", 1);
		this.m_wb.toContent(ret);
		
	}
	public void uncomp() {

        SqlExec se = new SqlExec();
        JSONArray files = se.exec("up6_files"
            , "select f_id,f_nameLoc,f_pathLoc ,f_sizeLoc,f_lenSvr,f_perSvr,f_fdTask,f_md5 from up6_files where f_complete=0 and f_fdChild=0 and f_deleted=0"
            , "f_id,f_nameLoc,f_pathLoc,f_sizeLoc,f_lenSvr,f_perSvr,f_fdTask,f_md5"
            , "id,nameLoc,pathLoc,sizeLoc,lenSvr,perSvr,fdTask,md5");
        this.m_wb.toContent( files );
	}
	public void uncmp_down() {

		String uid = this.m_req.getParameter("uid");
        SqlExec se = new SqlExec();
        JSONArray files = se.select("down_files"
            , "f_id,f_nameLoc,f_pathLoc,f_perLoc,f_sizeSvr,f_fdTask"
            , new SqlParam[] {new SqlParam("f_uid",Integer.parseInt(uid))}
            , ""
            );
        this.m_wb.toContent( files );
	}
}