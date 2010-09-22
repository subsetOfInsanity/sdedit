// Copyright (c) 2006 - 2008, Markus Strauch.
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice, 
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice, 
// this list of conditions and the following disclaimer in the documentation 
// and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
// THE POSSIBILITY OF SUCH DAMAGE.// Copyright (c) 2006 - 2008, Markus Strauch.

package net.sf.sdedit.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for accessing the Windows registry by means of the
 * <tt>reg.exe</tt> tool.
 * 
 * @author Markus Strauch
 *
 */
public final class WindowsRegistry extends Thread {
	
	private volatile String result;
	
	private String category;
	
	private String key;
	
	private WindowsRegistry (String category, String key) {
		this.category = category;
		this.key = key;
	}
	
	/**
	 * @see java.lang.Thread#run()
	 */
	public void run () {
		try {
			result = fetchValue ();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts a process on a newly created thread that uses the reg.exe tool
	 * in order to compute the value for the given category and key. If the
	 * tool does not perform the task in at most two seconds, or if the
	 * tool does not find the key, we return <tt>null</tt>
	 * 
	 * @param category the registry category in which to lookup the key
	 * @param key the name of the key
	 * @return the value for the key
	 */
	public static String getValue (String category, String key) {
		WindowsRegistry registry = new WindowsRegistry (category, key);
		registry.start();
		try {
			registry.join(2000);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
		return registry.result;
	}
	
    private String fetchValue ()
    throws IOException
    {
    	category = category.replace('/', '\\');
    	Process proc = Runtime.getRuntime().exec(
    			"reg QUERY \"" + category + "\""); 
    	InputStream stream = new BufferedInputStream(proc.getInputStream());
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    		String line = reader.readLine();
    		while (line != null) {
    			String [] parts = line.split("\t");
    			if (parts.length == 3 && parts [0].trim().equals(key)) {
    				return parts[2].trim();
    			}
    			line = reader.readLine();
    		}
    	} finally {
    		stream.close();
    	}
    	return null;
    }
}
