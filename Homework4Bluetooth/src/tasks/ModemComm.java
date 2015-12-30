package tasks;

import java.io.IOException;
import java.io.OutputStream;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import utils.StreamGobbler;

public class ModemComm {
    static final UUID RFCOMM = new UUID(0x0003);

    public static void main(String[] args) throws IOException, InterruptedException {

        Parser parser = new GnuParser();
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("serverURL").withLongOpt("serverURL").isRequired(true).hasArg(true).withDescription("Bluetooth serial URL").create());
        CommandLine commandLine = null;
        String serverURL = null;
        try {
            commandLine = parser.parse(options, args);
            serverURL = (String) commandLine.getOptionValue("serverURL"); //--serverURL btspp://00123ABC67B9:2
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (serverURL == null) {
            System.out.println("Usage: java -jar homework4bluetooth.jar [-serverURL <destination>]");
            return;
        }

        System.out.println("Connecting to " + serverURL);

        StreamConnection clientConnection = (StreamConnection) Connector.open(serverURL);
        StreamGobbler outputGobbler = new StreamGobbler(clientConnection.openInputStream());
        outputGobbler.start();

        OutputStream writer = clientConnection.openOutputStream();
        byte[] buff = new byte[20];
        try {
            for (int idx = 1; idx < 20; idx++) {
                writer.write(("AT+CMGR=" + idx + "\r\n").getBytes());
                Thread.sleep(1000);
            }
        } finally {
            Thread.sleep(50000);
            outputGobbler.interrupt();
            outputGobbler.join();
            clientConnection.close();
        }
    }
}
