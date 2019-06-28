package Main;

import java.net.Socket;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TouchControl {
    private SocketClient socket;
    @FXML private Label movement;
    public TouchControl(SocketClient socket){
        System.out.println("Touch Control started.");
        //this.socket = new SocketClient("192.168.0.118",50051);
        System.out.println("Socket connection established.");
        this.socket = socket;
    }
    public void sendData(String data){
        //this.movement.setText(data);
        this.socket.sendData(data);
        
    }
    
    public void stopTouching(){
        if(socket.isOpen()){
            socket.closeConnection();
        }
    }
}
