package com.ncmem.up6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.ncmem.down2.biz.DnFile;
import com.ncmem.down2.biz.DnFileDM;
import com.ncmem.down2.biz.DnFileOdbc;
import com.ncmem.down2.biz.DnFileMySQL;
import com.ncmem.down2.biz.DnFileOracle;
import com.ncmem.down2.biz.DnFileSQL;

/**
 * Created by Administrator on 2021/1/6.
 */
public class DBConfig {
    public String m_db="sql";//sql,oracle,mysql

    String driver = "";
    String url = "";
    String name = "";
    String pass = "";
    public Boolean m_isOdbc=false;

    public DBConfig() {
        ConfigReader cr = new ConfigReader();
        this.m_db = cr.readString("$.database.connection.type");
        JSONObject o = cr.read("$.database.connection."+this.m_db);
        this.driver = o.getString("driver");
        this.url = o.getString("url");
        this.name = o.getString("name");
        this.pass = o.getString("pass");
        this.m_isOdbc = StringUtils.equals(this.m_db,"kingbase");
    }

    public DBFile db() {
        if( StringUtils.equals(this.m_db, "sql") ) return new DBFileSQL();
        else if( StringUtils.equals(this.m_db, "mysql") ) return new DBFileMySQL();
        else if( StringUtils.equals(this.m_db, "oracle") ) return new DBFileOracle();
        else if( StringUtils.equals(this.m_db, "dmdb") ) return new DBFileDM();
        else if( StringUtils.equals(this.m_db, "kingbase") ) return new DBFileOdbc();
        else return new DBFile();
    }

    public DbFolder folder()
    {
        if( StringUtils.equals(this.m_db, "sql") ) return new DbFolder();
        else if( StringUtils.equals(this.m_db, "mysql") ) return new DbFolderMySQL();
        else if( StringUtils.equals(this.m_db, "oracle") ) return new DbFolderOracle();
        else if( StringUtils.equals(this.m_db, "dmdb") ) return new DbFolderDM();
        else return new DbFolder();
    }

    public DnFile down() {
        if( StringUtils.equals(this.m_db, "sql") ) return new DnFileSQL();
        else if( StringUtils.equals(this.m_db, "mysql") ) return new DnFileMySQL();
        else if( StringUtils.equals(this.m_db, "oracle") ) return new DnFileOracle();
        else if( StringUtils.equals(this.m_db, "dmdb") ) return new DnFileDM();
        else if( StringUtils.equals(this.m_db, "kingbase") ) return new DnFileOdbc();
        else return new DnFile();
    }

    public Connection getCon()
    {
        Connection con = null;

        try
        {
            Class.forName(this.driver).newInstance();//加载驱动。
            if (StringUtils.equals(this.m_db, "mysql")) con = DriverManager.getConnection(this.url);
            else con = DriverManager.getConnection(this.url,this.name,this.pass);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return con;
    }
}
