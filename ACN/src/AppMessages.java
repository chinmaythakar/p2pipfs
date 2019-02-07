import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMessages implements Runnable, Serializable{
	p2pNode node = new p2pNode();
	
	@Override
	public void run() {
 		try
		{
			ExecutorService executor = Executors.newFixedThreadPool(500);
			ServerSocket ss = new ServerSocket(node.port);
			Message msg = new Message();
			while(true) {
		        			Socket sc = ss.accept();
		                    InputStream is = sc.getInputStream();
		                    ObjectInputStream ois = new ObjectInputStream(is);
		                    msg = (Message) ois.readObject();
		        			switch (msg.messageId) 
		        			{
		        			    case 0:{
        		        			        HashMap<String,ArrayList<String>> missing = new HashMap<String,ArrayList<String>>();
                                            for (Map.Entry<String, ArrayList<String>> entry : msg.CTT.entrySet()) 
                                            {
                                                String key = entry.getKey();
                                                ArrayList<String> value = entry.getValue();
                                                if(!(node.contentTracker.containsKey(key)))
                                                {
                                                    missing.put(key, value);
                                                }
                                            }
                                            if(missing.isEmpty())
                                                break;
                                            for(Map.Entry<String, ArrayList<String>> entry : missing.entrySet())
                                            {
                                                String key = entry.getKey();
                                                ArrayList<String> value = entry.getValue();
                                                node.contentTracker.put(key, value);
                                                Message msg1 = new Message();
                                                //msg1.CTT.put(key, value);
                                                msg1.hash = key;
                                                msg1.arr = value;
                                                msg1.messageId = 2;
                                                Socket sc1;
                                                OutputStream os;
                                                ObjectOutputStream oos;
                                                for(Map.Entry<String, Integer> e1 : node.peerTable.entrySet())
                                                {
                                                    String key1 = e1.getKey();
                                                    int value1 = e1.getValue();
                                                    if(key1.equals(msg.IPaddress))
                                                        continue;
                                                    sc1 = new Socket(key1, value1);
                                                    os = sc1.getOutputStream();
                                                    oos = new ObjectOutputStream(os);
                                                    oos.writeObject(msg1);
                                                }
                                                
                                            }
                                            break;
		        			           }
		        			    
		        			    case 1: {
/*                                          1/peer received
                                          Add entry into peer table with (hostname/ip, port) from message
                                          Send your CTT to (hostname,port) from message
                                          Compare your CTT with received CTT and add missing content in suitable data structure(say missing)
                                          if no entry in missing exit
                                          For each entry e in missing
                                              add e to your CTT
                                              send a publish message to all neighbors except incoming neighbor
                                          end   
*/                                            node.peerTable.put(msg.IPaddress, msg.port);

                                              Message msg2 = new Message();
                                              msg2.messageId = 0;
                                              msg2.IPaddress = node.ipAdd;
                                              msg2.port = node.port;
                                              msg2.CTT = node.contentTracker;
                                              Socket sc2 = new Socket(msg.IPaddress, msg.port);
                                              OutputStream os2 = sc2.getOutputStream();
                                              ObjectOutputStream oos2 = new ObjectOutputStream(os2);
                                              oos2.writeObject(msg2);
                                              
                                              HashMap<String,ArrayList<String>> missing = new HashMap<String,ArrayList<String>>();
                                              for (Map.Entry<String, ArrayList<String>> entry : msg.CTT.entrySet()) 
                                              {
                                                  String key = entry.getKey();
                                                  ArrayList<String> value = entry.getValue();
                                                  if(!(node.contentTracker.containsKey(key)))
                                                  {
                                                      missing.put(key, value);
                                                  }
                                              }
                                              if(missing.isEmpty())
                                                  break;
                                              for(Map.Entry<String, ArrayList<String>> entry : missing.entrySet())
                                              {
                                                  String key = entry.getKey();
                                                  ArrayList<String> value = entry.getValue();
                                                  node.contentTracker.put(key, value);
                                                  Message msg1 = new Message();
                                                  //msg1.CTT.put(key, value);
                                                  msg1.hash = key;
                                                  msg1.arr = value;
                                                  msg1.messageId = 2;
                                                  Socket sc1;
                                                  OutputStream os;
                                                  ObjectOutputStream oos;
                                                  for(Map.Entry<String, Integer> e1 : node.peerTable.entrySet())
                                                  {
                                                      String key1 = e1.getKey();
                                                      int value1 = e1.getValue();
                                                      if(key1.equals(msg.IPaddress))
                                                          continue;
                                                      sc1 = new Socket(key1, value1);
                                                      os = sc1.getOutputStream();
                                                      oos = new ObjectOutputStream(os);
                                                      oos.writeObject(msg1);
                                                  }
                                                  
                                              }
                                              break;
                                        }
		        			    
                                case 2: {
                                            /*2/publish received
                                            if (check using (hash and ip/hostname) from incoming message whether content present in own CCT == false)
                                                  Add entry into own CTT using (hash, content_info) from message
                                                  send a publish message (2,hash,content_info) to all neighbors except incoming neighbor
                                            else exit     
                                            */
                                            if(!(node.contentTracker.containsKey(msg.hash)))
                                            {
                                                node.contentTracker.put(msg.hash, msg.arr);
                                                Socket sc1;
                                                OutputStream os;
                                                ObjectOutputStream oos;
                                                for(Map.Entry<String, Integer> e1 : node.peerTable.entrySet())
                                                {
                                                    String key1 = e1.getKey();
                                                    int value1 = e1.getValue();
                                                    String [] str = msg.hash.split("~");
                                                    if(key1.equals(str[1]))
                                                        continue;
                                                    msg.IPaddress = node.ipAdd;
                                                    sc1 = new Socket(key1, value1);
                                                    os = sc1.getOutputStream();
                                                    oos = new ObjectOutputStream(os);
                                                    oos.writeObject(msg);
                                                }
                                            }
                                            break;
                                        }
                                
                                case 3: {
                                            /*      3/unpublish received
                                            if (check using (hash and ip/hostname) from incoming message whether content present in own CCT == true)
                                                remove content from own table
                                                send an unpublish message (3,hash) to all neighbors except incoming neighbor
                                            else exit     */
                                            if(node.contentTracker.containsKey(msg.hash))
                                            {
                                                node.contentTracker.remove(msg.hash);
                                                Socket sc1;
                                                OutputStream os;
                                                ObjectOutputStream oos;
                                                for(Map.Entry<String, Integer> e1 : node.peerTable.entrySet())
                                                {
                                                    String key1 = e1.getKey();
                                                    int value1 = e1.getValue();
                                                    String [] str = msg.hash.split("~");
                                                    if(key1.equals(str[1]))
                                                        continue;
                                                    msg.IPaddress = node.ipAdd;
                                                    sc1 = new Socket(key1, value1);
                                                    os = sc1.getOutputStream();
                                                    oos = new ObjectOutputStream(os);
                                                    oos.writeObject(msg);
                                                }
                                            }
                                            break;
                                        }
                                case 4: {
                                            //return the file published on your node to sender(msg.ipaddress, msg.port)
                                            String hash = msg.hash;
                                            File file = new File(node.hashToFile.get(hash));
                                            FileInputStream fis = new FileInputStream(file);
                                            byte[] data = new byte[(int)file.length()];
                                            fis.read(data);

                                            Socket sc1 = new Socket(msg.IPaddress, msg.port);
                                            OutputStream os = sc1.getOutputStream();
                                            ObjectOutputStream oos= new ObjectOutputStream(os);
                                            oos.writeObject(data);
                                            break;
                                        }
                                default: break;
                            }
		        		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
