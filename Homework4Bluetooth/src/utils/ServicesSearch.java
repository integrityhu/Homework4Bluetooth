package utils;

import java.io.IOException;
import java.util.Enumeration;

import javax.bluetooth.*;

public class ServicesSearch {

    public DiscoveryListener discoveryListener = new DeviceDiscoveryListener();
    
    @SuppressWarnings("rawtypes")
    public ServicesSearch(UUID[] searchUuidSet) throws IOException, InterruptedException {

        // First run RemoteDeviceDiscovery and use discoved device
        
        ((DeviceDiscoveryListener)discoveryListener).devicesDiscovered.clear();
        
        synchronized(((DeviceDiscoveryListener)discoveryListener).inquiryCompletedEvent) {
            System.out.println("LocalDeviceAddress: " + LocalDevice.getLocalDevice().getBluetoothAddress());
            System.out.println("FriendlyName: " + LocalDevice.getLocalDevice().getFriendlyName());
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, discoveryListener);
            if (started) {
                System.out.println("wait for device inquiry to complete...");
                ((DeviceDiscoveryListener)discoveryListener).inquiryCompletedEvent.wait();
                System.out.println(((DeviceDiscoveryListener)discoveryListener).devicesDiscovered.size() +  " device(s) found");
            }
        }

        ((DeviceDiscoveryListener)discoveryListener).serviceFound.clear();

        int[] attrIDs = new int[] { 0x0100 // Service name
        };  //

        for (Enumeration en = ((DeviceDiscoveryListener)discoveryListener).devicesDiscovered.elements(); en.hasMoreElements();) {
            RemoteDevice btDevice = (RemoteDevice) en.nextElement();

            synchronized (((DeviceDiscoveryListener)discoveryListener).serviceSearchCompletedEvent) {
                System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, discoveryListener);
                ((DeviceDiscoveryListener)discoveryListener).serviceSearchCompletedEvent.wait();
            }
        }

    }
}
