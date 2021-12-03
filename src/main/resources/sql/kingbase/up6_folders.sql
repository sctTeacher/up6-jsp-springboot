--drop table up6_folders
CREATE TABLE up6_folders
(
	   f_id					VARCHAR(32) NOT NULL   		 /*文件夹ID*/
	  ,f_nameLoc  			VARCHAR(255) DEFAULT ''   /*文件夹名称*/
	  ,f_pid  				VARCHAR(32) DEFAULT ''   	 /*父级ID */
	  ,f_uid  				INT4 DEFAULT 0   	 /*用户ID*/
	  ,f_lenLoc				INT8 DEFAULT 0 	 	 /*数字化的大小。以字节为单位，示例：1024551*/
	  ,f_sizeLoc  			VARCHAR(10) DEFAULT '' 	 /*格式化的大小。示例：10G*/
	  ,f_pathLoc			VARCHAR(255) DEFAULT ''  /*文件夹在客户端的路径。*/
	  ,f_pathSvr			VARCHAR(255) DEFAULT ''  /*文件夹在服务端的路径。*/
	  ,f_folders			INT4 DEFAULT 0  	 /*子文件夹数*/
	  ,f_fileCount  		INT4 DEFAULT 0  	 /*子文件数*/
	  ,f_filesComplete  	INT4 DEFAULT 0  	 /*已上传完的文件数量*/
	  ,f_complete  			BOOL DEFAULT 0  	 /*是否已上传完毕*/
	  ,f_deleted  			BOOL DEFAULT 0  	 /*是否已删除*/
	  ,f_time				DATE DEFAULT sysdate  	 /*上传时间*/
	  ,f_pidRoot			VARCHAR(32) default ''		 /*根级ID*/
	  ,f_pathRel			VARCHAR(255) default ''  /*相对路径。基于顶级节点。root\\child\\self*/
)