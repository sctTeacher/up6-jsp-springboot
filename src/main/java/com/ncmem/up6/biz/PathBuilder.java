package com.ncmem.up6.biz;

import java.io.File;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.ncmem.up6.ConfigReader;
import com.ncmem.up6.PathTool;
import com.ncmem.up6.model.FileInf;
import org.springframework.core.io.ClassPathResource;


public class PathBuilder {
	
	public PathBuilder(){}
	
	/**
	 * 获取上传路径
	 * 格式：
	 * 	resources/upload
	 * @return
	 * @throws IOException
	 */
	public String getRoot() throws IOException{
		String root = "";//

		ConfigReader cr = new ConfigReader();
		JSONObject jo =  cr.module("path");
		String pathSvr = jo.getString("upload-folder");
		pathSvr = pathSvr.replace("{root}", root);
		pathSvr = pathSvr.replaceAll("\\\\", "/");
		return pathSvr;
	}
	public String genFolder(FileInf fd) throws IOException{return "";}
	public String genFile(int uid,FileInf f) throws IOException{return "";}
	public String genFile(int uid,String md5,String nameLoc)throws IOException{return "";}
	/**
	 * 相对路径转换成绝对路径
	 * 格式：
	 * 	/2021/05/28/guid/nameLoc => d:/upload/2021/05/28/guid/nameLoc
	 * @return
	 * @throws IOException
	 */
	public String relToAbs(String path) throws IOException
	{
		String root = this.getRoot();
		root = root.replaceAll("\\\\", "/");
		path = path.replaceAll("\\\\", "/");
		if(path.startsWith("/"))
		{
			path = PathTool.combine(root, path);
		}
		return path;
	}
	/**
	 * 将路径转换成相对路径
	 * @return
	 * @throws IOException
	 */
	public String absToRel(String path) throws IOException
	{
		String root = this.getRoot().replaceAll("\\\\", "/");
		path = path.replaceAll("\\\\", "/");
		path = path.replaceAll(root, "");
		return path;
	}
}
