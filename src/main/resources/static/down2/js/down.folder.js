function FdDownloader(fileLoc, mgr)
{
    var _this = this;
    this.ui = { msg: null, process: null, percent: null, btn: {del:null,cancel:null,down:null,stop:null},div:null};
    this.app = mgr.app;
    this.Manager = mgr;
    this.Config = mgr.Config;
    this.fields = $.extend({},mgr.Config.Fields);//每一个对象自带一个fields幅本
    this.State = this.Config.state.None;
    this.event = mgr.event;
    this.fileSvr = {
          id:""//
        , f_id:""
        , uid: this.fields["uid"]
        , nameLoc: ""//自定义文件名称
        , folderLoc: this.Config["Folder"]
        , pathLoc: ""
        , fileUrl: ""
        , cmpCount: 0
        , fileCount: 0
        , lenLoc: 0
        , perLoc: "0%"
        , lenSvr: 0
        , sizeSvr:"0byte"
        , complete: false
        , fdTask: true
        , files: null
        , svrInit: false//服务器已经记录信息
    };
    var url = this.Config["UrlDown"] + "?" + this.Manager.to_params(this.fields);
    $.extend(this.fileSvr, fileLoc, {fileUrl:url});//覆盖配置

    this.hideBtns = function ()
    {
        $.each(this.ui.btn, function (i, n)
        {
            $(n).hide();
        });
    };

    //方法-准备
    this.ready = function ()
    {
        this.hideBtns();
        this.ui.btn.del.click(function () { _this.remove(); });
        this.ui.btn.stop.click(function () { _this.stop(); });
        this.ui.btn.down.click(function () { _this.Manager.allStoped = false;_this.down(); });
        this.ui.btn.cancel.click(function () { _this.remove(); });
        this.ui.btn.open.click(function () { _this.open(); });
        this.ui.btn.errInf.click(function () { _this.ui.errFiles.toggleClass("d-hide"); });

        this.ui.btn.down.show();
        this.ui.btn.cancel.show();
        this.ui.ico.file.hide();
        this.ui.ico.fd.show();
        this.ui.msg.text("正在下载队列中等待...");
        this.State = this.Config.state.Ready;
        this.Manager.add_wait(this.fileSvr.id);//添加到等待队列
    };
    //自定义配置,
    this.reset_fields = function (v) {
        if (v == null) return;
        $.extend(this.fields, v);
        //单独拼接url
        var url = this.Config["UrlDown"] + "?" + this.Manager.to_params(this.fields);
        $.extend(this.fileSvr, { fileUrl: url });//覆盖配置
    };

    //加载文件列表
    this.load_files = function ()
    {
        //已记录将不再记录
        if (this.fileSvr.svrInit) return;
        this.ui.btn.down.hide();
        this.ui.msg.text("开始加载文件列表...");
        var param = $.extend({}, this.fields, { time: new Date().getTime() });
        $.extend(param, { id: this.fileSvr.f_id});
        $.ajax({
            type: "GET"
            , dataType: 'jsonp'
            , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
            , url: _this.Config["UrlFdData"]
            , data: param
            , success: function (msg) {
                var json = JSON.parse(decodeURIComponent(msg.value));
                _this.ui.msg.text("正在初始文件夹...");
                setTimeout(function () {
                    _this.app.initFolder($.extend({}, _this.Config, _this.fileSvr, { files: json }));
                }, 300);
                
            }
            , error: function (req, txt, err) { alert("创建信息失败！" + req.responseText); }
            , complete: function (req, sta) { req = null; }
        });
    };

    //方法-开始下载
    this.down = function ()
    {
        this.hideBtns();
        this.ui.btn.stop.show();
        this.ui.btn.errInf.hide();
        this.ui.errFiles.html("");
        this.ui.msg.text("开始连接服务器...");
        this.State = this.Config.state.Posting;
        this.Manager.add_work(this.fileSvr.id);
        this.Manager.remove_wait(this.fileSvr.id);
        if (!this.fileSvr.svrInit) {
            this.load_files();
        }
        else {
            this.app.downFolder(this.fileSvr);//下载队列
        }
    };

    //方法-停止传输
    this.stop = function ()
    {
        this.svr_update();
        this.hideBtns();
        this.State = this.Config.state.Stop;
        this.ui.msg.text("下载已停止");
        this.app.stopFile(this.fileSvr);
        this.Manager.del_work(this.fileSvr.id);//从工作队列中删除
    };

    this.remove = function ()
    {
        this.app.stopFile({id:this.fileSvr.id});
        //从上传列表中删除
        this.ui.div.remove();
        this.Manager.remove_url(this.fileSvr.nameLoc);
        this.svr_delete();
    };

    this.open = function ()
    {
        this.app.openPath(this.fileSvr);
    };

    this.openChild = function (file)
    {
        this.app.openChild({ folder: this.fileSvr, "file": file });
    };

    this.init_complete = function (json)
    {
        $.extend(this.fileSvr, json, { files: null });
        setTimeout(function () { _this.svr_create(); }, 200);
    };

    //在出错，停止中调用
    this.svr_update = function (json)
    {
        var param = $.extend({}, this.fields, { time: new Date().getTime() });
        $.extend(param, { id: this.fileSvr.id, lenLoc: this.fileSvr.lenLoc, perLoc: this.fileSvr.perLoc });

        $.ajax({
            type: "GET"
            , dataType: 'jsonp'
            , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
            , url: _this.Config["UrlUpdate"]
            , data: param
            , success: function (msg) { }
            , error: function (req, txt, err) { alert("更新下载信息失败！" + req.responseText); }
            , complete: function (req, sta) { req = null; }
        });
    };

    //添加记录
    this.svr_create = function ()
    {
        //已记录将不再记录
        if (this.fileSvr.svrInit) return;
        this.ui.btn.down.hide();
        this.ui.msg.text("正在创建任务...");
        var param = $.extend({}, this.fields, {time: new Date().getTime() });
        $.extend(param, {
              id: this.fileSvr.id
            , uid: this.fileSvr.uid
            , nameLoc: encodeURIComponent(this.fileSvr.nameLoc)
            , pathLoc: encodeURIComponent(this.fileSvr.pathLoc)
            , lenSvr: this.fileSvr.lenSvr
            , sizeSvr: this.fileSvr.sizeSvr
            , fdTask: 1
        });
        $.ajax({
            type: "GET"
            , dataType: 'jsonp'
            , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
            , url: _this.Config["UrlCreate"]
            , data: param
            , success: function (msg)
            {
                _this.ui.btn.down.show();
                _this.ui.msg.text("初始化完毕...");
                _this.fileSvr.svrInit = true;
                _this.svr_create_cmp();
            }
            , error: function (req, txt, err) { alert("创建信息失败！" + req.responseText); }
            , complete: function (req, sta) { req = null; }
        });
    };
    this.svr_create_cmp = function () {
        setTimeout(function () {
            _this.down();
        }, 200);
    };
    this.isComplete = function () { return this.State == this.Config.state.Complete; };
    this.svr_delete = function ()
    {
        var param = $.extend({}, this.fields,{id:this.fileSvr.id,time:new Date().getTime()});
        $.ajax({
            type: "GET"
            , dataType: 'jsonp'
            , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
            , url: _this.Config["UrlDel"]
            , data: param
            , success: function (json){}
            , error: function (req, txt, err) { alert("删除数据错误！" + req.responseText); }
            , complete: function (req, sta) { req = null; }
        });
    };

    this.svr_delete_file = function (f_id)
    {
        var param = $.extend({}, this.fields, {idSvr:f_id, time: new Date().getTime() });

        $.ajax({
            type: "GET"
            , dataType: 'jsonp'
            , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
            , url: _this.Config["UrlDel"]
            , data: param
            , success: function (json) { }
            , error: function (req, txt, err) { alert("删除数据错误！" + req.responseText); }
            , complete: function (req, sta) { req = null; }
        });
    };

    this.down_complete = function (json)
    {
        this.Manager.filesCmp.push(this);
        this.Manager.del_work(this.fileSvr.id);//从工作队列中删除
        this.Manager.remove_wait(this.fileSvr.id);
        this.Manager.remove_url(this.fileSvr.nameLoc);
        this.hideBtns();
        this.ui.btn.errInf.show();
        this.event.downComplete(this);//biz event
        this.ui.process.css("width", "100%");
        this.ui.percent.text("(100%)");

        if (typeof (json.errFiles) != "undefined") {
            var fileErrs = new Array();
            $.each(json.errFiles, function (i, n) {
                var tmp = "<li class=\"txt-ico\"><img title=\"{error}\" src=\"/js/error.png\"/>名称：{name} 大小：{size} <a class=\"btn-link\" href=\"{url}\" target=\"_blank\">打开链接</a></li>";
                tmp = tmp.replace(/{name}/gi, n.name);
                tmp = tmp.replace(/{url}/gi, n.url);
                tmp = tmp.replace(/{size}/gi, n.sizeSvr);
                tmp = tmp.replace(/{error}/gi, _this.Config.errCode[n.errCode]);
                fileErrs.push(tmp);
            });
            this.ui.errFiles.html(fileErrs.join(""));
        }

        var msg = ["下载完毕", " 共:", json.fileCount,
            " 成功:", json.cmpCount,
            "(空:", json.emptyCount,
            ",重复:", json.repeatCount,
            ") 失败:", json.errCount,
            " 用时:", json.timeUse];
        this.ui.msg.text(msg.join(""));
        this.ui.btn.open.show();
        this.State = this.Config.state.Complete;

        this.svr_delete();
        setTimeout(function () { _this.Manager.down_next(); }, 500);
    };

    this.down_process = function (json)
    {
        this.fileSvr.lenLoc = json.lenLoc;//保存进度
        this.fileSvr.perLoc = json.percent;
        this.ui.percent.text("(" + json.percent + ")");
        this.ui.process.css("width", json.percent);
        var msg = [json.index, "/", json.fileCount,
            " ", json.sizeLoc,
            " ", json.speed,
            " 剩余:", json.time,
            " 用时:", json.elapsed];
        this.ui.msg.text(msg.join(""));
    };

    this.down_begin = function (json)
    {
    };

    this.down_error = function (json)
    {
        this.hideBtns();
        this.ui.btn.down.show();
        this.ui.btn.del.show();
        this.ui.btn.errInf.show();
        this.event.downError(this, json.code);//biz event
        this.ui.msg.text(this.Config.errCode[json.code + ""]);

        var errors = new Array();
        var tmp = "<li class=\"m-t-xs\">文件：{count},\
        完成:{cmp}(空:{empty},重复:{repeat}),\
        错误:{err}(网络:{network},长路径:{lpath},空间不足:{full})\
        ,用时:{timeUse}</li>";
        tmp = tmp.replace(/{count}/gi, json.fileCount);
        tmp = tmp.replace(/{empty}/gi, json.emptyCount);
        tmp = tmp.replace(/{cmp}/gi, json.cmpCount);
        tmp = tmp.replace(/{err}/gi, json.errCount);
        tmp = tmp.replace(/{network}/gi, json.netCount);
        tmp = tmp.replace(/{repeat}/gi, json.repeatCount);
        tmp = tmp.replace(/{lpath}/gi, json.lpathCount);
        tmp = tmp.replace(/{full}/gi, json.fullCount);
        tmp = tmp.replace(/{timeUse}/gi, json.timeUse);
        errors.push(tmp);

        if (typeof (json.errFiles) != "undefined") {
            $.each(json.errFiles, function (i, n) {
                tmp = "\
                <li>\
                    <div class=\"txt-ico m-t-xs txt-limit-sm\">\
                        <a title=\"{name}\" class=\"txt-link\" href=\"{url}\" target=\"_blank\"><img name=\"err\"/>{name}</a>\
                    </div>\
                    <div style=\"margin:3px 0 0 0;\">大小：{size}</div>\
                    <div class=\"txt-limit-sm m-t-xs\" title=\"{path}\">路径：{path}</div>\
                    <div class=\"m-t-xs\">错误：{error}</div>\
                    <div class=\"m-t-xs\">错误码：{errCode}</div>\
                </li>";
                tmp = tmp.replace(/{name}/gi, n.name);
                tmp = tmp.replace(/{url}/gi, n.url);
                tmp = tmp.replace(/{size}/gi, n.sizeSvr);
                tmp = tmp.replace(/{error}/gi, _this.Config.errCode[n.errCode]);
                tmp = tmp.replace(/{path}/gi, n.pathLoc);
                tmp = tmp.replace(/{errCode}/gi, n.errCodeNet);
                errors.push(tmp);
            });
        }
        this.ui.errFiles.html(errors.join(""));
        this.ui.errFiles.find("img[name='err']").each(function (n, i) {
            $(this).attr("src", _this.Manager.data.ico.err);
        });
        this.State = this.Config.state.Error;
        this.svr_update();
        this.Manager.del_work(this.fileSvr.id);//从工作队列中删除
        this.Manager.add_wait(this.fileSvr.id);
    };

    this.down_stoped = function (json)
    {
        this.hideBtns();
        this.ui.btn.down.show();
        this.ui.btn.del.show();
        this.Manager.del_work(this.fileSvr.id);//从工作队列中删除
        this.Manager.add_wait(this.fileSvr.id);
    };
}