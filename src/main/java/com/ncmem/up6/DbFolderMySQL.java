package com.ncmem.up6;

import com.ncmem.up6.sql.SqlExec;

public class DbFolderMySQL extends DbFolder
{
	
	/**
	 * 更新子文件路径
	 * @param pathRelOld
	 * @param pathRelNew
	 */
	public void updatePathRel(String pathRelOld,String pathRelNew)
	{
        //更新子文件路径
        String sql = String.format("update up6_folders set f_pathRel=REPLACE(f_pathRel,'%s/','%s/') where locate('%s/',f_pathRel)>0",
            pathRelOld,
            pathRelNew,
            pathRelOld
            );
        
        SqlExec se = new SqlExec();
        se.exec(sql);		
	}
}
