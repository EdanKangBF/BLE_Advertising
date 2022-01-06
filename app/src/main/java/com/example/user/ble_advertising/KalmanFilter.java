package com.example.user.ble_advertising;

import android.util.Log;

// Adapted from:
// https://web.archive.org/web/20140131183356/http://interactive-matter.eu/blog/2009/12/18/filtering-sensor-data-with-a-kalman-filter/
public class KalmanFilter {
    public double q; // process noise covariance
    public double r; // measurement noise covariance
    public double p; // value
    public double x; // estimation error covariance
    public double k; // kalman gain
    public static double TRAINING_PREDICTION_LIMIT = 500;
    public double predictionCycles = 0;

    //Constructor
    KalmanFilter(double processNoise, double sensorNoise, double estimatedError,double initialValue){
        q = processNoise;
        r = sensorNoise;
        p = estimatedError;
        x = initialValue;

        Log.d("Kalman Filter: ", "initialised");
    }

    double getFilteredValue(double measurement){
        //prediction phase
        p = p + q;

        //measurement update
        k = p / (p + r);
        x = x + k * (measurement - x);
        p = (1 - k) * p;

        Log.d("getFilteredValue: ", "initialised");
        return x;
    }

}
