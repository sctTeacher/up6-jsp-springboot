package com.ncmem.filemgr.controller;

import com.google.gson.Gson;
import com.ncmem.up6.*;
import com.ncmem.up6.biz.FolderBuilder;
import com.ncmem.up6.biz.PathBuilderUuid;
import com.ncmem.up6.biz.up6_biz_event;
import com.ncmem.up6.model.FileInf;
import com.ncmem.up6.sql.SqlExec;
import com.ncmem.up6.sql.SqlParam;
import com.ncmem.up6.sql.SqlWhereMerge;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by jmzy on 2021/1/7.
 */
@Controller
public class MgrApi {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HttpServletResponse res;

    /**
     * 加载文件列表数据，
     * @param pid 父级ID
     * @param uid 用户ID
     * @param pageSize     页大小
     * @param pageIndex    页索引
     * @return
     */
    @RequestMapping(value="filemgr/data",method = RequestMethod.GET)
    @ResponseBody
    public String data(@RequestParam(value="pid", required=false,defaultValue="")String pid,
                       @RequestParam(value="uid", required=false,defaultValue="")Integer uid,
                       @RequestParam(value="limit", required=false,defaultValue="")String pageSize,
                       @RequestParam(value="page", required=false,defaultValue="")String pageIndex,
                       @RequestParam(value="pathRel", required=false,defaultValue="")String pathRel
    )
    {

        if(StringUtils.isBlank(pageSize)) pageSize = "20";
        if(StringUtils.isBlank(pageIndex)) pageIndex = "1";
        pathRel = PathTool.url_decode(pathRel);
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

        swm.equal("f_uid", uid);
        swm.equal("f_complete", true);
        swm.equal("f_deleted", false);
        swm.equal("f_fdChild", true);

        Boolean isRoot = StringUtils.isBlank(pid);
        if(isRoot) swm.equal("f_fdChild", false);

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

        System.out.println(o.toString());
        return o.toString();
    }

    /**
     * 搜索文件列表数据，
     * @param pid 父级ID
     * @param uid 用户ID
     * @param pageSize     页大小
     * @param pageIndex    页索引
     * @return
     */
    @RequestMapping(value="filemgr/search",method = RequestMethod.GET)
    @ResponseBody
    public String search(@RequestParam(value="pid", required=false,defaultValue="")String pid,
                       @RequestParam(value="uid", required=false,defaultValue="")Integer uid,
                       @RequestParam(value="limit", required=false,defaultValue="")String pageSize,
                       @RequestParam(value="page", required=false,defaultValue="")String pageIndex,
                       @RequestParam(value="pathRel", required=false,defaultValue="")String pathRel,
                         @RequestParam(value="key", required=false,defaultValue="")String key
    )
    {

        if(StringUtils.isBlank(pageSize)) pageSize = "20";
        if(StringUtils.isBlank(pageIndex)) pageIndex = "1";
        pathRel = PathTool.url_decode(pathRel);
        pathRel += '/';
        key = PathTool.url_decode(key);

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

        swm.equal("f_uid", uid);
        swm.equal("f_complete", true);
        swm.equal("f_deleted", false);

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

        System.out.println(o.toString());
        return o.toString();
    }

    /**
     * 重命名文件或目录
     * @param pid 父ID
     * @param id 文件或目录ID
     * @param folder 是否是文件夹
     * @param nameNew 新名称
     * @return
     */
    @RequestMapping(value="filemgr/rename",method = RequestMethod.GET)
    @ResponseBody
    public String rename(@RequestParam(value="f_pid", required=false,defaultValue="")String pid,
                       @RequestParam(value="f_id", required=false,defaultValue="")String id,
                       @RequestParam(value="f_fdTask", required=false,defaultValue="")Boolean folder,
                       @RequestParam(value="f_nameLoc", required=false,defaultValue="")String nameNew
    )
    {
        if (folder) return this.rename_folder(pid,id,nameNew);
        else return this.rename_file(pid,id, nameNew);
    }


    /**
     * 重命名文件
     * @param pid     父ID
     * @param id      文件ID
     * @param name    文件名称
     * @return
     */
    public String rename_file(String pid,String id,String name)
    {
        DBConfig cfg = new DBConfig();
        boolean ret = cfg.db().existSameFile(name, pid);
        if (ret)
        {
            JSONObject v = new JSONObject();
            v.put("state", false);
            v.put("msg", "存在同名文件");
            v.put("code", "102");
            return v.toString();
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
            return v.toString();
        }
    }

    /**
     * 重命名目录
     * @param pid     父级ID
     * @param id      目录ID
     * @param name    目录名称
     * @return
     */
    public String rename_folder(String  pid,String id,String name)
    {
        DBConfig cfg = new DBConfig();
        boolean ext = cfg.folder().existSameFolder(name, pid);
        if(ext)
        {
            JSONObject v = new JSONObject();
            v.put("state", false);
            v.put("msg", "存在同名目录");
            v.put("code", "102");
            return v.toString();
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
            return v.toString();
        }
    }

    /**
     * 删除文件或目录
     * @param id 文件ID
     * @return
     */
    @RequestMapping(value="filemgr/del",method = RequestMethod.GET)
    @ResponseBody
    public String del(@RequestParam(value="id", required=false,defaultValue="")String id,
                      @RequestParam(value="pathRel", required=false,defaultValue="")String pathRel
    )
    {
        pathRel = PathTool.url_decode(pathRel);
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
        return ret.toString();
    }

    /**
     * 批量删除
     * @param data
     * @return
     */
    @RequestMapping(value="filemgr/del-batch",method = RequestMethod.GET)
    @ResponseBody
    public String del_batch(@RequestParam(value="data", required=false,defaultValue="")String data
    )
    {
        data = PathTool.url_decode(data);
        JSONArray o = JSONArray.fromObject(data);

        SqlExec se = new SqlExec();
        se.exec_batch("up6_files", "update up6_files set f_deleted=1 where f_id=?", "", "f_id", o);
        se.exec_batch("up6_folders", "update up6_folders set f_deleted=1 where f_id=?", "", "f_id", o);

        JSONObject ret = new JSONObject();
        ret.put("ret", 1);
        return ret.toString();
    }

    /**
     * 获取当前目录路径
     * @param data
     * @return
     */
    @RequestMapping(value="filemgr/path",method = RequestMethod.GET)
    @ResponseBody
    public String path(@RequestParam(value="data", required=false,defaultValue="")String data
    )
    {
        data = PathTool.url_decode(data);
        JSONObject fd = JSONObject.fromObject(data);

        DbFolder df = new DbFolder();
        return df.build_path(fd).toString();
    }

    /**
     * 加载树
     * @param pid
     * @return
     */
    @RequestMapping(value="filemgr/tree",method = RequestMethod.GET)
    @ResponseBody
    public String tree(@RequestParam(value="pid", required=false,defaultValue="")String pid
    )
    {
        SqlWhereMerge swm = new SqlWhereMerge();
        swm.equal("f_fdChild", false);
        swm.equal("f_fdTask", true);
        swm.equal("f_deleted", false);
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
        return arr.toString();
    }

    /**
     * 文件初始化，在上传文件前调用，前端调用
     * @param pid 父ID
     * @param pidRoot 根ID
     * @param id 文件ID
     * @param md5 文件MD5
     * @param uid 用户ID
     * @param lenLoc 数字化的本地文件大小 1024
     * @param sizeLoc 格式化的本地文件大小 10MB
     * @param callback jq回调方法，提供跨域调用
     * @param pathLoc 本地文件路径 c:\\file.txt
     * @param pathRel 文件相对路径。/root/file.txt
     * @return
     */
    @RequestMapping(value="filemgr/f_create",method = RequestMethod.GET)
    @ResponseBody
    public String f_create(@RequestParam(value="pid", required=false,defaultValue="")String pid,
                           @RequestParam(value="pidRoot", required=false,defaultValue="")String pidRoot,
                           @RequestParam(value="id", required=false,defaultValue="")String id,
                           @RequestParam(value="md5", required=false,defaultValue="")String md5,
                           @RequestParam(value="uid", required=false,defaultValue="")String uid,
                           @RequestParam(value="lenLoc", required=false,defaultValue="")String lenLoc,
                           @RequestParam(value="sizeLoc", required=false,defaultValue="")String sizeLoc,
                           @RequestParam(value="callback", required=false,defaultValue="")String callback,
                           @RequestParam(value="pathLoc", required=false,defaultValue="")String pathLoc,
                           @RequestParam(value="pathRel", required=false,defaultValue="")String pathRel
    )
    {
        if(StringUtils.isBlank(pidRoot)) pidRoot = pid;//当前文件夹是根目录
        pathLoc	= PathTool.url_decode(pathLoc);
        pathRel = PathTool.url_decode(pathRel);

        //参数为空
        if (	StringUtils.isBlank(md5)&&
                StringUtils.isBlank(uid)&&
                StringUtils.isBlank(sizeLoc))
        {
            return callback + "({\"value\":null})";
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
        return json;
    }

    /**
     * 文件夹初始化，上传文件夹前调用。
     * @param pid
     * @param pidRoot
     * @param id
     * @param md5
     * @param uid
     * @param lenLoc
     * @param sizeLoc
     * @param callback
     * @param pathLoc
     * @param pathRel
     * @return
     */
    @RequestMapping(value="filemgr/fd_create",method = RequestMethod.GET)
    @ResponseBody
    public String fd_create(@RequestParam(value="pid", required=false,defaultValue="")String pid,
                           @RequestParam(value="pidRoot", required=false,defaultValue="")String pidRoot,
                           @RequestParam(value="id", required=false,defaultValue="")String id,
                           @RequestParam(value="md5", required=false,defaultValue="")String md5,
                           @RequestParam(value="uid", required=false,defaultValue="")String uid,
                           @RequestParam(value="lenLoc", required=false,defaultValue="")String lenLoc,
                           @RequestParam(value="sizeLoc", required=false,defaultValue="")String sizeLoc,
                           @RequestParam(value="callback", required=false,defaultValue="")String callback,
                           @RequestParam(value="pathLoc", required=false,defaultValue="")String pathLoc,
                           @RequestParam(value="pathRel", required=false,defaultValue="")String pathRel
    )
    {

        if( StringUtils.isBlank(pidRoot)) pidRoot = pid;//父目录是根目录
        pathRel = PathTool.url_decode(pathRel);
        pathLoc	= PathTool.url_decode(pathLoc);

        //参数为空
        if (    StringUtils.isBlank(id)||
                StringUtils.isBlank(uid)||
                StringUtils.isBlank(pathLoc))
        {
            return callback + "({\"value\":null})";
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
        return json;
    }

    /**
     * 获取文件夹数据，
     * @param id
     * @param callback
     * @return
     */
    @RequestMapping(value="filemgr/fd_data",method = RequestMethod.GET)
    @ResponseBody
    public String fd_data(@RequestParam(value="id", required=false,defaultValue="")String id,
                            @RequestParam(value="callback", required=false,defaultValue="")String callback
    )
    {

        if (StringUtils.isBlank(id))
        {
            return callback + "({\"value\":null})";
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
        return json;
    }

    /**
     * 创建目录，
     * @param name 目录名称
     * @param pid 父级ID
     * @param uid 用户ID
     * @param pidRoot 根ID
     * @param pathRel 相对路径,/test/test1
     * @return
     */
    @RequestMapping(value="filemgr/mk_folder",method = RequestMethod.GET)
    @ResponseBody
    public String mk_folder(@RequestParam(value="f_nameLoc", required=false,defaultValue="")String name,
                            @RequestParam(value="f_pid", required=false,defaultValue="")String pid,
                            @RequestParam(value="uid", required=false,defaultValue="")Integer uid,
                            @RequestParam(value="f_pidRoot", required=false,defaultValue="")String pidRoot,
                            @RequestParam(value="f_pathRel", required=false,defaultValue="")String pathRel
    )
    {
        name = PathTool.url_decode(name);
        pathRel = PathTool.url_decode(pathRel);
        pathRel = PathTool.combine(pathRel, name);

        DBConfig cfg = new DBConfig();
        DbFolder df = cfg.folder();
        if (df.exist_same_folder(name, pid))
        {
            JSONObject ret = new JSONObject();
            ret.put("ret", false);
            ret.put("msg", "已存在同名目录");
            return ret.toString();
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
        return ret.toString();
    }

    /**
     * 加载未上传完的数据
     * @param uid
     * @return
     */
    @RequestMapping(value="filemgr/uncomp",method = RequestMethod.GET)
    @ResponseBody
    public String uncomp(@RequestParam(value="uid", required=false,defaultValue="")String uid)
    {
        SqlExec se = new SqlExec();
        JSONArray files = se.exec("up6_files"
                , "select f_id,f_nameLoc,f_pathLoc ,f_sizeLoc,f_lenSvr,f_perSvr,f_fdTask,f_md5 from up6_files where f_complete=0 and f_fdChild=0 and f_deleted=0"
                , "f_id,f_nameLoc,f_pathLoc,f_sizeLoc,f_lenSvr,f_perSvr,f_fdTask,f_md5"
                , "id,nameLoc,pathLoc,sizeLoc,lenSvr,perSvr,fdTask,md5");
        return files.toString();
    }

    /**
     * 加载未下载完的数据
     * @param uid
     * @return
     */
    @RequestMapping(value="filemgr/uncmp_down",method = RequestMethod.GET)
    @ResponseBody
    public String uncmp_down(@RequestParam(value="uid", required=false,defaultValue="")String uid)
    {
        SqlExec se = new SqlExec();
        JSONArray files = se.select("down_files"
                , "f_id,f_nameLoc,f_pathLoc,f_perLoc,f_sizeSvr,f_fdTask"
                , new SqlParam[] {new SqlParam("f_uid",Integer.parseInt(uid))}
                , ""
        );
        return files.toString();
    }
}
