package com.fyp.musclefatigue.screen;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.databinding.ActivityTrackMusclesFatigueBinding;
import com.fyp.musclefatigue.helpers.TimestampUtil;
import com.github.mikephil.charting.components.Description;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.androidplot.xy.XYPlot;
import com.shimmerresearch.android.guiUtilities.supportfragments.SignalsToPlotFragment;

public class TrackMusclesFatigueActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TrackMusclesFatigueActivity.class.getSimpleName();

    private ActivityTrackMusclesFatigueBinding binding;

    ShimmerBluetoothManagerAndroid btManager;
    ShimmerDevice shimmerDevice;
    String shimmerBtAdd = "00:00:00:00:00:00";

    private static final int BLUETOOTH_PERMISSION_REQUEST = 1001;


  //  private double fatigueThreshold = -1; // Adjust this threshold based on your data characteristics
    private long millis = 0L;
    private boolean running;
    private boolean isFatigueDetected;
    private Handler timeHandler;

    List<Double> emgDataList = new ArrayList<>();
    List<Double> allEMGDataList = new ArrayList<>();

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackMusclesFatigueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        runTimer();
    }

    @Override
    public void onClick(View v) {
        if (binding.backIconIv.getId() == v.getId()){
            onBackPressed();
        }
        if (binding.startStreamingBtn.getId() == v.getId()){
            startStreaming();
        }
        if (binding.stopStreamingBtn.getId() == v.getId()){
            stopStreaming();
        }
        if (binding.saveStreamingBtn.getId() == v.getId()){
            checkPermissionForSaveFile();
        }
        if (binding.resetStreamingBtn.getId() == v.getId()){
            reset();
        }
//        if (binding.fatigueThresholdBtn.getId() == v.getId()){
//            if (binding.fatigueThresholdEt.getText().toString().isEmpty()){
//                binding.fatigueThresholdEt.setError("Fatigue Threshold cannot be empty");
//            }else{
//                fatigueThreshold = Double.parseDouble(binding.fatigueThresholdEt.getText().toString());
//            }
//        }
        if (binding.sensorStatusTv.getId() == v.getId()){
            if (!isBluetoothEnabled()){
                bluetoothOn();
            } else if (btManager == null || !btManager.DevicesConnected(shimmerBtAdd)) {
                connectDevice();
            }

        }
    }

    @Override
    protected void onStart() {
        //Connect the Shimmer using its Bluetooth Address
        try {
            btManager.connectShimmerThroughBTAddress(shimmerBtAdd);
        } catch (Exception e) {
            Log.e(TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
            Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                    "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        //Disconnect the Shimmer device when app is stopped
        if(shimmerDevice != null) {
            if(shimmerDevice.isSDLogging()) {
                shimmerDevice.stopSDLogging();
                Log.d(TAG, "Stopped Shimmer Logging");
            }
            else if(shimmerDevice.isStreaming()) {
                shimmerDevice.stopStreaming();
                Log.d(TAG, "Stopped Shimmer Streaming");
            }
            else {
                shimmerDevice.stopStreamingAndLogging();
                Log.d(TAG, "Stopped Shimmer Streaming and Logging");
            }
        }

        if (btManager != null)
            btManager.disconnectAllDevices();

        running = false;
        Log.i(TAG, "Shimmer DISCONNECTED");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }



    private void init(){
        binding.backIconIv.setOnClickListener(this);
        binding.sensorStatusTv.setOnClickListener(this);
        binding.startStreamingBtn.setOnClickListener(this);
        binding.stopStreamingBtn.setOnClickListener(this);
        binding.resetStreamingBtn.setOnClickListener(this);
        binding.saveStreamingBtn.setOnClickListener(this);
       // binding.fatigueThresholdBtn.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_PERMISSION_REQUEST);
        }

        try {
            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
        }

        if (!isBluetoothEnabled()){
            binding.sensorStatusTv.setText(Html.fromHtml(getString(R.string.bluetooth_off)));
            binding.sensorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_yellow_dot),null);
        }else if (btManager == null || !btManager.DevicesConnected(shimmerBtAdd)){
            binding.sensorStatusTv.setText(Html.fromHtml(getString(R.string.sensor_disconnected)));
            binding.sensorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_red_dot),null);
        }else {
            binding.sensorStatusTv.setText(Html.fromHtml(getString(R.string.sensor_connected)));
            binding.sensorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_green_dot),null);
        }

       // binding.fatigueThresholdEt.setText(String.valueOf(fatigueThreshold));
    }

    private void startStreaming() {
        if (btManager != null){
            Shimmer shimmer = (Shimmer) btManager.getShimmer(shimmerBtAdd);
            if(shimmer != null) {   //this is null if Shimmer device is not connected
                //Disable PC timestamps for better performance. Disabling this takes the timestamps on every full packet received instead of on every byte received.
                shimmer.enablePCTimeStamps(false);
                //Enable the arrays data structure. Note that enabling this will disable the Multimap/FormatCluster data structure
                shimmer.enableArraysDataStructure(true);
                running = true;
                btManager.startStreaming(shimmerBtAdd);
                if (isFatigueDetected){
                    emgDataList.clear();
                    allEMGDataList.clear();
                    isFatigueDetected = false;
                }
            } else {
                Toast.makeText(this, "Can't start streaming\nShimmer device is not connected", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Can't start streaming\nShimmer device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopStreaming() {
        if(btManager.getShimmer(shimmerBtAdd) != null) {
            btManager.stopStreaming(shimmerBtAdd);
            running = false;
        } else {
            Toast.makeText(this, "Can't stop streaming\nShimmer device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void reset() {
        stopStreaming();
        emgDataList.clear();
        allEMGDataList.clear();
        plotData(emgDataList,detectMuscleFatigue(emgDataList));
        binding.fatigueDetectTv.setVisibility(View.GONE);
        millis = 0L;
        running = false;
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    if ((msg.obj instanceof ObjectCluster)) {
                        ObjectCluster objc = (ObjectCluster) msg.obj;

                        // Retrieve EMG data
                        double emgData = objc.getFormatClusterValue(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT, ChannelDetails.CHANNEL_TYPE.CAL.toString());

                        // Store EMG data in the list
                        if(allEMGDataList.size() == 0 || allEMGDataList.get(allEMGDataList.size()-1) != emgData){
                            emgDataList.add(emgData);
                            allEMGDataList.add(emgData);
                        }


                        // Limit the number of data points to prevent hanging
                        if (emgDataList.size() > 400) {
                            emgDataList.remove(0); // Remove oldest data point
                        }

                        isFatigueDetected = detectMuscleFatigue(emgDataList);

                        // Update graph with new data points
                        plotData(emgDataList ,isFatigueDetected);

                    }
                    break;
                case Shimmer.MESSAGE_TOAST:
                    /** Toast messages sent from {@link Shimmer} are received here. E.g. device xxxx now streaming.
                     *  Note that display of these Toast messages is done automatically in the Handler in {@link com.shimmerresearch.android.shimmerService.ShimmerService} */
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state = null;
                    String macAddress = "";

                    if (msg.obj instanceof ObjectCluster) {
                        state = ((ObjectCluster) msg.obj).mState;
                        macAddress = ((ObjectCluster) msg.obj).getMacAddress();
                    } else if (msg.obj instanceof CallbackObject) {
                        state = ((CallbackObject) msg.obj).mState;
                        macAddress = ((CallbackObject) msg.obj).mBluetoothAddress;
                    }

                    Log.d(TAG, "Shimmer state changed! Shimmer = " + macAddress + ", new state = " + state);

                    switch (state) {
                        case CONNECTED:
                            Log.i(TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            Log.i(TAG, "Shimmer [" + macAddress + "] is now CONNECTED");
                            shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                            if(shimmerDevice != null) { Log.i(TAG, "Got the ShimmerDevice!"); }
                            else { Log.i(TAG, "ShimmerDevice returned is NULL!"); }
                            break;
                        case CONNECTING:
                            Log.i(TAG, "Shimmer [" + macAddress + "] is CONNECTING");
                            break;
                        case STREAMING:
                            Log.i(TAG, "Shimmer [" + macAddress + "] is now STREAMING");
                            break;
                        case STREAMING_AND_SDLOGGING:
                            Log.i(TAG, "Shimmer [" + macAddress + "] is now STREAMING AND LOGGING");
                            break;
                        case SDLOGGING:
                            Log.i(TAG, "Shimmer [" + macAddress + "] is now SDLOGGING");
                            if(shimmerDevice == null) {
                                shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                                Log.i(TAG, "Got the ShimmerDevice!");
                            }
                            break;
                        case DISCONNECTED:
                            binding.sensorStatusTv.setText(Html.fromHtml(getString(R.string.sensor_disconnected)));
                            binding.sensorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_red_dot),null);
                            Log.i(TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };



    public void connectDevice() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH_CONNECT,BLUETOOTH_SCAN}, 2);
                return;
            }
        } else{
            Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
            startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
        }

    }


    /**
     * Get the result from the paired devices dialog
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER) {
            if (resultCode == Activity.RESULT_OK) {
                btManager.disconnectAllDevices();   //Disconnect all devices first
                shimmerDevice = null;
                //Get the Bluetooth mac address of the selected device:
                shimmerBtAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                btManager.connectShimmerThroughBTAddress(shimmerBtAdd);   //Connect to the selected device
                binding.sensorStatusTv.setText(Html.fromHtml(getString(R.string.sensor_connected)));
                binding.sensorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_green_dot),null);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private boolean detectMuscleFatigue(List<Double> data) {
//        // Simulated fatigue detection based on amplitude variation
//        // Calculate amplitude change rate
//
//        double avgAmplitudeChange = 0;
//        for (int i = 1; i < data.size(); i++) {
//            double amplitudeChange = Math.abs(data.get(i) - data.get(i - 1));
//            avgAmplitudeChange += amplitudeChange;
//        }
//        avgAmplitudeChange /= (data.size() - 1);
//
//        // Check for fatigue
//        return avgAmplitudeChange > calculateBaseline(data);
//    }
//    private double calculateBaseline(List<Double> emgData) {
//        double sum = 0;
//        int windowSize = Math.min(5, emgData.size());
//
//        for (int i = 0; i < windowSize; i++) {
//            sum += emgData.get(i);
//        }
//
//        if(sum!=0){
//            return (sum / windowSize)+10;
//        }
//        else
//        {
//            return 0;
//        }
//
//    }
private boolean detectMuscleFatigue(List<Double> data) {
    // Simulated fatigue detection based on amplitude variation
    // Calculate amplitude change rate
    double avgAmplitudeChange = 0;
    for (int i = 1; i < data.size(); i++) {
        double amplitudeChange = Math.abs(data.get(i) - data.get(i - 1));
        avgAmplitudeChange += amplitudeChange;
    }
    avgAmplitudeChange /= (data.size() - 1);

    // Check for fatigue
    return avgAmplitudeChange > calculateBaseline(data);
}

    private double calculateBaseline(List<Double> emgData) {
        double sum = 0;
        int windowSize = Math.min(5, emgData.size());

        for (int i = 0; i < windowSize; i++) {
            sum += emgData.get(i);
        }

        if (sum != 0) {
            return (sum / windowSize) +35; // Adjusted threshold
        } else {
            return 0;
        }
    }

    // Additional method for noise cancellation
    private List<Double> applyNoiseCancellation(List<Double> data) {
        // Implement noise cancellation technique (e.g., moving average)
        // Here's a simple example using a moving average with a window size of 3
        List<Double> filteredData = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - 1); j <= Math.min(data.size() - 1, i + 1); j++) {
                sum += data.get(j);
                count++;
            }
            double average = sum / count;
            filteredData.add(average);
        }
        return filteredData;
    }

    // Method to process real-time EMG data
    public void processRealTimeEMG(List<Double> realTimeData) {
        List<Double> filteredData = applyNoiseCancellation(realTimeData);
        boolean isFatigued = detectMuscleFatigue(filteredData);
        if (isFatigued) {
            // Notify about muscle fatigue
            System.out.println("Muscle fatigue detected!");
        }
    }







    private void plotData(List<Double> data, boolean fatigueDetected) {

        List<Entry> entries = new ArrayList<>();
        int startIndex = Math.max(0, data.size() - 500); // Starting index for the sliding window

        for (int i = startIndex; i < data.size(); i++) {
            entries.add(new Entry(i - startIndex, data.get(i).floatValue())); // Adjust index for sliding window
        }

        LineDataSet dataSet = new LineDataSet(entries, "Muscle Signal");
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.RED);

        LineData lineData = new LineData(dataSet);
        binding.lineChart.setData(lineData);

        if (fatigueDetected) {
            binding.fatigueDetectTv.setVisibility(View.VISIBLE);
            Description description = new Description();
            description.setText("Fatigue Detected");
            description.setTextSize(12f);
            description.setTextColor(Color.RED);
            description.setPosition(0f, 0f);
            binding.lineChart.setDescription(description);
            stopStreaming();
        }else{
            binding.fatigueDetectTv.setVisibility(View.GONE);
        }

        binding.lineChart.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, save the file
                saveCSVFile();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied! Cannot save csv file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bluetoothOn(){
        if (!isBluetoothEnabled()) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_PERMISSION_REQUEST);
            } else {
                enableBluetooth();
            }
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_PERMISSION_REQUEST);
        }
    }

    // Sets the NUmber of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view.
    private void runTimer() {

        // Creates a new Handler
        timeHandler = new Handler();

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        timeHandler.post(new Runnable() {
            @Override

            public void run() {


                // Set the text view text.
                binding.timeCounterTv.setText(TimestampUtil.getMillisToClockTimeString(millis));

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    millis += 1000L;
                }

                // Post the code again
                // with a delay of 1 second.
                if (timeHandler != null)
                    timeHandler.postDelayed(this, 1000);
            }
        });
    }

    private void checkPermissionForSaveFile(){
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                // Permission already granted, save the file
                saveCSVFile();
            }
        } else {
            // No need to request permission, save the file
            saveCSVFile();
        }
    }


    private void saveCSVFile() {

        if (allEMGDataList == null || allEMGDataList.size() == 0){
            Toast.makeText(this, "No streaming data for save", Toast.LENGTH_SHORT).show();
            return;
        }


        // Create a directory if it doesn't exist
        File directory = new File(getInternalStorageDirectoryPath(this)+"/"+Environment.DIRECTORY_DOWNLOADS, "MusclesFatigueCSVs");
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }

        // Create the file
        String fileName = "EMG_MUSCLES_DATA_" + new SimpleDateFormat("dd-MM-yy_HHmm", Locale.US).format(new Date()) + ".csv";
        File file = new File(directory, fileName);

        try {
            // Write data to the file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("EMG_CH1_16BIT_CAL".getBytes());
            fos.write("\n".getBytes());
            for (double data: allEMGDataList) {
                Log.d(TAG, "saveCSVFile: "+data);
                fos.write(formatDouble(data).getBytes());
                fos.write("\n".getBytes());
            }
            fos.close();
            Toast.makeText(this, "File saved to Downloads/MusclesFatigueCSVs/"+fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file!", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("#.###"); // Adjust the format as needed
        return df.format(value);
    }

    public static String getInternalStorageDirectoryPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            return storageManager.getPrimaryStorageVolume().getDirectory().getAbsolutePath();
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }




}