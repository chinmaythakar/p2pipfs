import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable{
    public int messageId;
    public String IPaddress;
    public int port;
    public HashMap<String,ArrayList<String>> CTT = new HashMap<String,ArrayList<String>>();
    public String hash;
    public ArrayList<String> arr = new ArrayList<String>(); 
}
