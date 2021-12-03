package com.ncmem.down2.controller;

import com.google.gson.Gson;
import com.ncmem.down2.biz.DnFile;
import com.ncmem.down2.biz.DnFolder;
import com.ncmem.down2.model.DnFileInf;
import com.ncmem.up6.DBConfig;
import com.ncmem.up6.PathTool;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * Created by jmzy on 2021/1/6.
 * 路径：
 * down2/clear
 * down2/f_create
 * down2/f_del
 * down2/f_down
 * down2/f_list_cmp
 * down2/f_list
 * down2/f_update
 * down2/fd_data
 */
@RestController
public class DownApi {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HttpServletResponse res;

    /**
     * 清空下载数据表，由前端调用
     */
    @RequestMapping(value="down2/clear",method = RequestMethod.GET)
    public void clear()
    {
        DBConfig db = new DBConfig();
        db.down().Clear();
    }

    /**
     * 文件初始化，向数据表添加一条下载记录，由前端调用
     * 调用位置：down.file.js
     * @param id 文件ID
     * @param uid 用户ID
     * @param fdTask 是否是文件夹任务
     * @param nameLoc 文件名称
     * @param pathLoc 文件本地保存路径
     * @param lenSvr 数字化的远程文件大小。1024
     * @param sizeSvr 格式化的远程文件大小。10MB
     * @param cbk JQ回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="down2/f_create",method = RequestMethod.GET)
    public String f_create(@RequestParam(value="id", required=false,defaultValue="")String id,
                           @RequestParam(value="uid", required=false,defaultValue="")String uid,
                           @RequestParam(value="fdTask", required=false,defaultValue="")String fdTask,
                           @RequestParam(value="nameLoc", required=false,defaultValue="")String nameLoc,
                           @RequestParam(value="pathLoc", required=false,defaultValue="")String pathLoc,
                           @RequestParam(value="lenSvr", required=false,defaultValue="")String lenSvr,
                           @RequestParam(value="sizeSvr", required=false,defaultValue="")String sizeSvr,
                           @RequestParam(value="callback", required=false,defaultValue="")String cbk)
        {

            pathLoc = PathTool.url_decode(pathLoc);
            nameLoc	= PathTool.url_decode(nameLoc);//utf-8解码
            sizeSvr = PathTool.url_decode(sizeSvr);

            if (  StringUtils.isEmpty(uid)
                    ||StringUtils.isBlank(pathLoc)
                    ||StringUtils.isBlank(lenSvr))
            {
                return cbk + "({\"value\":null}) ";
            }

            DnFileInf inf = new DnFileInf();
            inf.id	= id;
            inf.uid = Integer.parseInt(uid);
            inf.nameLoc = nameLoc;
            inf.pathLoc = pathLoc;
            inf.lenSvr = Long.parseLong(lenSvr);
            inf.sizeSvr = sizeSvr;
            inf.fdTask = fdTask == "1";

            DBConfig db = new DBConfig();
            db.down().Add(inf);

            Gson gson = new Gson();
            String json = gson.toJson(inf);
            try {
                json = URLEncoder.encode(json,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            json = json.replaceAll("\\+","%20");
            json = cbk + "({\"value\":\"" + json + "\"})";//返回jsonp格式数据。
            return (json);
    }

    /**
     * 从down_files中删除下载任务，在文件下载完毕后自动调用，前端调用
     * 调用位置：down.file.js
     * @param fid 文件ID
     * @param uid 用户ID
     * @param cbk JQ回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="down2/f_del",method = RequestMethod.GET)
    @ResponseBody
    public String f_del(@RequestParam(value="id", required=false,defaultValue="")String fid,
                           @RequestParam(value="uid", required=false,defaultValue="")String uid,
                           @RequestParam(value="callback", required=false,defaultValue="")String cbk)
    {

        if (	StringUtils.isEmpty(uid)||
                StringUtils.isBlank(fid)
                )
        {
            return (cbk + "({\"value\":null})");
        }
        DBConfig db = new DBConfig();
        db.down().Delete(fid,Integer.parseInt(uid) );
        return (cbk+"({\"value\":1})");
    }

    /**
     * 下载文件块数据，由控件调用。
     * @param fid 文件ID
     * @param blockIndex    块索引，基于1
     * @param blockOffset   块偏移，相对于整个文件
     * @param blockSize     块大小
     * @param pathSvr       文件地址
     */
    @RequestMapping(value="down2/f_down",method = RequestMethod.GET)
    public void f_down(@RequestHeader(value="id", required=false,defaultValue="")String fid,
                       @RequestHeader(value="blockIndex", required=false,defaultValue="")String blockIndex,
                       @RequestHeader(value="blockOffset", required=false,defaultValue="")String blockOffset,
                       @RequestHeader(value="blockSize", required=false,defaultValue="")String blockSize,
                       @RequestHeader(value="pathSvr", required=false,defaultValue="")String pathSvr)
    {
        pathSvr = PathTool.url_decode(pathSvr);

        if (
                StringUtils.isBlank(fid)||
                StringUtils.isBlank(blockIndex)||
                StringUtils.isEmpty(blockOffset)||
                StringUtils.isBlank(blockSize)||
                StringUtils.isBlank(pathSvr) )
        {
            res.setStatus(500);
            res.setHeader("err","参数为空");
            String msg = String.format("fid:%s,blockIndex:%s,blockOffset:%s,blockSize:%s,pathSvr:%s",
                    fid,
                    blockIndex,
                    blockOffset,
                    blockSize,
                    pathSvr);
            try {
                res.getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        File f = new File(pathSvr);
        //文件不存在
        if(!f.exists())
        {
            res.setStatus(500);
            OutputStream os = null;
            try {
                os = res.getOutputStream();
                System.out.println(String.format("%s 文件不存在",pathSvr));
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        //文件存在
        long fileLen = f.length();
        res.setContentType("application/x-download");
        res.setHeader("Pragma","No-cache");
        res.setHeader("Cache-Control","no-cache");
        res.addHeader("Content-Length",blockSize);
        res.setDateHeader("Expires", 0);

        OutputStream os = null;
        try {
            os = res.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try
        {
            RandomAccessFile raf = new RandomAccessFile(pathSvr,"r");

            int readToLen = Integer.parseInt(blockSize);
            int readLen = 0;
            raf.seek( Long.parseLong(blockOffset) );//定位索引
            byte[] data = new byte[1048576];//1MB

            while( readToLen > 0 )
            {
                readLen = raf.read(data,0,Math.min(1048576,readToLen) );
                readToLen -= readLen;
                os.write(data, 0, readLen);

            }
            os.flush();
            os.close();
            raf.close();
            os = null;
            res.flushBuffer();
        }
        catch(Exception e)
        {
            res.setStatus(500);
            try {
                os.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        finally
        {
            if(os != null)
            {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                os = null;
            }
        }
    }

    /**
     * 列出已经上传完的文件列表，在打开页面时调用，由前端调用
     * @param uid 用户ID
     * @param cbk jq回调方法，提供跨域调用
     */
    @RequestMapping(value="down2/f_list_cmp",method = RequestMethod.GET)
    public String f_list_cmp(@RequestParam(value="uid", required=false,defaultValue="")String uid,
                       @RequestParam(value="callback", required=false,defaultValue="")String cbk)
    {
        if (!StringUtils.isEmpty(uid))
        {
            DBConfig db = new DBConfig();
            String json = db.down().all_complete(Integer.parseInt(uid));
            if(!StringUtils.isBlank(json))
            {
                try {
                    json = URLEncoder.encode(json,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                json = json.replace("+","%20");
                return cbk + "({\"value\":\""+json+"\"})";
            }
        }
        return (cbk+"({\"value\":null})");
    }

    /**
     * 列出未下载完的文件列表，在打开下载页面时调用，由前端调用
     * @param uid 用户ID
     * @param cbk jq回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="down2/f_list",method = RequestMethod.GET)
    public String f_list(@RequestParam(value="uid", required=false,defaultValue="")String uid,
                             @RequestParam(value="callback", required=false,defaultValue="")String cbk)
    {

        if (!StringUtils.isEmpty(uid))
        {
            String json = DnFile.all_uncmp( Integer.parseInt(uid));

            if(!StringUtils.isBlank(json))
            {
                try {
                    json = URLEncoder.encode(json,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                json = json.replaceAll("\\+","%20");//
                json = cbk + "({\"value\":\""+json+"\"})";
                return json;
            }
        }

        return cbk + "({\"value\":null})";
    }

    /**
     * 更新文件下载进度，由前端调用。
     * @param id 文件ID
     * @param uid 用户ID
     * @param lenLoc 已下载大小
     * @param per 已下载百分比
     * @param cbk jq回调方法，提供跨域调用。
     * @return
     */
    @RequestMapping(value="down2/f_update",method = RequestMethod.GET)
    public String f_update(@RequestParam(value="id", required=false,defaultValue="")String id,
                           @RequestParam(value="uid", required=false,defaultValue="")String uid,
                           @RequestParam(value="lenLoc", required=false,defaultValue="")String lenLoc,
                           @RequestParam(value="perLoc", required=false,defaultValue="")String per,
                         @RequestParam(value="callback", required=false,defaultValue="")String cbk)
    {
        per = per.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        per = PathTool.url_decode(per);

        if (
                StringUtils.isEmpty(uid)||
                StringUtils.isEmpty(id)||
                StringUtils.isEmpty(cbk))
        {
            return cbk + "({\"value\":0})";
        }

        DBConfig db = new DBConfig();
        db.down().process(id,Integer.parseInt(uid),lenLoc,per);
        return cbk + "({\"value\":1})";
    }

    /**
     * 列出文件夹数据，由前端调用
     * 调用位置：down.folder.js
     * @param id 文件夹ID
     * @param cbk JQ回调方法，提供跨域调用
     * @return
     */
    @RequestMapping(value="down2/fd_data",method = RequestMethod.GET)
    @ResponseBody
    public String fd_data(@RequestParam(value="id", required=false,defaultValue="")String id,
                           @RequestParam(value="callback", required=false,defaultValue="")String cbk)
    {
        String json = cbk + "({\"value\":null})";

        if (  !StringUtils.isEmpty(id)	)
        {
            DnFolder df = new DnFolder();
            String data = df.childs(id);
            //XDebug.Output("文件列表",data);
            try {
                data = URLEncoder.encode(data,"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            data = data.replace("+","%20");
            json = cbk + "({\"value\":\""+data+"\"})";
        }
        return json;
    }
}
