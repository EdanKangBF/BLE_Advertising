// Related: https://iasj.net/iasj?func=fulltext&aId=123828

package com.example.user.ble_advertising;

import android.util.Log;

public class LogDistancePathLossModel {
    public double rssi; // rssi
    double referenceRssi = -55; //rssid0, measured at chosen reference distance d0
    double referenceDistance = 0.944; //d0
    double pathLossExponent = 0.3; //line of sight in building, n
    double flatFadingMitigation = 0; //set to zero, as in no large obstacle, used to mitigate for flat fading. sigma

    LogDistancePathLossModel(double measuredRssi){
        rssi = measuredRssi; // rssi is measuresd rssi from nearby beacon
    }

    double getCalculatedDistance(){
        double distance;
        double rssiDiff = rssi = referenceRssi - flatFadingMitigation;

        double i = Math.pow(10, -(rssiDiff/10 * pathLossExponent));

        distance = referenceDistance * i;

        Log.d("getCalculatedDistance: ", "initiated");
        return distance;
    }
}
