package com.example.childhealthcareproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BLEScanActivity extends AppCompatActivity {
    private static final String TAG = "BLEScanActivity";
    private Button scanBLEbutton;
    private int color_green, color_red;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000; //ms
    private ArrayList<BluetoothDevice> mLeDevices;
    private LeDeviceRecyclerAdapter mLeDeviceRecyclerAdapter;
    private RecyclerView mRecyclerView;

    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_BLE_PERMISSION = 1;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_scan);

        mHandler = new Handler();

        // Initialize scanBLEbutton
        scanBLEbutton = findViewById(R.id.scanButton);
        // Initialize color for buttons
        color_green = ContextCompat.getColor(this, R.color.green);
        ;
        color_red = Color.parseColor("#E11C24");
        // Set an OnClickListener to the button
        scanBLEbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartScanning();
            }
        });

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize mBluetoothLeScanner
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Check permissions
        permissionCheck();

        // Check if there's a saved instance state
        if (savedInstanceState != null) {
            connected = savedInstanceState.getBoolean("connected");
            mScanning = savedInstanceState.getBoolean("mScanning");

        } else {
            connected = false;
            mScanning = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("connected", connected);
        outState.putBoolean("mScanning", mScanning);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize mLeDevices and mLeDeviceRecyclerAdapter
        mLeDevices = new ArrayList<>();
        mLeDeviceRecyclerAdapter = new LeDeviceRecyclerAdapter(mLeDevices);

        // Find and set up your RecyclerView
        mRecyclerView = findViewById(R.id.device_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mLeDeviceRecyclerAdapter);
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        if (mBluetoothLeScanner != null) {
            Log.d(TAG, "scanLeDevice: mScanning=" + mScanning);
            if (!mScanning) {
                // Stops scanning after a predefined scan period.
                mHandler.postDelayed(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        mScanning = false;
                        permissionCheck();
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        scanBLEbutton.setText("Restart scanning");
                        scanBLEbutton.setBackgroundColor(color_green);
                        Log.d(TAG, "scanLeDevice: run: stopScan");
                    }
                }, SCAN_PERIOD);

                mScanning = true;

                permissionCheck();

                mBluetoothLeScanner.startScan(mLeScanCallback);
                Log.d(TAG, "scanLeDevice: startScan");

            } else {
                mScanning = false;
                mBluetoothLeScanner.stopScan(mLeScanCallback);
                scanBLEbutton.setText("Restart scanning");
                scanBLEbutton.setBackgroundColor(color_green);
                Log.d(TAG, "scanLeDevice: stopScan");
            }
        } else {
            Log.d(TAG, "scanLeDevice: mBluetoothLeScanner is NULL");
        }
    }

    private void restartScanning() {
        // Clear the list of devices
        if (mLeDevices != null) {
            mLeDevices.clear();
        }
        if (mLeDeviceRecyclerAdapter != null) {
            mLeDeviceRecyclerAdapter.notifyDataSetChanged();
        }

        disconnectBLE();

        // Restart scanning
        scanLeDevice();

        // Set the button
        scanBLEbutton.setText("Scanning");
        scanBLEbutton.setBackgroundColor(color_red);
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.d(TAG, "onScanResult: start");
                    super.onScanResult(callbackType, result);
                    mLeDeviceRecyclerAdapter.addDevice(result.getDevice());
                    mLeDeviceRecyclerAdapter.notifyDataSetChanged();
                }
            };


    public class LeDeviceRecyclerAdapter extends RecyclerView.Adapter<LeDeviceRecyclerAdapter.ViewHolder> {
        private ArrayList<BluetoothDevice> leDeviceList;

        public LeDeviceRecyclerAdapter(ArrayList<BluetoothDevice> leDevices) {
            leDeviceList = leDevices;
            Log.d(TAG, "LeDeviceRecyclerAdapter: done");
        }

        public void addDevice(BluetoothDevice device) {
            Log.d(TAG, "addDevice: start");
            if (!leDeviceList.contains(device)) {
                leDeviceList.add(device);
                notifyItemInserted(leDeviceList.size() - 1);
                Log.d(TAG, "addDevice: added");
            }
        }

        public BluetoothDevice getDevice(int position) {
            return leDeviceList.get(position);
        }

        public void clear() {
            leDeviceList.clear();
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: start");
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ble_devices, parent, false);
            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: called");
            BluetoothDevice device = leDeviceList.get(position);

            permissionCheck();
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                holder.deviceName.setText(deviceName);
                Log.d(TAG, "onBindViewHolder: add deviceName");
            } else {
                holder.deviceName.setText(R.string.unknown_device);
            }
            holder.deviceAddress.setText(device.getAddress());
            Log.d(TAG, "onBindViewHolder: add deviceAddress");
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount: " + leDeviceList.size());
            return leDeviceList.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView deviceName;
            TextView deviceAddress;

            public ViewHolder(View itemView) {
                super(itemView);
                deviceName = itemView.findViewById(R.id.device_name);
                deviceAddress = itemView.findViewById(R.id.device_address);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (deviceName.getCurrentTextColor() != color_green) {
                                deviceName.setTextColor(color_green); // Connect
                                BluetoothDevice device = leDeviceList.get(position);
                                String deviceAddress = device.getAddress();

                                // Call the connect method in MainActivity
                                ((BLEScanActivity) v.getContext()).connectBLE(deviceAddress);
                                permissionCheck();
                                Log.d(TAG, "onClick: connected to " + device.getName() + " with address: " + deviceAddress);
                            } else {
                                deviceName.setTextColor(Color.BLACK); // Disconnect
                                ((BLEScanActivity) v.getContext()).disconnectBLE();
                            }
                        }
                    }
                });
            }
        }
    }

    @SuppressLint("MissingPermission")
    private boolean connectBLE(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        try {
            if(!connected){
                permissionCheck();

                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                Toast.makeText(BLEScanActivity.this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();

//                Intent bleDeviceIntent = new Intent(this, ParentActivity.class);
                Intent bleDeviceIntent = new Intent(this, ParentTempActivity.class);
                bleDeviceIntent.putExtra("BleDevice", device);
                startActivity(bleDeviceIntent);
                finish();
                Log.d(TAG, "connectBLE: start ParentActivity");

                connected = true;
            }
            return true;

        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.  Unable to connect.");
            return false;
        }
    }

    private void disconnectBLE(){
        permissionCheck();
        stopService(new Intent( this, BLEDataService.class ) );
        connected = false;
    }

    private void permissionCheck(){
        if (ActivityCompat.checkSelfPermission(BLEScanActivity.this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it from the user.
            ActivityCompat.requestPermissions(BLEScanActivity.this, new String[]{android.Manifest.permission.BLUETOOTH}, REQUEST_BLE_PERMISSION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(BLEScanActivity.this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it from the user.
                ActivityCompat.requestPermissions(BLEScanActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLE_PERMISSION);
            }

            if (ActivityCompat.checkSelfPermission(BLEScanActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it from the user.
                ActivityCompat.requestPermissions(BLEScanActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLE_PERMISSION);
            }

            if (ActivityCompat.checkSelfPermission(BLEScanActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it from the user.
                ActivityCompat.requestPermissions(BLEScanActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLE_PERMISSION);
            }
        }

        if (ActivityCompat.checkSelfPermission(BLEScanActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it from the user.
            ActivityCompat.requestPermissions(BLEScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLE_PERMISSION);
        }
    }
}
