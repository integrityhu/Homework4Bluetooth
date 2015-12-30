package utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

public class DeviceDiscoveryListener implements DiscoveryListener {

    @SuppressWarnings("rawtypes")
    public Vector/* <RemoteDevice> */devicesDiscovered = new Vector();
    public Object inquiryCompletedEvent = new Object();
    public Object serviceSearchCompletedEvent = new Object();
    @SuppressWarnings("rawtypes")
    public Map/* <String> */serviceFound = new HashMap();

    @SuppressWarnings("unchecked")
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
        devicesDiscovered.addElement(btDevice);
        try {
            System.out.println("     name " + btDevice.getFriendlyName(false));
        } catch (IOException cantGetDeviceName) {
        }
    }

    public void inquiryCompleted(int discType) {
        System.out.println("Device Inquiry completed!");
        synchronized (inquiryCompletedEvent) {
            inquiryCompletedEvent.notifyAll();
        }
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        System.out.println("service search completed!");
        synchronized (serviceSearchCompletedEvent) {
            serviceSearchCompletedEvent.notifyAll();
        }
    }

    @SuppressWarnings("unchecked")
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            if (url == null) {
                continue;
            }
            try {
                serviceFound.put(servRecord[i].getHostDevice().getFriendlyName(false), url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int[] attributeIds = servRecord[i].getAttributeIDs();
            for (int attrid = 0; attrid < attributeIds.length; attrid++) {
                DataElement serviceName = servRecord[i].getAttributeValue(attrid);
                System.out.println("-- Available service is: "+serviceName+"("+attrid+")");
            }
            DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
            if (serviceName != null) {
                System.out.println("service " + serviceName.getValue() + " found " + url);
            } else {
                System.out.println("service found " + url);
            }
        }
    }
}