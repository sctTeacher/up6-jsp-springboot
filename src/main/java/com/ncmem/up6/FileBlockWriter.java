package com.ncmem.up6;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *文件续传类，负责将文件块写入硬盘中
 */
public class FileBlockWriter {
	
	public FileBlockWriter()
	{
	}
	
	public void CreateFile(String pathSvr,long lenLoc)
	{
		try 
		{
			File ps = new File(pathSvr);
			PathTool.createDirectory(ps.getParent());
			
		    RandomAccessFile raf = new RandomAccessFile(pathSvr, "rw");
		    raf.setLength(lenLoc);//fix:以原始大小创建文件
		    raf.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void write(long offset,String pathSvr,MultipartFile block)
	{		
		//根据索引将文件块数据写入到在服务端文件中
		try 
		{
			InputStream stream = block.getInputStream();			
			byte[] data = new byte[(int)block.getSize()];
			stream.read(data);
			stream.close();			
			
			//bug:在部分服务器中会出现错误：(另一个程序正在使用此文件，进程无法访问。)
			RandomAccessFile raf = new RandomAccessFile(pathSvr,"rw");
			//定位文件位置
			raf.seek(offset);
			raf.write(data);
			raf.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(long offset,String pathSvr,ByteArrayOutputStream ostm)
	{		
		//根据索引将文件块数据写入到在服务端文件中
		try 
		{
			byte[] data = ostm.toByteArray();
			
			//bug:在部分服务器中会出现错误：(另一个程序正在使用此文件，进程无法访问。)
			RandomAccessFile raf = new RandomAccessFile(pathSvr,"rw");
			//定位文件位置
			raf.seek(offset);
			raf.write(data);
			raf.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}