package com.example.user.ble_advertising;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    public static final int GENERAL = 0xFFFF; // reserved id that can be used for testing purposes; you cannot ship any product with 0xFFFF set as the manufacturer id.
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean isScanning = false;
    private ScanCallback mScanCallback;
    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    private ArrayList<String> scanResultList;
    private static final long SCAN_PERIOD = 999999999;
    private Handler mHandler = new Handler();

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private boolean isAdvertising = false;
    private AdvertiseCallback mAdvertiseCallback;

    private static String[] PERMISSIONS_ACCESS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;


    private Dialog dialogPermission = null;
    private int REQUEST_ENABLE_BT = 1;


    private TextView textView_info;
    private ListView listView_scanResult;
    private TextView editText_data;
    //    private List<ScanFilter> filters = new ArrayList<ScanFilter>();
    //Triangulation Parameters
    //RSSI = -(10n log(d) + A)
    KalmanFilter kf = new KalmanFilter(0.065, 1.4, 0, 0);
    private ArrayList<ArrayList<String>> nodes = new ArrayList<ArrayList<String>>();


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBluetoothLowEnergyFeature();
        initBluetoothService();
        initScanAndAdvertiseCallback();
        initUI();
    }

    private void checkBluetoothLowEnergyFeature() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initBluetoothService() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!mBluetoothAdapter.isLe2MPhySupported()) {
            Toast.makeText(this, "!mBluetoothAdapter.isLe2MPhySupported()", Toast.LENGTH_SHORT).show();
        }
    }

    private void initScanAndAdvertiseCallback() {
        mScanCallback = new ScanCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                mBluetoothDevices.clear();
                scanResultList.clear();
                saveScanResult(result);
                //TODO: OnScanResult
                setAndUpdateListView();

//                BluetoothDevice device = result.getDevice();
//                mBluetoothDevices.add(device);
//                listAdapter.notifyDataSetChanged();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                mBluetoothDevices.clear();
                //scanResultList.clear();
                for (ScanResult result : results) {
                    saveScanResult(result);
                }
                setAndUpdateListView();
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Toast.makeText(MainActivity.this
                        , "Error scanning devices: " + errorCode
                        , Toast.LENGTH_LONG).show();
            }
        };
        mAdvertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveScanResult(ScanResult result) {
        if (result.getScanRecord() != null && hasManufacturerData(result.getScanRecord())) {
            String tempValue = unpackPayload(result.getScanRecord()
                    .getManufacturerSpecificData(GENERAL));
            tempValue = tempValue.substring(1);
            if (!mBluetoothDevices.contains(result.getDevice())) {
//                Log.d("predicted distance", String.valueOf(predict));
//                Log.d("TXPower" , String.valueOf(result.getTxPower()));
//                Log.d("getRSSI " , String.valueOf(result.getRssi()));
//                Log.d("y", String.valueOf(y));
//                Log.d("distance " , distance + "M");
//                Log.d("RSSI value", String.valueOf(result.getRssi()));
//                Log.d("Log-Distance Path" , String.valueOf(d.getCalculatedDistance()));
//                Log.d("ITU Model" , String.valueOf(itu_model));

                int count = 0;
                count++;
                //add to arraylist
                mBluetoothDevices.add(result.getDevice());
                scanResultList.add(result.getDevice().getName() + ","
                        + result.getRssi() + ","
                        + result.getTxPower() + ","
                        + tempValue + ","
                        + "50" + "," +
                        count);

                for(int i = 0; i<scanResultList.size(); i++){
                    nodes.add(scanResultList);
                }


                Log.d("scanResultList" , String.valueOf(nodes));
//                writeToFile(scanResultList, this);
//                Log.d("write to file: ", "success");


            }
        }
    }


    private boolean hasManufacturerData(ScanRecord record) {
        SparseArray<byte[]> data = record.getManufacturerSpecificData();
        return (data != null && data.get(GENERAL) != null);
    }

    private String unpackPayload(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.get();
        byte[] b = new byte[buffer.limit()];
        for (int i = 0; i < buffer.limit(); i++) {
            b[i] = buffer.get(i);
        }
        try {
            return (new String(b, "UTF-8"));
        } catch (Exception e) {
            return " Unable to unpack.";
        }
    }

    private void setAndUpdateListView() {
        ListAdapter listAdapter = new ArrayAdapter<>(getBaseContext()
                , android.R.layout.simple_expandable_list_item_1, scanResultList);
        listView_scanResult.setAdapter(listAdapter);
    }

    private void initUI() {
        textView_info = findViewById(R.id.textView_info);
        listView_scanResult = findViewById(R.id.listView_scanResult);
        scanResultList = new ArrayList<>();
        setAndUpdateListView();
        editText_data = findViewById(R.id.editText_data);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            checkBluetoothEnableThenScanAndAdvertising();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            checkBluetoothEnableThenScanAndAdvertising();
        } else {
            showDialogForPermission();
        }
    }

    private void showDialogForPermission() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle(getResources().getString(R.string.dialog_permission_title));
        dialogBuilder.setMessage(getResources().getString(R.string.dialog_permission_message));
        dialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_permission_ok)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                PERMISSIONS_ACCESS,
                                REQUEST_ACCESS_FINE_LOCATION);
                    }
                });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.dialog_permission_no)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this
                                , getResources().getString(R.string.dialog_permission_toast_negative)
                                , Toast.LENGTH_LONG).show();
                    }
                });
        if (dialogPermission == null) {
            dialogPermission = dialogBuilder.create();
        }
        if (!dialogPermission.isShowing()) {
            dialogPermission.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetoothEnableThenScanAndAdvertising();
                } else {
                    Toast.makeText(MainActivity.this
                            , getResources().getString(R.string.dialog_permission_toast_negative)
                            , Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkBluetoothEnableThenScanAndAdvertising() {
        if (mBluetoothAdapter.isEnabled()) {
            startScanLeDevice();
            startAdvertising();
        } else {
            openBluetoothSetting();
        }
    }

    private void startScanLeDevice() {
        if (isScanning) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     stopScanLeDevice();
                                 }
                             }
                , SCAN_PERIOD);

        isScanning = true;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        int reportDelay = 0;
        if (mBluetoothAdapter.isOffloadedScanBatchingSupported()) {
            reportDelay = 1000;
        }
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // SCAN LATENCY
                .setReportDelay(reportDelay)
                .build();
        mBluetoothLeScanner.startScan(null, settings, mScanCallback);
        textView_info.setText(getResources().getString(R.string.bt_scanning));
    }

    private void stopScanLeDevice() {
        if (isScanning) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            isScanning = false;
            textView_info.setText(getResources().getString(R.string.bt_stop_scan));
        }
    }

//    private void setScanFilter(){
//        ScanFilter filter = new ScanFilter.Builder()
//                .setServiceUuid(ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb"))
//                .setDeviceName("fromScan")
//                .build();
//        filters.add(filter);
//
//    }

//    private List<ScanFilter> buildScanFilters() {
//        List<ScanFilter> scanFilters = new ArrayList<>();
//
//        ScanFilter.Builder builder = new ScanFilter.Builder();
//        // Comment out the below line to see all BLE devices around you
//        builder.setServiceUuid(ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb"));
//        scanFilters.add(builder.build());
//
//        return scanFilters;
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startAdvertising() {
        if (isAdvertising) {
            return;
        }

        if (!mBluetoothAdapter.isLe2MPhySupported()) {
            isAdvertising = true;
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //ADVERTISING FREQUENCY
                    .setConnectable(false)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //ADJUST DISTANCE
                    .build();
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addManufacturerData(GENERAL, buildPayload(editText_data.getText().toString())) // maximum 24 bytes if alone else 22 bytes
                    .addServiceUuid(ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb")) //0xb81d random service uuid
                    .setIncludeDeviceName(true)
                    .build();

            AdvertiseData scanResponse = new AdvertiseData.Builder()
                    .addManufacturerData(GENERAL, buildPayload("BT4"))
                    .setIncludeDeviceName(false) // TODO: Changed to false 3 Jan
                    .build();

            mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponse, mAdvertiseCallback);
        } else {
            isAdvertising = true;
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //ADVERTISING FREQUENCY
                    .setConnectable(false)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //ADJUST DISTANCE
                    .build();
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addManufacturerData(GENERAL, buildPayload(editText_data.getText().toString())) // maximum 24 bytes if alone else 22 bytes
                    .addServiceUuid(ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb")) //0xb81d random service uuid
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(true)
                    .build();

            AdvertiseData scanResponse = new AdvertiseData.Builder()
                    .addManufacturerData(GENERAL, buildPayload("Scan BT 5.0"))
                    .setIncludeDeviceName(false) // TODO: Changed to false 3 Jan
                    .build();

            mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponse, mAdvertiseCallback);
        }
    }

    private byte[] buildPayload(String value) {
        byte flags = (byte) 0x8000000;
        byte[] b = {};
        try {
            b = value.getBytes("UTF-8");
        } catch (Exception e) {
            return b;
        }
        int max = 26;
        int capacity;
        if (b.length <= max) {
            capacity = b.length + 1;
        } else {
            capacity = max + 1;
            System.arraycopy(b, 0, b, 0, max);
        }
        byte[] output;
        output = ByteBuffer.allocate(capacity)
                .order(ByteOrder.LITTLE_ENDIAN) //GATT APIs expect LE order
                .put(flags) //Add the flags byte
                .put(b)
                .array();
        return output;
    }

    private void openBluetoothSetting() {
        Intent bluetoothSettingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(bluetoothSettingIntent, REQUEST_ENABLE_BT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            checkBluetoothEnableThenScanAndAdvertising();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void btnClick(View v) {
        switch (v.getId()) {
            case R.id.button_scan:
                startScanLeDevice();
                break;
            case R.id.button_stop:
                stopScanLeDevice();
                break;
            case R.id.button_save:
                stopAndRestartAdvertising();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopAndRestartAdvertising() {
        if (isAdvertising) {
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            isAdvertising = false;
        }
        startAdvertising();
    }



    private void writeToFile(ArrayList<String> scanResultList, Context context) {
        StringBuilder str = new StringBuilder("");
        for (String eachstring : scanResultList) {
            str.append(eachstring).append(",");
        }
        String commaseparatedlist = str.toString();

        if (commaseparatedlist.length() > 0) {
            commaseparatedlist = commaseparatedlist.substring(0, commaseparatedlist.length() - 1);
        }
        //Log.d("write to file", commaseparatedlist);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config_100cm_2.txt", Context.MODE_APPEND));
            outputStreamWriter.write(commaseparatedlist);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        try {
            File path = context.getFilesDir();
            File file = new File(path, "test.txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(commaseparatedlist.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }

}
