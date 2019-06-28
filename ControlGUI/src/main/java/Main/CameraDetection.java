package Main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class CameraDetection{
    private ScheduledExecutorService timer;
    private VideoCapture capture;
    private boolean cameraActive = false;
    private boolean sendDone = false;
    private int absoluteHandSize = 0;
    private CascadeClassifier handCascade;
    private SocketClient socket;
    String path = CameraDetection.class.getResource("/palm.xml").getPath();
    @FXML
    private ImageView frameSet;
    
    public CameraDetection(ImageView frameSet, SocketClient socket) {
        this.socket = socket;
        System.out.println("123");
        path = (path.toCharArray()[0] == '/') ? path.substring(1) : path;
        //socket = new SocketClient("192.168.0.118",50051);
        this.frameSet = frameSet;
    }

    public void startCamera() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture();
        if (!cameraActive) {
            capture.open(0);
            if (capture.isOpened()) {
                cameraActive = true;
                Runnable frameGrabber = () -> {
                    if(cameraActive){
                        Mat frame = getFrame();
                        Size size = frame.size();
                        Imgproc.line(frame, new Point(0, size.height / 2), new Point(size.width, size.height / 2), new Scalar(0, 0, 0), 2);
                        Imgproc.line(frame, new Point(size.width / 3, 0), new Point(size.width / 3, size.height), new Scalar(0, 0, 0), 2);
                        Imgproc.line(frame, new Point((size.width / 3) * 2, 0), new Point((size.width / 3) * 2, size.height), new Scalar(0, 0, 0), 2);
                        Mat frame2 = detectHand(frame);
                        frameSet.setImage(Utils.mat2Image(frame2));
                    }
                };
                timer = Executors.newSingleThreadScheduledExecutor();
                timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            }
        }
    }

    private Mat getFrame() {    
        if (capture.isOpened()) {
            Mat frame = new Mat();
            try {
                capture.read(frame);
                if (!frame.empty()) {
                    Core.flip(frame, frame, Core.ROTATE_180);
                }
            } catch (Exception e) {}
            return frame;
        } else {return null;}
    }

    public Mat detectHand(Mat frame) {
        handCascade = new CascadeClassifier(path); // /resources/palm.xml
        
        MatOfRect hands = new MatOfRect();
        Mat grayFrame = new Mat();
        Imgproc.bilateralFilter(frame, grayFrame, 9, 0, 0);
        Imgproc.cvtColor(grayFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);
        //Imgproc.GaussianBlur(grayFrame,grayFrame, new Size(0,0), 5);
        
        int height = grayFrame.rows();
        if (Math.round(height * 0.1f) > 0) {
            absoluteHandSize = Math.round(height * 0.1f);
        }
        
        //this.handCascade.detectMultiScale3(grayFrame, hands, new MatOfInt(9), new MatOfDouble(1.1));
        handCascade.detectMultiScale(frame, hands, 1.1, 9, 0, new Size(), new Size());
        Rect[] handsArray = hands.toArray();
        Point handLocation = new Point();
        if (handsArray.length > 0) {
            for (Rect hand : handsArray) {
                handLocation = new Point(hand.tl().x + (hand.br().x - hand.tl().x) / 2, hand.tl().y + (hand.br().y - hand.tl().y) / 2);
                //Imgproc.rectangle(frame, hand.tl(), hand.br(), new Scalar(0, 255, 0, 255), 3);
                Imgproc.circle(
                    frame, //Matrix obj of the image
                    handLocation, //Center of the circle
                    3, //Radius
                    new Scalar(0, 0, 255), //Scalar object for color
                    25 //Thickness of the circle
                );
            }
            if(!sendDone) checkLocation(handLocation,frame.size());
            return frame;
        } else {return frame;}
    }

    public void checkLocation(Point handLocation, Size frameSize) {
        //Hand is in a forward position
        if (handLocation.y < frameSize.height / 2) {
            if (handLocation.x > frameSize.width / 3 && handLocation.x < (frameSize.width / 3) * 2) {
                //move forward
                sendDone = socket.sendData("forward");
            }
            if (handLocation.x < frameSize.width / 3) {
                //move forward and left
                sendDone = socket.sendData("forward-left");
            } else {
                //move forward and right
                sendDone = socket.sendData("forward-right");
            }
        }
        if (handLocation.y > frameSize.height / 2) {
            if (handLocation.x > frameSize.width / 3 && handLocation.x < (frameSize.width / 3) * 2) {
                //move backward
                sendDone = socket.sendData("backward");
            }
            if (handLocation.x < frameSize.width / 3) {
                //move backward and left
                sendDone = socket.sendData("backward-left");
            } else {
                //move backward and right
                sendDone = socket.sendData("backward-right");
            }
        }
        String location = "";
        sendDone = false;
    }

    public boolean stopCamera() {
//        if (socket.isOpen()) {
//            socket.closeConnection();
//        }
        timer.shutdownNow();
//        try {
//            if(!timer.awaitTermination(1000, TimeUnit.MICROSECONDS)){
//                timer.shutdownNow();
//            }
//            
//        }catch (InterruptedException e) {}
        System.out.println(timer.isShutdown());
        System.out.println(timer.isTerminated());
        capture.release();
        System.out.println(capture.isOpened());
        return true;
    }
}
