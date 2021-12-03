CREATE TABLE up6_files
(
	 f_id				VARCHAR(32) NOT NULL			 /*文件ID，唯一。由于f_fid与oracle数据库字段有冲突，现改为f_idSign*/
	,f_pid				VARCHAR(32) DEFAULT ''		 /**/
	,f_pidRoot			VARCHAR(32) DEFAULT ''		 /*根级文件夹ID*/
	,f_fdTask			BOOL DEFAULT 0	 	 /*表示是否是一个文件夹上传任务。提供给 ajax_f_list.jsp使用*/	
	,f_fdChild			BOOL DEFAULT 0		 /*是文件夹中的子项*/
	,f_uid  			INT4 DEFAULT 0   	 /*用户ID*/
	,f_nameLoc 			VARCHAR(255)  DEFAULT '' /*文件在本地电脑中的名称。例：QQ.exe*/ 
	,f_nameSvr  		VARCHAR(255)  DEFAULT '' /*文件在服务器中的名称。一般为文件MD5+扩展名。*/
	,f_pathLoc 			VARCHAR(512)  DEFAULT '' /*文件在本地电脑中的完整路径。*/
	,f_pathSvr 		 	VARCHAR(512)  DEFAULT '' /*文件在服务器中的完整路径。*/
	,f_pathRel  		VARCHAR(512)  DEFAULT '' /*文件在服务器中的相对路径。*/
	,f_md5  			VARCHAR(40)   DEFAULT '' /*文件md5,sha1,crc*/
	,f_lenLoc  			INT8 DEFAULT 0  	 /*文件总长度。以字节为单位*/
	,f_sizeLoc  		VARCHAR(10) DEFAULT ''   /*格式化的文件尺寸。示例：10MB*/
	,f_pos  			INT8 DEFAULT 0  	 /*文件续传位置。*/
	,f_lenSvr			INT8 DEFAULT 0  	 /*已上传长度。以字节为单位。*/
	,f_perSvr  			VARCHAR(6) DEFAULT '0%'  /*已上传百分比。示例：10%*/
	,f_complete  		BOOL DEFAULT 0    /*是否已上传完毕。*/
	,f_time  			DATE DEFAULT sysdate     /*文件上传时间*/
	,f_deleted  		BOOL DEFAULT 0    /*是否已删除。*/
	,f_scan  			BOOL DEFAULT 0    /*是否扫描完毕。*/
);