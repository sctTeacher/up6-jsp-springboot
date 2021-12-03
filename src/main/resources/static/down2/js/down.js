/*
版权所有(C) 2009-2021 荆门泽优软件有限公司
保留所有权利
产品网站：http://www.ncmem.com/webapp/down2/index.aspx
控件下载：http://www.ncmem.com/webapp/down2/pack.aspx
示例下载：http://www.ncmem.com/webapp/down2/versions.aspx
联系邮箱：1085617561@qq.com
版本：2.4.15
*/
function debug_msg(v) { $(document.body).append("<div>"+v+"</div>");}

function DownloaderMgr(cfg)
{
	var _this = this;
	this.Config = {
		  "Folder"		: ""
		, "Debug"		: false//调试模式
		, "LogFile"		: "f:\\log.txt"//日志文件路径。
		, "Company"		: "荆门泽优软件有限公司"
		, "Version"		: page.path.version.down2
		, "License2"	: page.path.license2.down2
		, "Cookie"		: ""//
        , "NameLimit": { fileLen: 0, folderLen: 0,/*名称长度限制*/hash:""/**防重复码生成规则,md5,crc*/, hashLen: 0 }
        , "CfgFile": { gen: ""/**配置文件生成规则ori,crc,md5*/ }
		, "ThreadCount"	: 3//并发数
        , "ThreadBlock"	: 3//文件块线程数，每个文件使用多少线程下载数据。3~10
        , "ThreadChild" : 3//子文件线程数，提供给文件夹使用。3~10
        , "ThreadQuery"	: 5//查询线程,3~10
        , "SpeedLimit"  : 0//限速，以字节为单位。0表示不限制
        , "UrlEncode"	: true//url编码配置，默认开启
        , "IgoChildErr" : true//忽略子文件下载错误。
        , "TimeOut"     : 5//连接默认超时时间，以秒为单位
		, "FilePart"	: 104857600//文件块大小，计算器：http://www.beesky.com/newsite/bit_byte.htm
        , "FolderClear"	: true//下载前是否清空目录
        , "Proxy"       : {url: ""/**http://192.168.0.1:8888 */,pwd: ""/**admin:123456 */}//代理
        //file
        , "UrlCreate"   : page.path.root+"down2/f_create"
        , "UrlDel"      : page.path.root+"down2/f_del"
        , "UrlList"     : page.path.root+"down2/f_list"
        , "UrlListCmp"  : page.path.root+"down2/f_list_cmp"
        , "UrlUpdate"   : page.path.root+"down2/f_update"
        , "UrlDown"     : page.path.root+"down2/f_down"
	    //folder
        , "UrlFdData"   : page.path.root+"down2/fd_data"
        //x86
        , ie: {
              part: { clsid: "6528602B-7DF7-445A-8BA0-F6F996472569", name: "Xproer.DownloaderPartition" }
            , path: page.path.plugin.down2.ie32
        }
        //x64
        , ie64: {
            part: { clsid: "19799DD1-7357-49de-AE5D-E7A010A3172C", name: "Xproer.DownloaderPartition64" }
            , path: page.path.plugin.down2.ie64
        }
        , firefox: { name: "", type: "application/npHttpDown", path: page.path.plugin.down2.firefox }
        , chrome: { name: "npHttpDown", type: "application/npHttpDown", path: page.path.plugin.down2.chr }
        , chrome45: { name: "com.xproer.down2", path: page.path.plugin.down2.chr }
        , exe: { path: page.path.plugin.down2.exe }
        , mac: { path: page.path.plugin.down2.mac }
        , linux: { path: page.path.plugin.down2.linux }
        , arm64: { path: page.path.plugin.down2.arm64 }
        , mips64: { path: page.path.plugin.down2.mips64 }
        , edge: {protocol:"down2",port:9700,visible:false}
        , "Fields": { "uname": "test", "upass": "test", "uid": "0" }
        , errCode: {
            "0": "发送数据错误",
            "1": "接收数据错误",
            "2": "访问本地文件错误",
            "3": "域名未授权",
            "4": "文件大小超过限制",
            "5": "地址为空",
            "6": "配置文件不存在",
            "7": "本地目录不存在",
            "8": "查询文件信息失败",
            "9": "子文件大小超过限制",
            "10": "子文件数量超过限制",
            "11": "连接服务器失败",
            "12": "url地址错误",
            "13": "服务器错误",
            "14": "打开配置文件失败",
            "15": "空间不足",
            "16": "文件名称为空",
            "17": "服务器大小与请求大小不一致",
            "18": "已存在相同任务",
            "19": "路径过长",
            "20": "访问被拒绝"
        }
        , state: {
            Ready: 0,
            Posting: 1,
            Stop: 2,
            Error: 3,
            GetNewID: 4,
            Complete: 5,
            WaitContinueUpload: 6,
            None: 7,
            Waiting: 8
        }
        , ui: {
            path: 'span[name="path"]',
            file: 'div[name="file"]',
            panel: 'div[name="down_panel"]',
            list: 'div[name="down_body"]',
            setupPnl: 'div[name="setupPnl"]',
            header: 'div[name="down_header"]',
            toolbar: 'div[name="down_toolbar"]',
            btn: {
                setFolder: "span[name='btnSetFolder']",
                start: "span[name='btnStart']",
                stop: "span[name='btnStop']",
                setup: 'span[name="btnSetup"]',
                setupCmp: 'span[name="btnSetupCmp"]',
                clear: 'span[name="btnClear"]'
            },
            ele: {
                ico: {
                    file: 'img[name="file"]',
                    fd: 'img[name="folder"]',
                    error: 'img[name="err"]',
                    inf: 'img[name="inf"]'
                },
                name: 'div[name="name"]',
                size: 'div[name="size"]',
                process: 'div[name="process"]',
                percent: 'div[name="percent"]',
                msg: 'div[name="msg"]',
                errPnl: 'div[name="errPnl"]',
                errFiles: 'ul[name="errFiles"]',
                btn: {
                    cancel: 'span[name="cancel"]',
                    stop: 'span[name="stop"]',
                    down: 'span[name="down"]',
                    del: 'span[name="del"]',
                    open: 'span[name="open"]',
                    openFd: 'span[name="open-fd"]',
                    errInf: 'a[name="btnErrInf"]'
                }
            }
        },
        event:{}
    };
    $.extend(this.Config,cfg);

    this.event = {
          downComplete: function (obj) { },
          downError: function (obj, err) { },
          queueComplete: function () { },
          folderSel: function (path) { },
          ready: function () { },
        fileAppend: function (obj) { },/**添加任务 */
        selFolder: function (dir) { }
    };
    $.extend(this.event,this.Config.event);

    this.data = {
        browser: {
            name: navigator.userAgent.toLowerCase(), ie: true, ie64: false, firefox: false, chrome: false, edge: false, arm64: false, mips64: false
        },
        cur: null,
        ico: {
            file: "/static/down2/js/file.png",
            folder: "/static/down2/js/folder.png",
            err: "/static/down2/js/error.png",
            inf: "/static/down2/js/inf.png"
        },
    };
    this.ui = {
        list: null, file: null, path: null, setupPnl: null,
        btn: { selFolder: null, start: null, stop: null, setup: null, clear: null }
    };

    this.websocketInited = false;
	this.data.browser.ie = this.data.browser.name.indexOf("msie") > 0;
	this.data.browser.ie = this.data.browser.ie ? this.data.browser.ie : this.data.browser.name.search(/(msie\s|trident.*rv:)([\w.]+)/) != -1;
	this.data.browser.firefox = this.data.browser.name.indexOf("firefox") > 0;
	this.data.browser.chrome = this.data.browser.name.indexOf("chrome") > 0;
	this.data.browser.arm64 = this.data.browser.name.indexOf("aarch64") > 0;
	this.data.browser.mips64 = this.data.browser.name.indexOf("mips64") > 0;
    this.data.browser.edge = this.data.browser.name.indexOf("edge") > 0;
    this.pluginInited = false;
    this.edgeApp = new WebServerDown2(this);
    this.edgeApp.ent.on_close = function () { _this.stop_queue(); };
    this.app = down2_app;
    this.app.edgeApp = this.edgeApp;
    this.app.Config = this.Config;
    this.app.ins = this;
    if (this.data.browser.edge) { this.data.browser.ie = this.data.browser.firefox = this.data.browser.chrome = this.data.browser.chrome45 = false; }
	
	this.idCount = 1; 	//上传项总数，只累加
	this.queueCount = 0;//队列总数
	this.filesMap = new Object(); //本地文件列表映射表
	this.filesCmp = new Array();//已完成列表
	this.filesUrl = new Array();
    this.queueWait = new Array(); //等待队列，数据:id1,id2,id3
    this.queueWork = new Array(); //正在上传的队列，数据:id1,id2,id3
	this.parter = null;
    this.working = false;
    this.allStoped = false;//

    //api
    /**
     * 添加up6文件任务
     * 参数：
     * lenSvr 远程文件大小
     * pathSvr 远程文件路径
     * nameLoc 文件名称
     * fileUrl 文件URL
     * folderLoc 本地下载路径
     * f_id up6文件ID
     */
    this.addFile = function (f) {
        if (!this.pluginCheck()) return;
        var pv = $.extend({},f,{fileUrl:this.Config["UrlDown"]});
        this.app.addFile(pv);
    };
    /**
     * 添加up6文件夹任务
     * 参数：
     * lenSvr 远程文件夹大小
     * pathSvr 远程文件夹路径
     * nameLoc 文件名称
     * fileUrl 文件URL
     * folderLoc 本地下载路径
     * f_id up6文件夹ID
     */
    this.addFolder = function (v) {
        if (!this.pluginCheck()) return;
        this.app.addFolder(v);
    };
    this.addTask = function(v){
        if(v.fdTask) this.addFolder(v);
        else this.addFile(v);
    };
    this.addUrl = function (data) {
        if (!this.pluginCheck()) return;
        this.app.addUrl(data);
    };
    this.addUrls = function (data) {
        if (!this.pluginCheck()) return;
        this.app.addUrls(data);
    };
    this.addJson = function (data) {
        if (!this.pluginCheck()) return;
        this.app.addJson(data);
    };
    this.openFolder = function () {
        if (!this.pluginCheck()) return;
        this.app.openFolder();
    };

    //biz
	this.getHtml = function()
	{ 
        var html = "";
        html += '<object name="parter" classid="clsid:' + this.Config.ie.part.clsid + '"';
        html += ' codebase="' + this.Config.ie.path + '#version=' + _this.Config["Version"] + '" width="1" height="1" ></object>';
        if (this.data.browser.edge) html = '';
        //上传列表项模板
        html += '<div class="file-item file-item-single" name="file">\
                    <div class="img-box"><img name="file" src="/static/down2/js/file.png"/><img class="d-hide" name="folder" src="/static/down2/js/folder.png"/></div>\
                    <div>\
                        <div class="area-l">\
                            <div name="name" class="name">HttpUploader程序开发.pdf</div>\
                            <div name="percent" class="percent">(35%)</div>\
                            <div name="size" class="size" child="1">1000.23MB</div>\
                            <div class="process-border"><div name="process" class="process"></div></div>\
                            <div name="msg" class="msg top-space">15.3MB 20KB/S 10:02:00</div>\
                        </div>\
                        <div class="area-r">\
                            <span class="btn-box d-hide" name="down" title="继续"><div>继续</div></span>\
                            <span class="btn-box d-hide" name="stop" title="停止"><div>停止</div></span>\
                            <span class="btn-box d-hide" name="cancel" title="取消">取消</span>\
                            <span class="btn-box d-hide" name="del" title="删除"><div>删除</div></span>\
                            <span class="btn-box d-hide" name="open" title="打开"><div>打开</div></span>\
                            <span class="btn-box d-hide" name="open-fd" title="文件夹"><div>文件夹</div></span>\
                        </div>\
                        <div name="errPnl" class="msg top-space err-panel"><a name="btnErrInf" class="btn-ico"><img name="inf" src="/static/down2/js/inf.png"/>详细信息</a><ul name="errFiles" class="list-v d-hide"></ul></div>\
                    </div>\
				</div>';
        //上传列表
        html += '<div class="files-panel" name="down_panel">\
                    <div class="header" name="down_header">下载文件<span name="path"></span></div>\
					<div name="down_toolbar" class="toolbar">\
						<span class="btn-t d-hide" name="btnSetFolder"><div>设置下载目录</div></span>\
						<span class="btn-t d-hide" name="btnStart">全部下载</span>\
						<span class="btn-t d-hide" name="btnStop">全部停止</span>\
						<span class="btn-t" name="btnSetup">安装控件</span>\
						<span class="btn-t" name="btnSetupCmp">我已安装</span>\
						<span class="btn-t d-hide" name="btnClear">清除已完成</span>\
					</div>\
					<div class="content" name="down_content">\
						<div name="down_body" class="file-post-view d-hide"></div>\
                        <div name="setupPnl" class="file-post-view"></div>\
					</div>\
				</div>';
        return html;
	};

    this.to_params= function (param, key) {
        var paramStr = "";
        if (param instanceof String || param instanceof Number || param instanceof Boolean) {
            paramStr += "&" + key + "=" + encodeURIComponent(param);
        } else {
            $.each(param, function (i) {
                var k = key == null ? i : key + (param instanceof Array ? "[" + i + "]" : "." + i);
                paramStr += '&' + _this.to_params(this, k);
            });
        }
        return paramStr.substr(1);
    };
	this.set_config = function (v) { $.extend(this.Config, v); };
	this.clearComplete = function ()
	{
	    $.each(this.filesCmp, function (i,n)
	    {
            n.remove();
            _this.remove_url(n.fileSvr.nameLoc);
	    });
	    this.filesCmp.length = 0;
    };
    this.find_ui = function (o) {
        var tmp = {
            ico: {
                file: o.find(this.Config.ui.ele.ico.file)
                , fd: o.find(this.Config.ui.ele.ico.fd)
            }
            , name: o.find(this.Config.ui.ele.name)
            , size: o.find(this.Config.ui.ele.size)
            , process: o.find(this.Config.ui.ele.process)
            , percent: o.find(this.Config.ui.ele.percent)
            , msg: o.find(this.Config.ui.ele.msg)
            , errPnl: o.find(this.Config.ui.ele.errPnl)
            , errFiles: o.find(this.Config.ui.ele.errFiles)
            , btn: {
                cancel: o.find(this.Config.ui.ele.btn.cancel)
                , stop: o.find(this.Config.ui.ele.btn.stop)
                , down: o.find(this.Config.ui.ele.btn.down)
                , del: o.find(this.Config.ui.ele.btn.del)
                , open: o.find(this.Config.ui.ele.btn.open)
                , openFd: o.find(this.Config.ui.ele.btn.openFd)
                , errInf: o.find(this.Config.ui.ele.btn.errInf)
            }
            , div: o
        };
        $.each(tmp.btn, function (i, n) {
            n.hover(function () {
                $(this).addClass("bk-hover");
            }, function () { $(this).removeClass("bk-hover"); });
        });
        return tmp;
    };
	this.add_ui = function (f)
    {
		if (this.exist_url(f.nameLoc)) {
            alert("已存在相同名称的任务：" + f.nameLoc);
            return null;
        }
        this.filesUrl.push(f.nameLoc);

	    var _this = this;

        var tmp = this.ui.file.clone();
	    tmp.css("display", "block");
	    this.ui.list.append(tmp);
        var ui = this.find_ui(tmp);

        tmp.find('span[tp="btn-item"]').hover(function () {
            $(this).addClass("bk-hover");
        }, function () {$(this).removeClass("bk-hover");});

        var downer;
        if (f.fdTask) { downer = new FdDownloader(f, this); }
	    else { downer = new FileDownloader(f,this);}
	    this.filesMap[f.id] = downer;//
        downer.ui = ui;

	    ui.name.text(f.nameLoc);
	    ui.name.attr("title", f.nameLoc);
	    ui.msg.text("");
	    ui.size.text(f.sizeSvr);
	    ui.percent.text("("+f.perLoc+")");
        ui.process.width(f.perLoc);

        downer.ready(); //准备
        this.event.fileAppend(downer);
    };
	this.resume_folder = function (fdSvr)
    {
        var fd = $.extend({}, fdSvr, { svrInit: true });
	    this.add_ui(fd);
	    //if (null == obj) return;
        //obj.svr_inited = true;

	    //return obj;
    };
    this.resume_file = function (fSvr) {
        var f = $.extend({}, fSvr, { svrInit: true });
        this.add_ui(f);
        //if (null == obj) return;
        //obj.svr_inited = true;

        //return obj;
    };
	this.init_file = function (f)
    {
        this.app.initFile(f);
    };
    this.init_folder = function (f) {
        this.app.initFolder($.extend({},this.Config,f));
    };
    this.init_file_cmp = function (json)
    {
        var p = this.filesMap[json.id];
        p.init_complete(json);
    };
    this.add_file = function (f) {
        var obj = this.add_ui(f);
    };
    this.add_folder = function (f)
	{
	    var obj = this.add_ui(f);
	};
	this.exist_url = function (tn)
    {
        var ret = false;
        $.each(this.filesUrl, function (i, n) {
            if (n == tn) {
                ret = true;
                return true;
            }
        });
        return ret;
	};
    this.remove_url = function (url) {
        this.filesUrl = $.grep(this.filesUrl, function (n, i) {
            return n == url;
        },true);
    };
    this.remove_wait = function (id) {
        if (this.queueWait.length == 0) return;
        this.queueWait = $.grep(this.queueWait, function (n, i){
            return n == id;
        }, true);
    };
    this.down_file = function (json) { };
    //队列控制
    this.work_full = function () { return (this.queueWork.length + 1) > this.Config.ThreadCount; };
    this.add_wait = function (id) { this.queueWait.push(id); };
    this.add_work = function (id) { this.queueWork.push(id); };
    this.del_work = function (id) {
        if (_this.queueWork.length < 1) return;
        this.queueWork = $.grep(this.queueWork, function (n, i){
            return n = id;
        }, true);
    };
    this.down_next = function () {
        if (_this.allStoped) return;
        if (_this.work_full()) return;
        if (_this.queueWait.length < 1) return;
        var f_id = _this.queueWait.shift();
        var f = _this.filesMap[f_id];
        f.down();
    };

	this.init_end = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.init_end(json);
	};
	this.down_begin = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_begin(json);
	};
	this.down_process = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_process(json);
	};
	this.down_error = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_error(json);
    };
    this.down_open_folder = function (json) {
        //用户选择的路径
        //json.path
        this.Config["Folder"] = json.path;
        this.event.folderSel(json.path);
    };
	this.down_recv_size = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_recv_size(json);
	};
	this.down_recv_name = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_recv_name(json);
	};
	this.down_complete = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_complete(json);
	};
	this.down_stoped = function (json)
	{
	    var p = this.filesMap[json.id];
	    p.down_stoped(json);
	};
    this.start_queue = function ()
    {
        this.allStoped = false;
        this.down_next();
    };
	this.stop_queue = function (json)
    {
        this.allStoped = true;
        $.each(this.queueWork, function (i, n) {
            _this.filesMap[n].stop();
        });        
	};
	this.queue_begin = function (json) { this.working = true;};
	this.queue_end = function (json) { this.working = false;};
    this.load_complete = function (json) {
        if (this.websocketInited) return;
        this.websocketInited = true;
        this.pluginInited = true;

        this.ui.btn.selFolder.removeClass("d-hide");
        this.ui.btn.start.removeClass("d-hide");
        this.ui.btn.stop.removeClass("d-hide");
        this.ui.btn.clear.removeClass("d-hide");
        this.ui.list.removeClass("d-hide");
        this.ui.setupPnl.addClass("d-hide");
        this.ui.btn.setup.hide();
        this.ui.btn.setupCmp.hide();

        setTimeout(function () { 
            _this.loadFiles();//
            _this.event.ready(); }, 300);

        var needUpdate = true;
        if (typeof (json.version) != "undefined") {
            if (json.version == this.Config.Version) {
                needUpdate = false;
            }
        }
        if (needUpdate) this.update_notice();
        else {
            this.ui.btn.setup.hide();
            this.ui.btn.setupCmp.hide();
        }
    };
    this.load_complete_edge = function (json) {
        this.pluginInited = true;
        this.ui.btn.setup.hide();
        this.ui.btn.setupCmp.hide();
        _this.app.init();
    };
	this.recvMessage = function (str)
	{
	    var json = JSON.parse(str);
	         if (json.name == "init_file_cmp") { _this.init_file_cmp(json); }
	    else if (json.name == "open_folder") { _this.down_open_folder(json); }
	    else if (json.name == "down_recv_size") { _this.down_recv_size(json); }
	    else if (json.name == "down_recv_name") { _this.down_recv_name(json); }
	    else if (json.name == "init_end") { _this.init_end(json); }
	    else if (json.name == "add_file") { _this.add_file(json); }
	    else if (json.name == "add_folder") { _this.add_folder(json); }
	    else if (json.name == "down_begin") { _this.down_begin(json); }
	    else if (json.name == "down_process") { _this.down_process(json); }
	    else if (json.name == "down_error") { _this.down_error(json); }
	    else if (json.name == "down_complete") { _this.down_complete(json); }
	    else if (json.name == "down_stoped") { _this.down_stoped(json); }
	    else if (json.name == "queue_complete") { _this.event.queueComplete(); }
	    else if (json.name == "queue_begin") { _this.queue_begin(json); }
	    else if (json.name == "queue_end") { _this.queue_end(json); }
	    else if (json.name == "load_complete") { _this.load_complete(json); }
	    else if (json.name == "load_complete_edge") { _this.load_complete_edge(json); }
    };

    this.pluginLoad = function () {
        if (!this.pluginInited) {
            if (this.data.browser.edge) {
                this.edgeApp.connect();
            }
        }
    };
    this.pluginCheck = function () {
        if (!this.pluginInited) {
            alert("控件没有加载成功，请安装控件或等待加载。");
            this.pluginLoad();
            return false;
        }
        return true;
    };
    this.checkBrowser = function ()
	{
	    //Win64
	    if (window.navigator.platform == "Win64")
	    {
	        $.extend(this.Config.ie, this.Config.ie64);
        }//macOS
        else if (window.navigator.platform == "MacIntel") {
            this.data.browser.edge = true;
            this.app.postMessage = this.app.postMessageEdge;
            this.edgeApp.run = this.edgeApp.runChr;
            this.Config.exe.path = this.Config.mac.path;
        }//linux
        else if (window.navigator.platform == "Linux x86_64") {
            this.data.browser.edge = true;
            this.app.postMessage = this.app.postMessageEdge;
            this.edgeApp.run = this.edgeApp.runChr;
            this.Config.exe.path = this.Config.linux.path;
        }//Linux aarch64
        else if (this.data.browser.arm64) {
            this.data.browser.edge = true;
            this.app.postMessage = this.app.postMessageEdge;
            this.edgeApp.run = this.edgeApp.runChr;
            this.Config.exe.path = this.Config.arm64.path;
        }//Linux mips64
        else if (this.data.browser.mips64) {
            this.data.browser.edge = true;
            this.app.postMessage = this.app.postMessageEdge;
            this.edgeApp.run = this.edgeApp.runChr;
            this.Config.exe.path = this.Config.mips64.path;
        }
	    else if (this.data.browser.firefox)
        {
            this.data.browser.edge = true;
            this.app.postMessage = this.app.postMessageEdge;
            this.edgeApp.run = this.edgeApp.runChr;
        }
	    else if (this.data.browser.chrome)
	    {
	        this.app.check = this.app.checkFF;
	        $.extend(this.Config.firefox, this.Config.chrome);
            this.data.browser.edge = true;
            this.app.postMessage = this.app.postMessageEdge;
            this.edgeApp.run = this.edgeApp.runChr;
        }
        else if (this.data.browser.edge) {
            this.app.postMessage = this.app.postMessageEdge;
        }
	};
    this.checkBrowser();

    //升级通知
    this.update_notice = function () {
        this.ui.btn.setup.text("升级控件");
        this.ui.btn.setup.css("color", "red");
        this.ui.btn.setup.show();
    };

	//安全检查，在用户关闭网页时自动停止所有上传任务。
	this.safeCheck = function()
    {
        $(window).bind("beforeunload", function (event)
        {
            if (_this.queueWork.length > 0)
            {
				event.returnValue = "您还有程序正在运行，确定关闭？";
            }
        });
        
		$(window).bind("unload", function()
        {
            if (_this.data.browser.edge) _this.edgeApp.close();
            if (_this.queueWork.length > 0)
            {
                _this.stop_queue();
            }
        });
	};
	
	this.loadAuto = function()
	{
	    var html = this.getHtml();
	    var ui = $(document.body).append(html);
	    this.initUI(ui);
	};
	//加截到指定dom
	this.loadTo = function(id)
	{
	    var obj = $("#" + id);
	    var html = this.getHtml();
	    var ui = obj.append(html);
	    this.initUI(ui);
	};
	this.initUI = function (ui/*jquery obj*/)
    {
        this.down_panel = ui.find(this.Config.ui.panel);
        this.ui.btn.setup = ui.find(this.Config.ui.btn.setup);
        this.ui.btn.setupCmp = ui.find(this.Config.ui.btn.setupCmp);
        this.ui.file = ui.find(this.Config.ui.file);
        this.ui.path = ui.find(this.Config.ui.path);
        this.parter = ui.find('embed[name="ffParter"]').get(0);
        this.ieParter = ui.find('object[name="parter"]').get(0);

        var down_body = ui.find(this.Config.ui.list);
        var down_head = ui.find(this.Config.ui.header);
        var post_bar = ui.find(this.Config.ui.toolbar);
        down_body.height(this.down_panel.height() - post_bar.height() - down_head.height() - 1);

        this.ui.btn.selFolder = ui.find(this.Config.ui.btn.setFolder);
        this.ui.btn.start = ui.find(this.Config.ui.btn.start);
        this.ui.btn.stop = ui.find(this.Config.ui.btn.stop);
        this.ui.btn.clear = ui.find(this.Config.ui.btn.clear);
        this.ui.list = down_body;
        this.ui.setupPnl = ui.find(this.Config.ui.setupPnl);
        this.ui.setupPnl.height(this.ui.list.height());
        //设置下载文件夹
        this.ui.btn.selFolder.click(function () { _this.openFolder(); });
        this.ui.btn.setup.click(function () { window.open(_this.Config.exe.path); });
        this.ui.btn.setupCmp.click(function () {_this.edgeApp.connect(); });
        //清除已完成
        this.ui.btn.clear.click(function () { _this.clearComplete(); });
        this.ui.btn.start.click(function () { _this.start_queue(); });
        this.ui.btn.stop.click(function () { _this.stop_queue(); });
        ui.find('.btn-t').hover(function () {
            $(this).addClass("bk-hover");
        }, function () { $(this).removeClass("bk-hover"); });
        //图标
        ui.find(this.Config.ui.ele.ico.file).attr("src", this.data.ico.file);
        ui.find(this.Config.ui.ele.ico.fd).attr("src", this.data.ico.folder);
        ui.find(this.Config.ui.ele.ico.inf).attr("src", this.data.ico.inf);

        //this.LoadData();
        this.safeCheck();//

        setTimeout(function () {
            if (!_this.data.browser.edge) {
                if (_this.data.browser.ie) {
                    _this.parter = _this.ieParter;
                }
                _this.parter.recvMessage = _this.recvMessage;
            }

            if (_this.data.browser.edge) {
                _this.edgeApp.connect();
            }
            else {
                _this.app.init();
            }
        }, 500);
	};

    //加载未未完成列表
	this.loadFiles = function ()
	{
	    var param = $.extend({}, this.Config.Fields, { time: new Date().getTime()});
	    $.ajax({
	        type: "GET"
            , dataType: 'jsonp'
            , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
            , url: _this.Config["UrlList"]
            , data: param
            , success: function (msg)
            {
                if (msg.value == null) return;
                var files = JSON.parse(decodeURIComponent(msg.value));

                for (var i = 0, l = files.length; i < l; ++i)
                {
                    if (files[i].fdTask)
                    { _this.resume_folder(files[i]); }
                    else { _this.resume_file(files[i]); }
                }
            }
            , error: function (req, txt, err) { alert("加载文件列表失败！" +req.responseText); }
            , complete: function (req, sta) {req = null;}
	    });
	};
}
