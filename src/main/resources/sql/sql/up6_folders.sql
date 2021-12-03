CREATE TABLE [dbo].[up6_folders](
	[f_id] [char](32) COLLATE Chinese_PRC_CI_AS NOT NULL,
	[f_nameLoc] [nvarchar](255) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_nameLoc]  DEFAULT (''),
	[f_pid] [char](32) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_pid]  DEFAULT (''),
	[f_uid] [int] NULL CONSTRAINT [DF_up6_folders_f_uid]  DEFAULT ((0)),
	[f_lenLoc] [bigint] NULL CONSTRAINT [DF_up6_folders_f_lenLoc]  DEFAULT ((0)),
	[f_sizeLoc] [nvarchar](50) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_sizeLoc]  DEFAULT (''),
	[f_pathLoc] [nvarchar](255) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_pathLoc]  DEFAULT (''),
	[f_pathSvr] [nvarchar](255) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_pathSvr]  DEFAULT (''),
	[f_folders] [int] NULL CONSTRAINT [DF_up6_folders_f_folders]  DEFAULT ((0)),
	[f_fileCount] [int] NULL CONSTRAINT [DF_up6_folders_f_fileCount]  DEFAULT ((0)),
	[f_filesComplete] [int] NULL CONSTRAINT [DF_up6_folders_f_filesComplete]  DEFAULT ((0)),
	[f_complete] [bit] NULL CONSTRAINT [DF_up6_folders_f_complete]  DEFAULT ((0)),
	[f_deleted] [bit] NULL CONSTRAINT [DF_up6_folders_f_deleted]  DEFAULT ((0)),
	[f_time] [datetime] NULL CONSTRAINT [DF_up6_folders_f_time]  DEFAULT (getdate()),
	[f_pidRoot] [char](32) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_pidRoot]  DEFAULT (''),
	[f_pathRel] [nvarchar](255) COLLATE Chinese_PRC_CI_AS NULL CONSTRAINT [DF_up6_folders_f_pathRel]  DEFAULT ('')
) ON [PRIMARY]

SET ANSI_PADDING OFF

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'文件夹名称' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_nameLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'父级ID' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_pid'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'用户ID。' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_uid'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'数字化的大小。以字节为单位。示例：1023652' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_lenLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'格式化的大小。示例：10G' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_sizeLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'文件夹在客户端的路径' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_pathLoc'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'文件夹在服务端的路径' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_pathSvr'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'文件夹数' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_folders'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'文件数' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_fileCount'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'已上传完的文件数' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_filesComplete'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'是否已上传完毕' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_complete'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'是否已删除' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_deleted'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'上传时间' ,@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE', @level1name=N'up6_folders', @level2type=N'COLUMN', @level2name=N'f_time'
