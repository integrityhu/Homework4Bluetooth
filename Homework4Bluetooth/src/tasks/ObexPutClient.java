package tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import utils.BlueUUIDs;
import utils.DeviceDiscoveryListener;
import utils.ServicesSearch;

public class ObexPutClient {

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws IOException, InterruptedException {
        InputStream inputStream = new FileInputStream(new File("default.properties"));
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        String subject = "openit.txt";
        File file = new File("default.properties");
        Parser parser = new GnuParser();
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("serverURL").withLongOpt("serverURL").hasArg(true).withDescription("Bluetooth server URL").create());
        options.addOption(OptionBuilder.withArgName("friendlyName").withLongOpt("friendlyName").hasArg(true).withDescription("Bluetooth device name").create());
        options.addOption(OptionBuilder.withArgName("subject").withLongOpt("subject").hasArg(true).withDescription("Subject of file").create());
        options.addOption(OptionBuilder.withArgName("file").withLongOpt("file").hasArg(true).withDescription("File for send").create());
        CommandLine commandLine = null;
        String serverURL = null;
        String friendlyName = null;
        try {
            commandLine = parser.parse(options, args);
            serverURL = (String) commandLine.getOptionValue("serverURL");
            friendlyName = (String) commandLine.getOptionValue("friendlyName");
            if (commandLine.getOptionValue("subject") != null) {
                subject = (String) commandLine.getOptionValue("subject");
            }
            if (commandLine.getOptionValue("file") != null) {
                file = new File((String) commandLine.getOptionValue("file"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if ((serverURL == null) && (friendlyName == null)) {
            serverURL = properties.getProperty(properties.getProperty("OBEXDeviceName"));
        } else if (friendlyName != null) {
            System.out.println("friendlyName:" + friendlyName);
            UUID[] searchUuidSet = new UUID[] { BlueUUIDs.OBEX_OBJECT_PUSH};
            ServicesSearch services = new ServicesSearch(searchUuidSet);
            if (((DeviceDiscoveryListener) services.discoveryListener).serviceFound.size() == 0) {
                System.out.println("OBEX service not found");
                return;
            } else {
                serverURL = (String) ((DeviceDiscoveryListener) services.discoveryListener).serviceFound.get(friendlyName);
            }
        }

        if (serverURL == null) {
            System.out.println("Usage: java -jar notifyOverBluetooth.jar [-serverURL <destination>] [-friendlyName <devicename>] [-file <filename>]");
            return;
        }

        System.out.println("Connecting to " + serverURL);

        ClientSession clientSession = (ClientSession) Connector.open(serverURL);
        HeaderSet hsConnectReply = clientSession.connect(null);
        if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
            System.out.println("Failed to connect");
            return;
        }

        HeaderSet hsOperation = clientSession.createHeaderSet();
        if ((subject != null) && (file != null)) {
            hsOperation.setHeader(HeaderSet.NAME, subject);
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            hsOperation.setHeader(HeaderSet.TYPE, ext);
        } else {
            hsOperation.setHeader(HeaderSet.NAME, "notify.txt");
            hsOperation.setHeader(HeaderSet.TYPE, "txt");
        }
        // Create PUT Operation
        Operation putOperation = clientSession.put(hsOperation);

        // Send some text to server
        byte data[] = {};
        if (file != null) {
            FileInputStream fIn = new FileInputStream(file);
            data = new byte[fIn.available()];
            fIn.read(data);
            fIn.close();
        } else {
            data = "Notify".getBytes("ISO-8859-2");
        }
        OutputStream os = putOperation.openOutputStream();
        os.write(data);
        os.close();

        putOperation.close();

        clientSession.disconnect(null);

        clientSession.close();
    }
}
