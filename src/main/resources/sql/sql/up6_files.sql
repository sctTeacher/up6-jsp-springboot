CREATE TABLE [dbo].[up6_files](
	[f_id] [char](32) COLLATE Chinese_PRC_CI_AS NOT NULL,
	[f_pid] [char](32) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_pid]  DEFAULT (''),
	[f_pidRoot] [char](32) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_pidRoot]  DEFAULT (''),
	[f_fdTask] [bit] NULL CONSTRAINT [DF_up6_files_f_fdTask]  DEFAULT ((0)),
	[f_fdChild] [bit] NULL CONSTRAINT [DF_up6_files_f_fdChild]  DEFAULT ((0)),
	[f_uid] [int] NULL CONSTRAINT [DF_up6_files_f_uid]  DEFAULT ((0)),
	[f_nameLoc] [nvarchar](255) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_nameLoc]  DEFAULT (''),
	[f_nameSvr] [nvarchar](255) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_nameSvr]  DEFAULT (''),
	[f_pathLoc] [nvarchar](512) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_pathLoc]  DEFAULT (''),
	[f_pathSvr] [nvarchar](512) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_pathSvr]  DEFAULT (''),
	[f_pathRel] [nvarchar](512) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_pathRel]  DEFAULT (''),
	[f_md5] [nvarchar](40) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_md5]  DEFAULT (''),
	[f_lenLoc] [bigint] NULL CONSTRAINT [DF_up6_files_f_lenLoc]  DEFAULT ((0)),
	[f_sizeLoc] [nvarchar](10) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_sizeLoc]  DEFAULT ('0Bytes'),
	[f_pos] [bigint] NULL CONSTRAINT [DF_up6_files_f_pos]  DEFAULT ((0)),
	[f_lenSvr] [bigint] NULL CONSTRAINT [DF_up6_files_f_lenSvr]  DEFAULT ((0)),
	[f_perSvr] [nvarchar](6) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_files_f_perSvr]  DEFAULT ('0%'),
	[f_complete] [bit] NULL CONSTRAINT [DF_up6_files_f_complete]  DEFAULT ((0)),
	[f_time] [datetime] NULL CONSTRAINT [DF_up6_files_f_time]  DEFAULT (getdate()),
	[f_deleted] [bit] NULL CONSTRAINT [DF_up6_files_f_deleted]  DEFAULT ((0)),
	[f_scan] [bit] NOT NULL CONSTRAINT [DF_up6_files_f_scan]  DEFAULT ((0)),
 CONSTRAINT [PK_up6_files_1] PRIMARY KEY CLUSTERED 
(
	[f_id] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_PADDING OFF

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'????????????GUID,???????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_id'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'???????????????ID' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_pid'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'???????????????ID' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_pidRoot'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????????????????????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_fdTask'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_fdChild'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????????????????????????????QQ.exe ' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_nameLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'????????????????????????????????????????????????MD5+????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_nameSvr'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????????????????????????????
?????????D:\Soft\QQ.exe
' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_pathLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'???????????????????????????????????????
?????????F:\ftp\user1\QQ2012.exe
' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_pathSvr'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'???????????????????????????????????????
?????????/www/web/upload/QQ2012.exe
' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_pathRel'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????MD5' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_md5'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'????????????????????????????????????
????????????9,223,372,036,854,775,807
' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_lenLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'????????????????????????????????????10MB' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_sizeLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'?????????????????????
????????????9,223,372,036,854,775,807
' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_pos'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'???????????????????????????????????????
????????????9,223,372,036,854,775,807
' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_lenSvr'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????????????????10%' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_perSvr'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'????????????????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_complete'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_time'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'??????????????????' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_files', @level2type=N'COLUMN', @level2name=N'f_deleted'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'?????????????????????????????????????????????????????????' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'up6_files', @level2type=N'COLUMN',@level2name=N'f_scan'
