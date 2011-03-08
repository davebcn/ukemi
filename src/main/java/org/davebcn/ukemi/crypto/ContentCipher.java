package org.davebcn.ukemi.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.log4j.Logger;

/**
 * Performs the ciphering/deciphering of the content given
 * Recomended cipher PBEWithMD5AndDES
 * @author dave
 *
 */
public class ContentCipher {

	private String algorithm;
	private String password;
	
	
	private Logger logger = Logger.getLogger(ContentCipher.class);
	
	public ContentCipher(String algorithm, String password) throws NoSuchAlgorithmException, NoSuchPaddingException{
		this.logger.info("Starting content cipher with algorithm " + algorithm);
		this.algorithm = algorithm;
		this.password = password;
	}
	
	public byte[] encipher(byte[] toBeEnciphered) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException{
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, getKey(), getParameters());
		return cipher.doFinal(toBeEnciphered);
	}
	
	public byte[] decipher(byte[] toBeEnciphered) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException{
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, getKey(), getParameters());
		return cipher.doFinal(toBeEnciphered);
	}
	
	private PBEParameterSpec getParameters(){
		byte[] salt = new byte[] { 0x7d, 0x60, 0x43, 0x5f, 0x02, (byte) 0xe9, (byte) 0xe0, (byte) 0xae };
		int iterationCount = 2048;
		PBEParameterSpec spec = new PBEParameterSpec(salt, iterationCount);
		
		return spec;
	}
	
	private Key getKey() throws NoSuchAlgorithmException, InvalidKeySpecException{
	    PBEKeySpec pbeSpec = new PBEKeySpec(this.password.toCharArray());
	    SecretKeyFactory keyFact = SecretKeyFactory.getInstance(algorithm);

	    Key sKey = keyFact.generateSecret(pbeSpec);
		
	    return sKey;
	}
}
