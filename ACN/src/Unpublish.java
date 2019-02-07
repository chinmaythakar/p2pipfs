import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class Unpublish implements Runnable, Serializable{
	
	//initializations 
	p2pNode node = new p2pNode();
	String hash;
	
	//constructor
	public Unpublish(String hashval) {
		this.hash=hashval;
	}
	
	@Override
	public void run() {
		try{
		// Write code to send upublish message (application message 3)
		//Remove from current CCT
	    node.contentTracker.remove(hash+"~"+node.ipAdd);
	    node.hashToFile.remove(hash);
        //send a bundled message with (3,hash) to all in peer table
		Message msg = new Message();
        msg.messageId = 3;
        /*ArrayList<String> arr= new ArrayList<String>();
        arr.add(node.ipAdd);
        msg.CTT.put(hash, arr);*/
        msg.hash = hash+"~"+node.ipAdd;
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
