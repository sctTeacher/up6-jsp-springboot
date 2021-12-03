package com.ncmem.up6.controller;

import com.google.gson.Gson;
import com.ncmem.up6.*;
import com.ncmem.up6.biz.PathBuilder;
import com.ncmem.up6.biz.PathBuilderUuid;
import com.ncmem.up6.biz.fd_scan;
import com.ncmem.up6.biz.up6_biz_event;
import com.ncmem.up6.biz.WebSafe;
import com.ncmem.up6.model.FileInf;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;

/**
 * * Created by jmzy on 2021/1/6.
 * 接口：
 * up6/clear
 * up6/f_create
 * up6/f_post
 * up6/f_process
 * up6/f_complete
 * up6/f_del
 * up6/f_list
 * up6/fd_create
 * up6/fd_complete
 * up6/fd_del
 * 注意：
 * 1.除f_post外其它接口均由前端(up6.js,up6.file.js,up6.folder.js)自动调用
 * 2.f_post由控件自动调用。
 */
@RestController
public class Up6Api {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HttpServletResponse res;

    @GetMapping("up6/test")
    public String test() {
        ClassPathResource res = new ClassPathResource("upload");
        try {
            return res.getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 清空数据库，由前端调用
     * @return
     */
    @RequestMapping(value="up6/clear",method = RequestMethod.GET)
    public String clear()
    {
        DBConfig cfg = new DBConfig();
        cfg.db().Clear();
        return "数据库清理完毕";
    }

    /**
     * 文件初始化,在文件上传前调用，在数据表中记录文件信息。由前諯调用
     * 调用位置：up6.file.js-md5_complete
     * @param id 文件ID，由控件生成，GUID
     * @param md5，文件MD5，由控件计算。
     * @param uid，用户ID，在JS中配置
     * @param lenLoc，数字格式的文件大小，控件提供
     * @param sizeLoc 格式化的文件大小，控件提供
     * @param callback JQ回调方法名称
     * @param pathLoc 文件本地路径，控件提供
     * @return
     */
    @RequestMapping(value="up6/f_create",method = RequestMethod.GET)
    public String f_create(@RequestParam(value="id", required=false,defaultValue="")String id,
                           @RequestParam(value="md5", required=false,defaultValue="")String md5,
                           @RequestParam(value="uid", required=false,defaultValue="")String uid,
                           @RequestParam(value="lenLoc", required=false,defaultValue="")String lenLoc,
                           @RequestParam(value="sizeLoc", required=false,defaultValue="")String sizeLoc,
                           @RequestParam(value="token", required=false,defaultValue="")String token,
                           @RequestParam(value="callback", required=false,defaultValue="")String callback,
                           @RequestParam(value="pathLoc", required=false,defaultValue="")String pathLoc) {

        pathLoc	= PathTool.url_decode(pathLoc);

        //参数为空
        if (	StringUtils.isBlank(md5)
                && StringUtils.isBlank(uid)
                && StringUtils.isBlank(sizeLoc))
        {
            return (callback + "({\"value\":null})");
        }

        FileInf fileSvr= new FileInf();
        fileSvr.id = id;
        fileSvr.fdChild = false;
        fileSvr.uid = Integer.parseInt(uid);
        fileSvr.nameLoc = PathTool.getName(pathLoc);
        fileSvr.pathLoc = pathLoc;
        fileSvr.lenLoc = Long.parseLong(lenLoc);
        fileSvr.sizeLoc = sizeLoc;
        fileSvr.deleted = false;
        fileSvr.md5 = md5;
        fileSvr.nameSvr = fileSvr.nameLoc;

        WebSafe ws = new WebSafe();
        boolean ret = ws.validToken(token, fileSvr);
        if(!ret)
        {
            return (callback + "({\"value\":\"0\",\"ret\":false,\"msg\":\"token error\"})");
        }

        //所有单个文件均以uuid/file方式存储
        PathBuilderUuid pb = new PathBuilderUuid();
        try {
            fileSvr.pathSvr = pb.genFile(fileSvr.uid,fileSvr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSvr.pathSvr = fileSvr.pathSvr.replace("\\","/");

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

        //将路径转换成相对路径
        try {
            fileSvr.pathSvr = pb.absToRel(fileSvr.pathSvr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //加密
        ConfigReader cr = new ConfigReader();
        JSONObject sec = cr.module("path");
        JSONObject security = sec.getJSONObject("security");
        boolean encrypt = security.getBoolean("encrypt");
        if (encrypt)
        {
            CryptoTool ct   = new CryptoTool();
            try {
                fileSvr.pathSvr = ct.encrypt(fileSvr.pathSvr.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(fileSvr);

        try {
            json = URLEncoder.encode(json,"UTF-8");//编码，防止中文乱码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        json = json.replace("+","%20");
        json = callback + "({\"value\":\"" + json + "\",\"ret\":true})";//返回jsonp格式数据。
        return json;
    }

    /**
     * 文件上传完毕，在文件上传完毕后由前端调用。
     * 调用位置：up6.file.js-post_complete
     * @param id 文件ID，由控件生成，GUID 示例：04fdfc13fc2a4b7a9d0682f8c344926f
     * @param uid 用户ID，在JS中困于
     * @param md5 文件MD5
     * @param pid 文件PID
     * @param callback JQ回调
     * @param nameLoc 文件名称，由控件提供
     * @return
     */
    @RequestMapping(value="up6/f_complete",method = RequestMethod.GET)
    public String f_complete(@RequestParam(value="id", required=false,defaultValue="")String id,
                             @RequestParam(value="uid", required=false,defaultValue="0")Integer uid,
                             @RequestParam(value="md5", required=false,defaultValue="")String md5,
                             @RequestParam(value="pid", required=false,defaultValue="")String pid,
                             @RequestParam(value="callback", required=false,defaultValue="")String callback,
                             @RequestParam(value="cover", required=false,defaultValue="0")Integer cover,
                             @RequestParam(value="nameLoc", required=false,defaultValue="")String nameLoc)
    {
        nameLoc	= PathTool.url_decode(nameLoc);
        //返回值。1表示成功
        int ret = 0;
        if ( !StringUtils.isBlank(id))
        {
            DBConfig cfg = new DBConfig();
            cfg.db().complete(id);

            //覆盖同名文件-更新同名文件状态
            if(cover == 1) cfg.db().delete(pid, nameLoc, uid, id);

            up6_biz_event.file_post_complete(id);
            ret = 1;
        }
        return callback + "(" + ret + ")";
    }

    /**
     * 删除文件,在文件列表中调用，由前端调用。
     * 调用位置，up6.js
     * @param id 文件ID
     * @param uid 用户ID
     * @param callback JQ回调方法，用于支持跨域调用
     * @return
     */
    @RequestMapping(value="up6/f_del",method = RequestMethod.GET)
    @ResponseBody
    public String f_del(@RequestParam(value="id", required=false,defaultValue="")String id,
                             @RequestParam(value="uid", required=false,defaultValue="0")String uid,
                             @RequestParam(value="callback", required=false,defaultValue="")String callback
                             )
    {
        //返回值。1表示成功
        int ret = 0;

        if (!StringUtils.isBlank(id) &&
            !StringUtils.isBlank(uid))
        {
            DBConfig cfg = new DBConfig();
            DBFile db = cfg.db();
            db.Delete(Integer.parseInt(uid),id);
            up6_biz_event.file_del(id,Integer.parseInt(uid));
            ret = 1;
        }
        return callback + "(" + ret + ")";
    }

    /**
     * 加载未上传完的文件列表，在文件列表中调用，由前端调用。
     * 加载上传面板时自动调用。
     * 调用位置：up6.js
     * @param uid 用户UI
     * @param cbk JQ回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="up6/f_list",method = RequestMethod.GET)
    public String f_list(@RequestParam(value="uid", required=false,defaultValue="0")String uid,
                        @RequestParam(value="callback", required=false,defaultValue="")String cbk
                        )
    {
        if( uid.length() > 0 )
        {
            DBConfig cfg = new DBConfig();
            String json = cfg.db().GetAllUnComplete( Integer.parseInt( uid ) );
            if( !StringUtils.isBlank(json))
            {
                try {
                    json = URLEncoder.encode(json,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                json = json.replace("+","%20");
                return  cbk + "({\"value\":\""+json + "\"})" ;

            }
        }
        return (cbk + "({\"value\":null})");
    }

    /**
     * 上传文件块，由控件调用
     * @param uid 用户ID
     * @param id 文件ID
     * @param lenSvr 远程文件大小，由控件提供
     * @param lenLoc 本地文件大小，由控件提供
     * @param blockOffset 块偏移位置（相对于整个文件），由控件提供
     * @param blockSize 块大小，由控件提供
     * @param blockIndex 块索引，基于1，由控件提供
     * @param blockMd5 块MD5，由控件提供
     * @param complete 是否是最后一块，由控件提供
     * @param pathSvr 远程文件路径（文件保存位置），由控件提供
     * @param thumb 缩略图，由控件提供
     * @param blockData 块数据，由控件提供
     * @return
     */
    @RequestMapping(value="up6/f_post",method = RequestMethod.POST)
    public String f_post(@RequestHeader(value="uid", required=false,defaultValue="")String uid,
                         @RequestHeader(value="id", required=false,defaultValue="")String id,
                         @RequestHeader(value="lenSvr", required=false,defaultValue="0")String lenSvr,
                         @RequestHeader(value="lenLoc", required=false,defaultValue="0")String lenLoc,
                         @RequestHeader(value="blockOffset", required=false,defaultValue="")String blockOffset,
                         @RequestHeader(value="blockSize", required=false,defaultValue="")String blockSize,
                         @RequestHeader(value="blockIndex", required=false,defaultValue="")String blockIndex,
                         @RequestHeader(value="blockMd5", required=false,defaultValue="")String blockMd5,
                         @RequestHeader(value="complete", required=false,defaultValue="")String complete,
                         @RequestHeader(value="token",required=false,defaultValue="") String token,
                         @RequestParam(value="pathSvr",required=false,defaultValue="") String pathSvr,
                         @RequestParam(value="pathLoc",required=false,defaultValue="") String pathLoc,
                         @RequestParam(value="thumb",required=false) MultipartFile thumb,
                         @RequestParam(value="file",required=true) MultipartFile blockData
                         )
    {
        //参数为空
        if(	 StringUtils.isEmpty( uid )||
                StringUtils.isBlank( id )||
                StringUtils.isEmpty( blockOffset ))
        {
            return "uid,id,blockOffset empty";
        }
        pathSvr = PathTool.url_decode(pathSvr);

        boolean verify = false;
        String msg = "";
        String md5Svr = "";
        ByteArrayOutputStream ostm = new ByteArrayOutputStream();
        long blockSizeSvr = blockData.getSize();
        pathLoc = blockData.getName();

        //加密
        ConfigReader cr = new ConfigReader();
        JSONObject sec = cr.module("path");
        JSONObject security = sec.getJSONObject("security");
        boolean encrypt = security.getBoolean("encrypt");
        if (encrypt)
        {
            CryptoTool ct   = new CryptoTool();
            try {
                pathSvr = ct.decrypt(pathSvr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ostm = ct.decrypt(blockData,Integer.parseInt(blockSize));
            } catch (Exception e) {
                e.printStackTrace();
            }
            blockSizeSvr = ostm.size();
        }

        //token验证
        WebSafe ws = new WebSafe();
        FileInf fileSvr = new FileInf();
        fileSvr.id = id;
        fileSvr.pathSvr = pathSvr;
        fileSvr.nameLoc = PathTool.getName(pathLoc);
        boolean ret = ws.validToken(token,fileSvr,"block");
        //token验证失败
        verify = ret;
        if(!verify)
        {
            msg = String.format("token error loc:%s",token);
        }

        if(!StringUtils.isBlank(blockMd5))
        {
            if(encrypt)
            {
                md5Svr = Md5Tool.fileToMD5(ostm);
            }
            else
            {
                try {
                    md5Svr = Md5Tool.fileToMD5(blockData.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(verify)
        {
            verify = Integer.parseInt(blockSize) == blockSizeSvr;
            if(!verify)
            {
                msg = "block size error sizeSvr:" + blockSizeSvr + "sizeLoc:" + blockSize;
            }
        }

        if(verify && !StringUtils.isBlank(blockMd5))
        {
            verify = md5Svr.equals(blockMd5);
            if(!verify) msg = "block md5 error";
        }

        if(verify)
        {
            PathBuilder pb = new PathBuilder();
            try {
                pathSvr = pb.relToAbs(pathSvr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //保存文件块数据
            FileBlockWriter res = new FileBlockWriter();
            //仅第一块创建
            if( Integer.parseInt(blockIndex)==1) res.CreateFile(pathSvr,Long.parseLong(lenLoc));
            if(encrypt)
            {
                res.write( Long.parseLong(blockOffset),pathSvr,ostm);
            }
            else
            {
                res.write( Long.parseLong(blockOffset),pathSvr,blockData);
            }
            up6_biz_event.file_post_block(id,Integer.parseInt(blockIndex));

            //保存缩略图，最后一块
            if(StringUtils.equals(complete, "true") && thumb != null)
            {
                String thumbPath = pathSvr+".thumb.png";
                res.CreateFile(thumbPath, thumb.getSize());
                res.write(0, thumbPath, thumb);
            }

            JSONObject o = new JSONObject();
            o.put("msg", "ok");
            o.put("md5", md5Svr);
            o.put("offset", blockOffset);//基于文件的块偏移位置
            msg = o.toString();
        }
        return (msg);
    }

    /**
     * 更新文件进度，由前端调用。
     * 调用位置：up6.file.js
     * @param id 文件ID
     * @param uid 用户ID
     * @param offset 偏移位置
     * @param lenSvr 已传大小
     * @param perSvr 已传百分比
     * @param cbk JQ回调方法
     * @return
     */
    @RequestMapping(value="up6/f_process",method = RequestMethod.GET)
    public String f_process(
                        @RequestParam(value="id", required=false,defaultValue="")String id,
                        @RequestParam(value="uid", required=false,defaultValue="")String uid,
                        @RequestParam(value="offset", required=false,defaultValue="")String offset,
                        @RequestParam(value="lenSvr", required=false,defaultValue="")String lenSvr,
                        @RequestParam(value="perSvr", required=false,defaultValue="")String perSvr,
                        @RequestParam(value="callback", required=false,defaultValue="")String cbk
    )
    {
        int ret = 0;
        if (	!StringUtils.isBlank(id)
                &&	!StringUtils.isBlank(lenSvr)
                &&	!StringUtils.isBlank(perSvr))
        {
            DBConfig cfg = new DBConfig();
            DBFile db = cfg.db();
            db.f_process(Integer.parseInt(uid),id,Long.parseLong(offset),Long.parseLong(lenSvr),perSvr);
            up6_biz_event.file_post_process(id);
            ret = 1;
        }
        return cbk + "({\"value\":"+ret+"})";
    }

    /**
     * 文件夹上传完毕，在文件夹上传完毕后自动调用。由前端调用
     * 调用位置：up6.folder.js
     * @param id 文件ID
     * @param uid 用户ID
     * @param cbk JQ回调方法
     * @return
     */
    @RequestMapping(value="up6/fd_complete",method = RequestMethod.GET)
    public String fd_complete(
            @RequestParam(value="id", required=false,defaultValue="")String id,
            @RequestParam(value="uid", required=false,defaultValue="")String uid,
            @RequestParam(value="callback", required=false,defaultValue="")String cbk,
            @RequestParam(value="cover", required=false,defaultValue="0")Integer cover
    )
    {
        int ret = 0;

        //参数为空
        if (	!StringUtils.isBlank(uid)||
                !StringUtils.isBlank(id))
        {
            //取当前节点信息
            DBConfig cfg = new DBConfig();
            DbFolder db = cfg.folder();
            FileInf folder = db.read(id);
            folder.uid = Integer.parseInt(uid);
            FileInf fdExist = db.read(folder.pathRel, folder.pid, id);
            if(1==cover && fdExist !=null)
            {
                folder.id = fdExist.id;
                folder.pid = fdExist.pid;
                folder.pidRoot = fdExist.pidRoot;
            }

            //根节点
            FileInf root = new FileInf();
            root.id = folder.pidRoot;
            root.uid = folder.uid;
            //当前节点是根节点
            if( StringUtils.isBlank(root.id)) root.id = folder.id;


            //上传完毕
            DBFile db2 = cfg.db();
            db2.fd_complete(id,uid);

            //扫描文件夹结构
            fd_scan sa = new fd_scan();
            sa.root = root;

            //清理同名子文件
            if(1==cover && fdExist !=null)
            {
                //覆盖同名子文件
                try {
                    sa.cover(folder,folder.pathSvr);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            //添加文件记录
            try {
                sa.scan(folder,folder.pathSvr);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            cfg.db().fd_scan(id,uid);

            up6_biz_event.folder_post_complete(id);

            //删除当前目录
            if(1 == cover && fdExist != null)
            {
                DbFolder.del(id, Integer.parseInt(uid));
            }

            ret = 1;
        }
        return cbk + "(" + ret + ")";
    }

    /**
     * 文件夹初始化，在上传文件夹时自动调用。
     * 调用位置：up6.folder.js
     * @param id 文件夹ID，GUID格式
     * @param uid 用户ID
     * @param lenLoc 数字化的文件夹大小：1024
     * @param sizeLoc 格式化的文件夹大小：10MB
     * @param pathLoc 文件夹本地路径。
     * @param cbk JQ回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="up6/fd_create",method = RequestMethod.GET)
    public String fd_create(
            @RequestParam(value="id", required=false,defaultValue="")String id,
            @RequestParam(value="uid", required=false,defaultValue="")String uid,
            @RequestParam(value="lenLoc", required=false,defaultValue="")String lenLoc,
            @RequestParam(value="sizeLoc", required=false,defaultValue="")String sizeLoc,
            @RequestParam(value="pathLoc", required=false,defaultValue="")String pathLoc,
            @RequestParam(value="callback", required=false,defaultValue="")String cbk
    )
    {
        //参数为空
        if (
                StringUtils.isBlank(id)||
                StringUtils.isBlank(uid)||
                StringUtils.isBlank(pathLoc))
        {
            return (cbk + "({\"value\":null})");
        }
        pathLoc = PathTool.url_decode(pathLoc);


        FileInf fileSvr = new FileInf();
        fileSvr.id      = id;
        fileSvr.fdChild = false;
        fileSvr.fdTask  = true;
        fileSvr.uid     = Integer.parseInt(uid);
        fileSvr.nameLoc = PathTool.getName(pathLoc);
        fileSvr.pathLoc = pathLoc;
        fileSvr.lenLoc  = Long.parseLong(lenLoc);
        fileSvr.sizeLoc = sizeLoc;
        fileSvr.deleted = false;
        fileSvr.nameSvr = fileSvr.nameLoc;

        //生成存储路径
        PathBuilderUuid pb = new PathBuilderUuid();
        try {
            fileSvr.pathSvr = pb.genFolder(fileSvr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSvr.pathSvr = fileSvr.pathSvr.replace("\\","/");
        PathTool.createDirectory(fileSvr.pathSvr);
        //添加到数据表
        DBConfig cfg = new DBConfig();
        DBFile db = cfg.db();
        db.Add(fileSvr);

        //加密
        ConfigReader cr = new ConfigReader();
        JSONObject sec = cr.module("path");
        JSONObject security = sec.getJSONObject("security");
        boolean encrypt = security.getBoolean("encrypt");
        if (encrypt)
        {
            CryptoTool ct   = new CryptoTool();
            try {
                fileSvr.pathSvr = ct.encrypt(fileSvr.pathSvr.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        up6_biz_event.folder_create(fileSvr);

        Gson g = new Gson();
        String json = g.toJson(fileSvr);
        try {
            json = URLEncoder.encode(json,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        json = json.replace("+","%20");

        JSONObject ret = new JSONObject();
        ret.put("value",json);
        json = cbk + String.format("(%s)",ret.toString());//返回jsonp格式数据。
        return json;
    }

    /**
     * 删除文件夹，在文件列表中点击删除按钮时调用，由前端调用
     * 调用位置：up6.js
     * @param id 文件夹ID
     * @param uid 用户ID
     * @param cbk JQ回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="up6/fd_del",method = RequestMethod.GET)
    public String fd_del(
            @RequestParam(value="id", required=false,defaultValue="")String id,
            @RequestParam(value="uid", required=false,defaultValue="")String uid,
            @RequestParam(value="callback", required=false,defaultValue="")String cbk
    )
    {
        int ret = 0;
        //参数为空
        if (	!StringUtils.isBlank(id)||
                uid.length()>0 )
        {
            DBConfig cfg = new DBConfig();
            cfg.db().delFolder(id,Integer.parseInt(uid));
            ret = 1;
        }
        return cbk + "({\"value\":"+ret+"})";
    }
}
