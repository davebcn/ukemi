package org.davebcn.ukemi;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;


public interface Content extends Serializable, Cloneable{

	public byte[] asArray();
	public InputStream asStream();
	public Date getLastModified();

	
	public Content clone();
	
}
