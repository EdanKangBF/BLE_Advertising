package com.example.user.ble_advertising;

// Received advertising device info
public class BeaconInfo {
    public String deviceMake;
    public String deviceUUID;
    public String txPower;
    public String payload;

    BeaconInfo(String deviceMake, String deviceUUID, String payload){
        this.deviceMake = deviceMake;
        this.deviceUUID = deviceUUID;
        this.payload = payload;
    }

}
