package com.gopay.web.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;

import javax.crypto.Cipher;

import org.bouncycastle.openssl.PEMReader;

import com.gopay.common.common.txn.TransUtil;


public class RSAUtils {
	
	private static final String RSA = "RSA/ECB/PKCS1Padding";
	
	private static final String PROVIDER = "BC";
	
	private static final String FILE_NAME = "classpath:privatekey.pem";
	
	static{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	public static Object string2Key(String pemstr){
		
		PEMReader reader=new PEMReader(new StringReader(pemstr));
		Object pkio=null;
		try {
			pkio = reader.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pkio;
	}
	
	public static byte[] readFile(String filename) throws Exception{
		InputStream is = new FileInputStream(filename);
		byte[] ret = new byte[is.available()];
		
		is.read(ret);
		
		return ret;
	}
	
	/**
	 * ����
	 * 
	 * @param key
	 *            ���ܵ���Կ
	 * @param raw
	 *            �Ѿ����ܵ�����
	 * @return ���ܺ������
	 * @throws Exception
	 */
	public static byte[] decrypt(Key key, byte[] raw) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance(RSA,  new org.bouncycastle.jce.provider.BouncyCastleProvider());
			cipher.init(cipher.DECRYPT_MODE, key);
			int blockSize = cipher.getBlockSize();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
			int j = 0;

			while (raw.length - j * blockSize > 0) {
				bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
				j++;
			}
			return bout.toByteArray();
		} catch (Exception e) {	
			throw new Exception(e.getMessage());
		}
	}
	
	/**
	 * ����
	 * 
	 * @param key
	 *            ���ܵ���Կ
	 * @param data
	 *            �����ܵ���������
	 * @return ���ܺ������
	 * @throws Exception
	 */
	public static byte[] encrypt(Key key, byte[] data) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance(RSA,new org.bouncycastle.jce.provider.BouncyCastleProvider());
			cipher.init(Cipher.ENCRYPT_MODE, key);
			int blockSize = cipher.getBlockSize();// ��ü��ܿ��С���磺����ǰ����Ϊ128��byte����key_size=1024
													// ���ܿ��СΪ127
													// byte,���ܺ�Ϊ128��byte;��˹���2�����ܿ飬��һ��127
													// byte�ڶ���Ϊ1��byte
			int outputSize = cipher.getOutputSize(data.length);// ��ü��ܿ���ܺ���С
			int leavedSize = data.length % blockSize;
			int blocksSize = leavedSize != 0 ? data.length / blockSize + 1
					: data.length / blockSize;
			byte[] raw = new byte[outputSize * blocksSize];
			int i = 0;
			while (data.length - i * blockSize > 0) {
				if (data.length - i * blockSize > blockSize)
					cipher.doFinal(data, i * blockSize, blockSize, raw, i
							* outputSize);
				else
					cipher.doFinal(data, i * blockSize, data.length - i
							* blockSize, raw, i * outputSize);
				// ������doUpdate���������ã��鿴Դ�������ÿ��doUpdate��û��ʲôʵ�ʶ������˰�byte[]�ŵ�ByteArrayOutputStream�У������doFinal��ʱ��Ž����е�byte[]���м��ܣ����ǵ��˴�ʱ���ܿ��С�ܿ����Ѿ�������OutputSize����ֻ����dofinal������

				i++;
			}
			return raw;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	/**
	 * 16�����ַ���ת����byte����
	 * 
	 * @param hex
	 * @return
	 */
//	public static byte[] hex2byte(String hex){
//		int len = (hex.length() / 2);
//		byte[] result = new byte[len];
//		char[] achar = hex.toLowerCase().toCharArray();
//		for(int i=0;i<len;i++){
//			int pos = i*2;
//			result[i] = (byte)(toByte(achar[pos])<<4 |toByte(achar[pos+1]));
//		}
//		
//		return result;
//	}
//	private static int toByte(char c) {
//		byte b = (byte)"0123456789abcdef".indexOf(c);
//		return b;
//	}
	public static byte[] hex2byte(String hexStr){
        byte[] bts = new byte[hexStr.length() / 2];
        for (int i = 0,j=0; j < bts.length;j++ ) {
           bts[j] = (byte) Integer.parseInt(hexStr.substring(i, i+2), 16);
           i+=2;
        }
        return bts;
    }

	
	/**
	 * byte����ת����16�����ַ���
	 * 
	 * @param b
	 */
	public static String byte2hex(byte[] b) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			result.append(hex.toLowerCase());
		}
		
		return result.toString();
	}
	
	
	/**
	 * ��RSA���ܵ����Ľ��н���
	 * 
	 * @param hexStr
	 * @return
	 * @throws Exception
	 */
	public static String decryptRSA(String hexStr) throws Exception{
		PrivateKey privateKey = null;
		String ret = null;
		
		byte[] data = readFile(TransUtil
				.getCanonicalFilePath(FILE_NAME));
		String str_data = new String(data);
		Object obj = string2Key(str_data);
		
		if(obj instanceof KeyPair){
			privateKey = ((KeyPair) obj).getPrivate();
			byte[] src_data = decrypt(privateKey, hex2byte(hexStr));
			ret = new String(src_data);
		}
		
		return ret;
	}
	
	public static  void  main(String args[]){
		
		String strPwd="503E1AA86B9958D8C5C1B0D344C8AC1BE34FED557A6F83B52A47AFFC0AED842D626E25B2300BD22D7E13DFB53B70AC0F4655A2A8C14D24440D8929D0C33332677FD0D624A9BEEE3995A671603E0BB1787A0D90824A840F178F251A631F21918146F0C6C1D108EF3478F7BF37A946383C8ED761199FBB193BD4BF0B5D6847CC65";
		try {
			System.out.println(decryptRSA(strPwd));
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		
	}
}
