package Main;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GoogleSpeechTest implements Runnable{
    private TargetDataLine microphone;
    private SocketClient socket;
            
    @Override
    public void run(){
        startRecording();
    }
    public GoogleSpeechTest() {
        socket = new SocketClient("192.168.0.118",50051);
    }
 
    public void startRecording(){       //Target data line
            
        System.out.println("Starting API");
        AudioInputStream audio = null;

        //Check if Microphone is Supported
        checkMicrophoneAvailability();

        //Print available mixers
        //printAvailableMixers();

        //Capture Microphone Audio Data
        try {

                // Signed PCM AudioFormat with 16kHz, 16 bit sample size, mono
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                //Check if Microphone is Supported
                if (!AudioSystem.isLineSupported(info)) {
                        System.out.println("Microphone is not available");
                        //setText("Microphone: Not available", mic);
                        System.exit(0);
                }

                //Get the target data line
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();


                //Audio Input Stream
                audio = new AudioInputStream(microphone);

        } catch (Exception ex) {
                ex.printStackTrace();
        }

        //Send audio from Microphone to Google Servers and return Text
        try (SpeechClient client = SpeechClient.create()) {

                ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {

                        public void onStart(StreamController controller) {
                                System.out.println("Started....");
                                //setText("Recording: True",rec);
                        }

                        public void onResponse(StreamingRecognizeResponse response) {
                                System.out.println("Speech detected:" + response.getResults(0).getAlternatives(0).getTranscript());
                                
                                socket.sendData(response.getResults(0).getAlternatives(0).getTranscript());
                                //setText(response.getResults(0).toString(),detect);
                        }

                        public void onComplete() {
                                System.out.println("Complete");
                        }

                        public void onError(Throwable t) {
                                System.err.println(t);
                        }
                };

                ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

                RecognitionConfig recConfig = RecognitionConfig.newBuilder().setEncoding(RecognitionConfig.AudioEncoding.LINEAR16).setLanguageCode("en-US").setSampleRateHertz(16000)
                                .build();
                StreamingRecognitionConfig config = StreamingRecognitionConfig.newBuilder().setConfig(recConfig).build();

                StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder().setStreamingConfig(config).build(); // The first request in a streaming call has to be a config

                clientStream.send(request);

                //Infinity loop from microphone
                while (true) {
                        byte[] data = new byte[10];
                        try {
                                audio.read(data);
                        } catch (IOException e) {
                                System.out.println(e);
                        }
                        request = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build();
                        clientStream.send(request);
                }
        } catch (Exception e) {
                System.out.println(e);
        }

    }

    /**
     * Checks if the Microphone is available
     */
    public void checkMicrophoneAvailability() {
            enumerateMicrophones().forEach((string , info) -> {
                System.out.println("Microphone detected:" + string);
            });
    }

    /**
     * Generates a hashmap to simplify the microphone selection process. The keyset is the name of the audio device's Mixer The value is the first
     * lineInfo from that Mixer.
     * 
     * @author Aaron Gokaslan (Skylion)
     * @return The generated hashmap
     */
    public static HashMap<String,Line.Info> enumerateMicrophones() {
            HashMap<String,Line.Info> out = new HashMap<String,Line.Info>();
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            for (Mixer.Info info : mixerInfos) {
                    Mixer m = AudioSystem.getMixer(info);
                    Line.Info[] lineInfos = m.getTargetLineInfo();
                    if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class))//Only adds to hashmap if it is audio input device
                            out.put(info.getName(), lineInfos[0]);//Please enjoy my pun
            }
            return out;
    }	
    
    public void endDetection(){
        if(socket.isOpen()){
            socket.closeConnection();
        }
        this.microphone.close();
        System.out.println("No longer recording...");
    }
}
