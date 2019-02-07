import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.Socket;

public class Publish implements Runnable, Serializable{
	//initializations
	File filename;
	String hash;
	p2pNode node = new p2pNode();
	
	//constructor
	public Publish(File filename) throws Exception
	{
		this.filename = filename;
		this.hash = calcSHA1(filename);
	}
	
	//calculate hash from file
	private static String calcSHA1(File file) throws Exception 
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		try (InputStream input = new FileInputStream(file)) 
		{
			byte[] buffer = new byte[8192];
			int len = input.read(buffer);
			while (len != -1) 
			{
				sha1.update(buffer, 0, len);
				len = input.read(buffer);
			}
			return new HexBinaryAdapter().marshal(sha1.digest());
		}
	}
	
	//find file type
	public static String getFileExtension(String fullName) {
	    String fileName = fullName;
	    int dotIndex = fileName.lastIndexOf('.');
	    //return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	    if(fileName.lastIndexOf(".")!=-1 && fileName.lastIndexOf(".")!=0)
	    {
	       // System.out.println(fileName.substring(fileName.lastIndexOf(".")+1));
	        return fileName.substring(fileName.lastIndexOf(".")+1);
	    }
	    else return "";
	}
	

	public void run() 
	{
		// TODO Auto-generated method stub
		
	    
	    try
	    {
	        ArrayList<String> content_info = new ArrayList<String>();
	        String test = getFileExtension(filename.getName());
	        content_info.add(test);
    		content_info.add(Long.toString(filename.length()));
    		//content_info.add(node.ipAdd);
    		content_info.add(Integer.toString(node.port));
    		node.contentTracker.put(hash+"~"+node.ipAdd, content_info);
    		node.hashToFile.put(hash, filename.getAbsolutePath());
    		
    		// Write code to send publish message (application message 2)        
            //send a bundled message with (2,hash,content_info, ownhostname) to all in peer table
    		Message msg = new Message();
    		//msg.CTT.put(hash, content_info);
    		msg.hash = hash+"~"+node.ipAdd;
    		msg.IPaddress = node.ipAdd;
    		msg.arr = content_info;
    		msg.messageId = 2;
    		Socket sc;
            OutputStream os;
            ObjectOutputStream oos;
    		for (Map.Entry<String,Integer> entry : node.peerTable.entrySet()) 
            {
    		   String key = entry.getKey();
    		   int value = entry.getValue();
    		   sc = new Socket(key, value);
    		   os = sc.getOutputStream();
    		   oos = new ObjectOutputStream(os);
    		   oos.writeObject(msg);
            }
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	}

}
