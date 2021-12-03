package com.ncmem.up6;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import com.ncmem.up6.model.FileInf;

/**
 * 
 * @author zysoft
 * 用法：
 *
 */
public class CryptoTool
{
	private String key = "2C4DD1CC9KAX4TA9";
	private String iv = "2C4DD1CC9KAX4TA9";
	 //算法名称
	private String KEY_ALGORITHM = "AES";
	 //加密算法，填充方式
	 private String algorithm = "AES/CBC/NoPadding";

	
	public CryptoTool()
	{		
	}	
	 
	public String encrypt(String data) throws Exception 
	{
	    try 
	    {
	    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	        Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
	        int blockSize = cipher.getBlockSize();

	        //ZeroPadding
	        byte[] dataBytes = data.getBytes();
	        int plaintextLength = dataBytes.length;
	        if (plaintextLength % blockSize != 0) {
	            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
	        }
	        
	        byte[] plaintext = new byte[plaintextLength];
	        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
	        
	        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
	        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
	
	        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
	        byte[] encrypted = cipher.doFinal(plaintext);
	
	        String str = new sun.misc.BASE64Encoder().encode(encrypted);
	        return str.replaceAll("\n", "").replaceAll("\r", "");
	
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public String encrypt(byte[] dataBytes) throws Exception 
	{
	    try 
	    {
	    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	        Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
	        int blockSize = cipher.getBlockSize();

	        //ZeroPadding
	        //byte[] dataBytes = data.getBytes();
	        int plaintextLength = dataBytes.length;
	        if (plaintextLength % blockSize != 0) {
	            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
	        }
	        
	        byte[] plaintext = new byte[plaintextLength];
	        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
	        
	        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
	        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
	
	        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
	        byte[] encrypted = cipher.doFinal(plaintext);
	
	        return new sun.misc.BASE64Encoder().encode(encrypted);
	
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

    public String decrypt(String data) throws Exception 
    {
        try
        {	            
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(data);
            
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
 
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }

	public String decrypt(InputStream data)
	{
		try
		{
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(data);

			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original);
			return originalString;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
    
    public String decrypt(String data, String encode) throws Exception 
    {
        try
        {	            
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(data);
            
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
 
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original,encode);
            return originalString.trim();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }
	 
	public byte[] encrypt(MultipartFile data) throws Exception
	{
	    try 
	    {
	    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	        Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
	        int blockSize = cipher.getBlockSize();

			InputStream stm = data.getInputStream();
			byte[] dataBytes = new byte[(int)data.getSize()];			
			stm.read(dataBytes);
			stm.close();
	        //ZeroPadding
	        //byte[] dataBytes = data.getInputStream().
	        int plaintextLength = dataBytes.length;
	        if (plaintextLength % blockSize != 0) {
	            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
	        }
	        
	        byte[] plaintext = new byte[plaintextLength];
	        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
	        
	        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
	        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
	
	        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);
			return encrypted;
	
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	 
	public ByteArrayOutputStream decrypt(MultipartFile block,int lenOri) throws Exception
	{
        try
        {	            
			InputStream stm = block.getInputStream();
			byte[] dataBytes = new byte[(int)block.getSize()];
			stm.read(dataBytes);
			stm.close();
            
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance(this.algorithm,"BC");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), this.KEY_ALGORITHM);
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
 
			byte[] out = cipher.doFinal(dataBytes);
			ByteArrayOutputStream ost = new ByteArrayOutputStream();
			ost.write(out,0,lenOri);
			return ost;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
	}
	
	public String token(FileInf f,String action)
	{
		String str = f.id + f.nameLoc + action;
		if(action == "block") str = f.id + f.pathSvr + action;
		str = Md5Tool.getMD5(str);
		try {
			str = this.encrypt(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
}