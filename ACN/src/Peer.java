import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class Peer implements Runnable, Serializable{
	
	String hostname;
	int port;
	p2pNode node = new p2pNode();
	public Peer(String hostname, String port)
	{
		this.hostname = hostname;
		this.port = Integer.parseInt(port);
	}
	@Override
	public void run() {
		try
		{
		    node.peerTable.put(hostname, port);
		    //send a bundled message with (1, ownipaddress/hostname,own port, own CTT) to hostname, port
		    p2pNode node = new p2pNode();
		    Message msg = new Message();
		    msg.messageId = 1;
		    msg.IPaddress = node.ipAdd;
		    msg.port = node.port;
		    msg.CTT = node.contentTracker;
		    Socket sc = new Socket(hostname, port);
		    OutputStream os = sc.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(msg);
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
		
	}
	
}
