package tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import utils.BlueUUIDs;
import utils.DeviceDiscoveryListener;
import utils.ServicesSearch;

public class LoggerClient {
    static final UUID RFCOMM = new UUID(0x0003);

    @SuppressWarnings("rawtypes")
    public static final Map/* <String> */serviceFound = new HashMap();

    @SuppressWarnings("rawtypes")
    public static final Vector/* <RemoteDevice> */devicesDiscovered = new Vector();

    private static final int MAX_READ = 15000;

    @SuppressWarnings({ "static-access", "deprecation" })
    public static void main(String[] args) throws IOException, InterruptedException {
        InputStream inputStream = new FileInputStream(new File("default.properties"));
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        Parser parser = new GnuParser();
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("serverURL").withLongOpt("serverURL").hasArg(true).withDescription("Bluetooth server URL").create());
        options.addOption(OptionBuilder.withArgName("friendlyName").withLongOpt("friendlyName").hasArg(true).withDescription("Bluetooth device name").create());
        CommandLine commandLine = null;
        String serverURL = null;
        String friendlyName = null;
        try {
            commandLine = parser.parse(options, args);
            serverURL = (String) commandLine.getOptionValue("serverURL");
            friendlyName = (String) commandLine.getOptionValue("friendlyName");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if ((serverURL == null) && (friendlyName == null)) {
            serverURL = properties.getProperty(properties.getProperty("GPSDeviceName"));
        } else if (friendlyName != null) {
            System.out.println("friendlyName:" + friendlyName);
            UUID[] searchUuidSet = new UUID[] { BlueUUIDs.RFCOMM};
            ServicesSearch services = new ServicesSearch(searchUuidSet);
            if (((DeviceDiscoveryListener) services.discoveryListener).serviceFound.size() == 0) {
                System.out.println("RFCOMM service not found");
                return;
            } else {
                serverURL = (String) ((DeviceDiscoveryListener) services.discoveryListener).serviceFound.get(friendlyName);
            }
        }

        if (serverURL == null) {
            System.out.println("Usage: java -jar readLogger.jar [-serverURL <destination>]|[-friendlyName <devicename>]");
            return;
        }

        System.out.println("Connecting to " + serverURL);

        StreamConnection clientConnection = (StreamConnection) Connector.open(serverURL);

        InputStream reader = clientConnection.openInputStream();
        byte[] buff = new byte[1];
        int counter = 0;
        try {
            while (((counter += reader.read(buff)) > 0) && (counter < MAX_READ)) {
                System.out.write(buff);
            }
        } finally {
            clientConnection.close();
        }
    }
}
