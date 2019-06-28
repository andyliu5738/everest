                         package Main;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class controller {

    @FXML private Button hand;
    @FXML private Button voice;
    @FXML private Button touch;
    @FXML private Button main;
    @FXML private Button frameStart;
    @FXML private Button voiceStart;
    @FXML private Button forward;
    @FXML private Button backward;
    @FXML private Button left;
    @FXML private Button right;
    @FXML private Button stop;
    @FXML private Button touchStopStart;
    @FXML private Label movingInfo;
    @FXML private TextField address;
    @FXML private TextField port;
    @FXML private Pane mainPane;
    @FXML private Pane handPane;
    @FXML private Pane touchPane;
    @FXML private Pane voicePane;
    @FXML private ImageView cameraFrame;
    private CameraDetection camera;
    private GoogleSpeechTest voiceObject;
    private TouchControl touchControl;
    private SocketClient socket = new SocketClient("::1",1234);
    int currentPane = 0;
    boolean cameraOn = false;
    boolean voiceRecording = false;
    boolean touchOn = false;
    private Thread voiceRecord, cam;
    String path;
    
    public void initialize() {
        System.out.println("Starting...");
        path = controller.class.getResource("/webcam.png").getPath();
        path = (path.toCharArray()[0] == '/') ? path.substring(1) : path;
    }
    
    //
    public void buttonClick(ActionEvent e) {
        //Very ugly method of converting button variable name into an integer
        Pane[] panes = {mainPane, handPane, touchPane, voicePane};
        System.out.println(e.toString());
        int buttonId = Arrays.asList("main", "hand", "touch", "voice").indexOf(
                e.toString().split("id=")[1].split(",")[0]);

        if(buttonId != 1 && cameraOn){
            if(camera.stopCamera()){
                frameStart.setText("Start");
                cameraOn = false;
            }
        }
        if(buttonId == 1){
            Image i = new Image("/webcam.png");
            cameraFrame.setImage(i);
        }
        
        for (int i = 0; i < 4; i++) {
            if (panes[i].isVisible()) {
                panes[i].setVisible(false);
                panes[buttonId].setVisible(true);
            }
        }
    }

    public void startRecording() {
        if (!voiceRecording) {
            voiceStart.setText("Stop Recording");

            voiceRecording = true;
            voiceObject = new GoogleSpeechTest();
            voiceRecord = new Thread(voiceObject);
            voiceRecord.start();
        } else {
            System.out.println("Stopping camera...");
            voiceObject.endDetection();
            voiceStart.setText("Start Recording");
            voiceRecording = false;

        }
    }

    public void startFrames() { //Also closes frames
        if (cameraOn) {
            if(camera.stopCamera()){
                frameStart.setText("Start");
                cameraOn = false;
            }
        } else {
            frameStart.setText("Stop");
            cameraOn = true;
            camera = new CameraDetection(cameraFrame, socket);
            camera.startCamera();
        }
    }

    public void startTouch(ActionEvent e) {
        
        if(socket.testConn()){
            String buttonId = e.toString().split("id=")[1].split(",")[0];
            socket.sendData(buttonId);
//            if (buttonId.equals("touchStopStart")) {
//                if (touchOn) {
//                    System.out.println("Stopping touch control...");
//                    touchStopStart.setText("Start Touch Control");
//                    touchControl.stopTouching();
//                    touchOn = false;
//                } else {
//                    System.out.println("Starting touch control...");
//                    touchStopStart.setText("Stop Touch Control");
//                    touchControl = new TouchControl(socket);
//                    touchOn = true;
//                }
//            } else if (touchOn) {
//                touchControl.sendData(buttonId);
//            }
            
        }
        else{
            System.out.println("Please enter valid Socket connection information on the main page");
        }
    }
    
    public void setSocket(){
        System.out.println(socket.setData(address.getText(), Integer.parseInt(port.getText())));
    }

}
