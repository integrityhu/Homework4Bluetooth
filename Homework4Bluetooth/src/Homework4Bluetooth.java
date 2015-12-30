import java.io.IOException;

import tasks.LoggerClient;
import tasks.ModemComm;
import tasks.ObexPutClient;


public class Homework4Bluetooth {

    /**
     * @param args
     * @throws InterruptedException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //ModemComm.main(args);
        ObexPutClient.main(args);
        LoggerClient.main(args);
    }

}
