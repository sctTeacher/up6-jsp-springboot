USE [up6]
GO

/****** Object:  StoredProcedure [dbo].[spPager]    Script Date: 05/21/2018 10:18:31 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE procedure [dbo].[spPager] 
(@table varchar(200),
@primarykey varchar(50),
@pagesize int,
@pageindex int,
@docount bit,
@where nvarchar(1000),
@sort varchar(50),
@fields varchar(500)
)
as
DECLARE @sql nvarchar(2000)
DECLARE @sqlParam nvarchar(1000)
	
	if( len(@where)<1)
		begin
			set @where = ' 1=1'
		end
	else if @where is null
		begin
			set @where = '1=1'
		end
	
	/*排序字段*/
	if( len(@sort)<1)
		begin
			set @sort = @primarykey +' desc'
		end
	else if @sort is null
		begin
			set @sort = @primarykey +' desc'
		end

		/*字段列表*/
	if( len(@fields)<1)
		begin
			set @fields = '*'
		end
	else if @fields is null
		begin
			set @fields = '*'
		end

if( @docount = 1)
	begin
	set @sql = N'select count('+@primarykey+N') from ' + @table + ' where ' + @where
	

	EXECUTE sp_executesql @sql
	end
else
	begin	
	SET @sqlParam = N'@size int, @index int'

	
	set @sql = N'
	 with temptbl as (
	SELECT ROW_NUMBER() OVER (ORDER BY '+@sort+' )AS Row, '+@fields+' from '+@table+' where '+@where+')
	 SELECT * FROM temptbl where Row between (@index-1)*@size+1 and (@index-1)*@size+@size '

	EXECUTE sp_executesql @sql, @sqlParam, @size = @pagesize,@index=@pageindex
end
GO


