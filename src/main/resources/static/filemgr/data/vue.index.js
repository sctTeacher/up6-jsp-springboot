$(function () {
    var v_app = new Vue({
        el: '#app',
        data: {
            items: []
            , count: 0
            , pageCurr: 1
            , pageLimit: 20
            , url: {
                f_create: page.path.root + "/filemgr/f_create",
                fd_create: page.path.root + "/filemgr/fd_create",
                fd_data: page.path.root + "/filemgr/fd_data"
            }
            , fields: {uid:0}
            , pathNav: []
            , pathCur: { f_id: "", f_pid: "", f_pidRoot: "", f_nameLoc: "根目录", f_pathRel: "/" }
            , pathRoot: { f_id: "", f_pid: "", f_pidRoot: "", f_nameLoc: "根目录", f_pathRel: "/" }
            , idSels: []
            , idSelAll: false
            , downCur:null
            , folderMker: {name:'',edit:false}
            , ico: {
                file: page.path.res + 'imgs/16/file.png',
                folder: page.path.res + 'imgs/16/folder.png',
                folderMk: page.path.res + 'imgs/16/folder1.png',
                btnUp: page.path.res + "imgs/16/upload.png",
                btnUpFd: page.path.res + "imgs/16/folder.png",
                btnPaste: page.path.res + "imgs/16/paste.png",
                btnDel: page.path.res + "imgs/16/del.png",
                btnDown: page.path.res + "imgs/16/down.png",
                btnEdit: page.path.res + "imgs/16/edit.png",
                btnPnlUp: page.path.res + "imgs/16/up-panel.png",
                btnPnlDown: page.path.res + "imgs/16/down-panel.png",
                ok: page.path.res + "imgs/16/ok1.png",
                cancel: page.path.res + "imgs/16/cancel.png"
            }
            , cookie: ""
            , search: {key:''}
        }
        , mounted: function () {
            this.pathNav.push(this.pathRoot);
        }
        , methods: {
            tm_format: function (v) {
                return moment(v).format('YYYY-MM-DD HH:mm:ss');
            }
            , trim: function (v) {
                v = v.replace(/^\s*|\s*$/gi, "");
                return v;
            }
            , init_data: function () {
                var _this = this;
                var param = jQuery.extend({}, this.fields, { time: new Date().getTime() });
            $.ajax({
                type: "GET"
                , dataType: "json"
                , url: "/filemgr/data"
                , data: param
                , success: function (res){
                        _this.items = res.data;
                        _this.count = res.count;
                        _this.page_init();
                }
                , error: function (req, txt, err){}
                , complete: function (req, sta) { req = null; }
            });
        }
            , btnUp_click: function () {
                this.$refs.up6.btnFile_click();
            }
            , btnUpFolder_click: function () {
                this.$refs.up6.btnFolder_click();
            }
            , btnPaste_click: function () {
                this.$refs.up6.btnPaste_click();
            }
            , btnMkFolder_click: function () {
                this.folderMker.edit = true;
                var _this = this;
                setTimeout(function () {
                    _this.$refs.fdName.focus();
                 }, 10);
            }
            , btnMkFdOk_click: function () {
                var _this = this;
                this.folderMker.name = this.folderMker.name.replace(/^\s*|\s*$/gi, "");
                if (this.folderMker.name.length<1) {
                    layer.alert('文件夹名称不能为空！,', { icon: 2 });
                    return;
                }

                var param = $.extend({},this.fields, {
                    f_pid: this.pathCur.f_id
                    , f_pathRel: encodeURIComponent(this.pathCur.f_pathRel)
                    , f_pidRoot: ""
                    , f_nameLoc: encodeURIComponent(this.folderMker.name)
                });

                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/mk_folder"
                    , data: param
                    , success: function (res) {
                        if (!res.ret) {
                            layer.alert('创建失败,' + res.msg, { icon: 5 });
                        }
                        else {
                            var fd = $.extend(param,res,
                                {
                                    f_nameLoc:_this.folderMker.name,
                                    f_fdTask:true
                                });
                            _this.folderMker.name = '';                            
                            _this.folderMker.edit = false;
                            _this.items.unshift(fd);
                            _this.page_changed(1, 20);
                        }
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });
            }
            , btnMkFdCancel_click: function () {
                this.folderMker.edit = false;
                this.folderMker.name = '';
            }
            , btnOpenUp: function () { }
            , btnOpenDown: function () { }
            , btnDowns_click: function () {//批量下载
                if (!this.$refs.down.check_path()) return;
                var _this = this;
                $.each(this.idSels, function (index, id) {
                    var arr = $.grep(_this.items, function (n, i) {
                        return n.f_id == id;
                    });
                    var f = arr[0];
                    var dt = {
                        f_id: f.f_id
                        , lenSvr: f.f_lenLoc
                        , pathSvr: f.f_pathSvr
                        , nameLoc: f.f_nameLoc
                        , fileUrl: _this.$refs.down.mgr.Config["UrlDown"]
                    };
                    if (f.f_fdTask) {
                        _this.$refs.down.mgr.addFolder(dt);
                    }
                    else {
                        _this.$refs.down.mgr.addFile(dt);
                    }
                });
                this.openDown_click();
            }
            , btnDels_click: function () { }
            , btnDel_click: function (f) {
                var _this = this;
                layer.alert('确定要删除选中项：'+f.f_nameLoc+'？', {
                    time: 0 //不自动关闭
                    , btn: ['确定', '取消']
                    , yes: function (index) {
                        layer.close(index);

                        var param = jQuery.extend({}, {
                            id: f.f_id,
                            pathRel: encodeURIComponent(f.f_pathRel),
                            time: new Date().getTime()
                        });
                        $.ajax({
                            type: "GET"
                            , dataType: "json"
                            , url: "/filemgr/del"
                            , data: param
                            , success: function (res) {
                                _this.page_changed(1, 20);
                            }
                            , error: function (req, txt, err) { }
                            , complete: function (req, sta) { req = null; }
                        });

                    }
                });
            }
            , btnSearch_click: function () {
                this.search.key = this.trim(this.search.key);

                if (this.search.key.length == 0) {
                    this.page_changed(1, this.pageLimit);
                    return;
                }

                var _this = this;
                var param = $.extend({}, this.fields, {
                    "page": 1,
                    limit: 100,
                    pid: this.pathCur.f_id,
                    pathRel: encodeURIComponent(this.pathCur.f_pathRel),
                    key: encodeURIComponent(this.search.key),
                    time: new Date().getTime()
                });
                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/search"
                    , data: param
                    , success: function (res) {
                        _this.items = res.data;
                        _this.count = res.count;
                        _this.pageLimit = 100;
                        _this.page_init();
                        _this.pageLimit = 20;
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });
            }
            , searchKey_changed: function () {
                this.search.key = this.trim(this.search.key);
            }
            , selAll_click: function () {
                var _this = this;
                if (this.idSelAll) {
                    $.each(this.items, function (i, n) {
                        _this.idSels.push(n.f_id);
                    });
                }
                else {
                    this.idSels.length = 0;
                }
            }
            , openUp_click: function () {
                var _this = this;
                layer.open({
                    type: 1
                    , maxmin: true
                    , shade: 0//不显示遮罩
                    , title: '上传文件'
                    , offset: 'rb'//右下角
                    //, btn: ['确定', '取消']
                    , content: $("#pnl-up")
                    , area: ['452px', '562px']
                    , success: function (layero, index) {
                        $(_this.$refs.up6).show();
                    }
                    , btn1: function (index, layero) {
                        layer.close(index);//关闭窗口
                        $(_this.$refs.up6).hide();
                    }
                    , btn2: function (index, layero) {
                        $(_this.$refs.up6).hide();
                    }
                });
            }
            , openDown_click: function () {//打开下载面板
                var _this = this;
                layer.open({
                    type: 1
                    , maxmin: true
                    , shade: 0//不显示遮罩
                    , title: '文件下载'
                    , offset: 'rb'//右下角
                    //, btn: ['确定', '取消']
                    , content: $("#pnl-down")
                    , area: ['452px', '562px']
                    , success: function (layero, index) {
                        $(_this.$refs.down2).show();                        
                    }
                    , btn1: function (index, layero) {
                        layer.close(index);
                        $(_this.$refs.down2).hide();                        
                    }
                    , btn2: function (index, layero) {
                        $(_this.$refs.down2).hide();                        
                    }
                });
            }
            , nav_click: function (p) {
                var _this = this;
                this.idSels.length = 0;
                this.idSelAll = false;
                //加载路径
                var param = jQuery.extend({},this.fields, p,{time: new Date().getTime() });
                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/path"
                    , data: { data: encodeURIComponent(JSON.stringify(param)), pathRel:encodeURIComponent(p.f_pathRel) }
                    , success: function (res) {
                        _this.pathNav = res;
                        _this.path_changed(p);
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });
            }
            , open_tree: function (d) { }
            , open_folder: function (d) {
                if (!d.f_fdTask) return;

                this.nav_click(d);
                //$.extend(this.data.up6.Config.bizData, { "pid": d.f_id.replace(/\s*/g, ""), "pidRoot": d.f_pidRoot.replace(/\s*/g, "") });
            }
            , path_changed: function (d) {
                var _this = this;
                $.extend(this.pathCur, d);
                //加载文件列表
                var param = $.extend({}, this.fields, {
                    pid: d.f_id,
                    pathRel: encodeURIComponent( d.f_pathRel),
                    time: new Date().getTime()
                });
                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/data"
                    , data: param
                    , success: function (res) {
                        _this.items = res.data;
                        _this.count = res.count;
                        _this.page_init();
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });

            }
            , page_init: function () {
                var _this = this;
                layui.use('laypage', function () {
                    var laypage = layui.laypage;

                    laypage.render({
                        elem: 'pager' //
                        , count: _this.count//
                        , layout: ['prev', 'page', 'next', 'limit', 'count', 'skip']
                        , limit: _this.pageLimit
                        , curr: _this.pageCurr
                        , jump: function (obj, first) {
                            //首次不执行
                            if (!first) {
                                _this.pageLimit = obj.limit;
                                _this.pageCurr = obj.curr;
                                _this.page_changed(obj.curr, obj.limit);
                            }
                        }
                    });
                });
            }
            , page_changed: function (page,size) {
                var _this = this;
                var param = $.extend({}, this.fields, {
                    "page": page, 
                    limit: size,
                    pid: this.pathCur.f_id,
                    pathRel: encodeURIComponent(this.pathCur.f_pathRel),
                    key: encodeURIComponent( this.search.key),
                    time: new Date().getTime()
                });
                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/data"
                    , data: param
                    , success: function (res) {
                        _this.items = res.data;
                        _this.count = res.count;
                        _this.page_init();
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });

            }
            , itemDown_click: function (f) {
                this.downCur = f;
                if (!this.$refs.down.check_path()) return;
                var _this = this;
                var dt = {
                    f_id: f.f_id
                    , lenSvr: f.f_lenLoc
                    , pathSvr: f.f_pathSvr
                    , nameLoc: f.f_nameLoc
                    , fileUrl: this.$refs.down.mgr.Config["UrlDown"]
                };
                if (f.f_fdTask) _this.$refs.down.mgr.addFolder(dt);
                else _this.$refs.down.mgr.addFile(dt);
                this.openDown_click();
            }
            , itemRename_click: function (f, i) {
                //$.extend(f, { edit: true });
                $("div[name='v" + i + "']").hide();
                $("input[name='name" + i + "']").show().val(f.f_nameLoc);
                $("div[name='edit" + i + "']").show();
            }
            , btnRename_ok: function (f, i) {
                var nameNew = $("input[name='name" + i + "']").val();
                nameNew = nameNew.replace(/^\s*|\s*$/gi, "");
                if (nameNew.length<1) {
                    layer.alert('名称不能为空', { icon: 2 });
                    return;
                }
                var _this = this;
                var param = jQuery.extend({}, {"f_pid":f.f_pid,"f_id":f.f_id,f_nameLoc: encodeURIComponent(nameNew),f_fdTask:f.f_fdTask,time: new Date().getTime() });
                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/rename"
                    , data: param
                    , success: function (res) {
	                	if (!res.state) {
	                        layer.alert('重命名失败,' + res.msg, { icon: 5 });
	                    }
	                    else
	                    {
	                    	f.f_nameLoc = nameNew;
	                        _this.btnRename_cancel(f,i);
	                    }
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });

            }
            , btnRename_cancel: function (f, i) {
                $("div[name='v" + i + "']").show();
                //$("input[name='" + name + "']").show().val(f.f_nameLoc);
                $("div[name='edit" + i + "']").hide();
            }
            , up6_loadComplete: function () {
                this.up6_loadTask();
            }
            , up6_itemSelected: function () {
                this.openUp_click();
            }
            , up6_fileAppend: function (f) {                
                //为文件设置当前位置
                $.extend(f.fields, { pid: this.pathCur.f_id, pidRoot: "",pathRel:encodeURIComponent(this.pathCur.f_pathRel) });
            }
            , up6_fileComplete: function (f) {
                this.page_changed(1, 20);
            }
            , up6_folderComplete: function (f) {
                this.page_changed(1, 20);
            }
            , up6_unsetup:function(html){
                layer.open({
                    title: '提示',
                    content: html
                  });
            }
            , up6_loadTask: function () {
                var _this = this;
                var param = $.extend({},this.fields,{ time: new Date().getTime() });
                $.ajax({
                    type: "GET"
                    , dataType: "json"
                    , url: "/filemgr/uncomp"
                    , data: param
                    , success: function (res) {
                        if (res.length > 0) _this.openUp_click();

                        $.each(res, function (i, n) {
                            if (n.fdTask) {
                                var f = _this.$refs.up6.mgr.addFolderLoc(n);
                                f.folderInit = true;
                                f.folderScan = true;
                                f.ui.btn.post.show();
                                f.ui.btn.del.show();
                                f.ui.btn.cancel.hide();
                            }
                            else {
                                var f = _this.$refs.up6.mgr.addFileLoc(n);
                                f.ui.percent.text("(" + n.perSvr + ")");
                                f.ui.process.css("width", n.perSvr);
                                f.ui.btn.post.show();
                                f.ui.btn.del.show();
                                f.ui.btn.cancel.hide();
                            }
                        });
                    }
                    , error: function (req, txt, err) { }
                    , complete: function (req, sta) { req = null; }
                });
            }
            , down_loadComplete: function () {
                this.down_loadTask();
            }
            , down_sameFileExist: function (n) { }
            , down_unsetup:function(html){
                layer.open({
                    title: '提示',
                    content: html
                  });
            }
            , down_folderSel:function (){
                var _this = this;
                setTimeout(function (){
                    if(_this.downCur != null) _this.itemDown_click(_this.downCur);
                    _this.btnDowns_click();
                    _this.openDown_click();
                },500);
            }
            , down_fileAppend:function(o){
                var _this = this;
                setTimeout(function (){
                    _this.$refs.down.mgr.start_queue();
                },500);
            }
            , down_loadTask: function () {
                var _this = this;
                //加载未完成的任务
                var param = $.extend({}, this.$refs.down.mgr.Config.Fields);
                $.ajax({
                    type: "GET"
                    , dataType: 'json'
                    , jsonp: "callback" //自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
                    , url: "/filemgr/uncmp_down"
                    , data: param
                    , success: function (files) {
                        if (files.length > 0) { _this.openDown_click(); }
                        $.each(files, function (i, n) {
                            var dt = {
                                svrInit: true
                                , id: n.f_id
                                , lenLoc: n.f_lenLoc
                                , pathLoc: n.f_pathLoc
                                , nameLoc: n.f_nameLoc
                                , sizeSvr: n.f_sizeSvr
                                , perLoc: n.f_perLoc
                                , fdTask: n.f_fdTask
                            };
                            _this.$refs.down.mgr.resume_file(dt);
                        });
                    }
                    , error: function (req, txt, err) { alert("加载文件列表失败！" + req.responseText); }
                    , complete: function (req, sta) { req = null; }
                });
            }
        }
    });
    v_app.init_data(); 
});