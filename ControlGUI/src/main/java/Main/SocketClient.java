
package Main;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.*; 
import java.io.*; 

public class SocketClient {
    // initialize socket and input output streams 
    private Socket socket = null; 
    private String input = ""; 
    private DataOutputStream out = null; 
    private String data;
    private String address;
    private int port;
    
    
    // constructor to put ip address and port 
    public SocketClient(String address, int port) 
    { 
        // establish a connection 
        this.address = address;
        this.port = port;
        testConn();
    }
    public boolean setData(String address, int port){
        this.address = address;
        this.port = port;
        return testConn();
    }
    public boolean testConn(){
        try
        { 
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 5000);
            boolean conn = socket.getInetAddress().isReachable(5000);
            if(conn){
                out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("INIT");
                out.close();
                return conn;
            }else{return false;}
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
            return false;
        } 
    }
    public boolean sendData(String input){
        try
        {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 5000);
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("{'client': 'user', 'data': ['{\"direction\":'"+input+"'}']}");
            out.close();
            System.out.println("Sent "+ input +" to the socket server");
        } 
        catch(IOException i) 
        { 
            System.out.println(i);
        } 
        return true;
    }
    
    public boolean isOpen(){
        return socket.isConnected();
    }
    
    public void closeConnection(){
        try{
            this.sendData("Closing connection...");
            //out.close();
            socket.close();
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
    }
}
