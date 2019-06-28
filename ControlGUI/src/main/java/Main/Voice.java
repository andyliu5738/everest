package Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.login.LoginException;

public class Voice implements Runnable {//
    private boolean doStop = false;
    private Configuration configuration;
    private List<String> words = new ArrayList<String>();
    private LiveSpeechRecognizer recognizer;
    private SpeechResult result;
    
    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return this.doStop == false;
    }
    
    @Override
    public void run(){
        try{
            this.start();
            
        }
        catch(IOException e){}
    }
    
    public Voice() throws IOException{
        this.configuration = new Configuration();
        this.configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        this.configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        this.configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        
    }
    
    public void start() throws IOException{
//        synchronized(Thread.currentThread()){
//            Thread.currentThread().notify();
//        }

        recognizer = new LiveSpeechRecognizer(this.configuration);
        recognizer.startRecognition(true);
        while ((result = recognizer.getResult()) != null) {  
            words.add(result.getWords().toString());
	}

    }
    public void stop(){
        System.out.println("stopping");
        recognizer.stopRecognition();
        
    }
}
