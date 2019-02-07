import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class clientGateway implements Runnable, Serializable{
    
    @Override
    public void run() 
    {
        try{
                int port = 8080;
                ServerSocket ss = new ServerSocket(port);
                ExecutorService executor = Executors.newFixedThreadPool(500);
                while(true) 
                {
                  Socket sc = ss.accept();
                  BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));                         
                  String line = br.readLine(); 
                  String [] url;
                  url = line.split(" ");
                  String hashVal = url[1];
                  //System.out.printf(hashVal);
                  hashVal = hashVal.substring(1);
                  p2pNode node = new p2pNode();               
                  if(node.contentTracker.containsKey(hashVal+"~"+node.ipAdd))
                  {
                      File file = new File(node.hashToFile.get(hashVal));
                      PrintWriter out = new PrintWriter(sc.getOutputStream());
                      OutputStream outS = sc.getOutputStream();
                      DataOutputStream binout;
                      if((node.contentTracker.get(hashVal+"~"+node.ipAdd)).get(0).equals("html"))
                      {   
                          //System.out.println("you're in");
                          String filepath = node.hashToFile.get(hashVal);
                          BufferedReader br1 = new BufferedReader(new FileReader(node.hashToFile.get(hashVal)));
                          String line1 = null;
                          String content = "";
                          while ((line1 = br1.readLine()) != null) 
                              {
                                  content += line1;
                              }
                          //System.out.println("content : "+content);
                          out.println("HTTP/1.1 200 OK");
                          out.println("Content-Type: text/html");
                          out.println("\r\n");
                          out.print(content);    
                          out.close();
                      }
                      else{
                              FileInputStream fis = new FileInputStream(file);
                              byte[] data = new byte[(int)file.length()];
                              fis.read(data);
                              fis.close();
                              
                              binout = new DataOutputStream(outS);
                              binout.writeBytes("HTTP/1.0 200 OK\r\n");
                              binout.writeBytes("Content-Type: image/png\r\n");
                              binout.writeBytes("Content-Length: "+data.length);
                              binout.writeBytes("\r\n\r\n");
                              binout.write(data);
                              out.close();
                              binout.close();
                          }
                  }
                  else 
                  {
                      int flag = 0;
                      for(Map.Entry<String, ArrayList<String>> entry : node.contentTracker.entrySet())
                      {
                          String key = entry.getKey();
                          ArrayList<String> value = entry.getValue();
                          String str[] = key.split("~");
                          String hashval = str[0];
                          if(hashval.equals(hashVal))
                          {
                              Socket sc1 = new Socket(str[1],Integer.parseInt(value.get(2)));
                              OutputStream os = sc1.getOutputStream();
                              ObjectOutputStream oos = new ObjectOutputStream(os);
                              Message msg = new Message();
                              msg.messageId = 4;
                              msg.hash = hashVal;
                              msg.port = 2222;
                              msg.IPaddress = node.ipAdd;
                              ServerSocket ss1 = new ServerSocket(msg.port);
                              oos.writeObject(msg);
                              Socket sock = ss1.accept();
                              InputStream is = sock.getInputStream();
                              ObjectInputStream ois = new ObjectInputStream(is);
                              byte[] content = (byte[]) ois.readObject();
                              PrintWriter out = new PrintWriter(sc.getOutputStream());
                              OutputStream outS = sc.getOutputStream();
                              DataOutputStream binout;
                              if(value.get(0).equals("html"))
                              {   
//                                  BufferedReader br1 = new BufferedReader(new FileReader(file));
//                                  String line1 = null;
//                                  String content = "";
//                                  while ((line1 = br1.readLine()) != null) {
//                                      content += line1;}
                                  out.println("HTTP/1.1 200 OK");
                                  out.println("Content-Type: text/html");
                                  out.println("\r\n");    
                                  out.print(new String(content));
                                  out.close();
                              }
                              else{
                                  binout = new DataOutputStream(outS);
                                  binout.writeBytes("HTTP/1.0 200 OK\r\n");
                                  binout.writeBytes("Content-Type: image/png\r\n");
                                  binout.writeBytes("Content-Length: "+content.length);
                                  binout.writeBytes("\r\n\r\n");
                                  binout.write(content);
                                  out.close();
                                  binout.close();
                                  }
                              flag = 1;
                              ss1.close();
                              break;
                          }
                      }
                      if(flag == 0)
                      {
                         
                              PrintWriter out = new PrintWriter(sc.getOutputStream());    
                              out.println("HTTP/1.1 404 Not Found");
                              out.println("Content-Type: text/html");
                              out.println("\r\n");
                              out.print("<html><body><h1>Resource not found</h1></body></html>");
                              out.close();
                      }
                  }
                }
            }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    public int CreateRandom()
    {
        Random r = new Random();
        int Low = 2000;
        int High = 5000;
        return r.nextInt(High-Low) + Low;
    }
    

}
