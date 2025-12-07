package com.example.childhealthcareproject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;
import java.util.UUID;

public class BLEDataService extends Service {
    private static final String TAG = "BLEDataService";
    private final String UUID_IMU_SERVICE = "a4e649f4-4be5-11e5-885d-feff819cdc9f";
    private final String UUID_ACC_CHARACTERISTIC = "c4c1f6e2-4be5-11e5-885d-feff819cdc9f";
    private final String UUID_THERMO_SERVICE = "00001809-0000-1000-8000-00805f9b34fb";
    private final String UUID_TEMP_CHARACTERISTIC = "00002a1c-0000-1000-8000-00805f9b34fb";
    private final String CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private BluetoothGatt bluetoothGattIMU, bluetoothGattThermo;
    private BluetoothGattCharacteristic accCharacteristic, tempCharacteristic;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange: STATE_CONNECTED");
                // Attempts to discover services after successful connection.
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect();
                Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTED");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered: start");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: BluetoothGatt.GATT_SUCCESS");

                if(gatt == bluetoothGattIMU){
                    Log.d(TAG, "onServicesDiscovered: bluetoothGattIMU");
                    List<BluetoothGattService> services = bluetoothGattIMU.getServices();
                    for (BluetoothGattService service : services) {
                        String serviceUUID = service.getUuid().toString();
                        Log.d(TAG, "Service discovered: " + serviceUUID);

                        if (serviceUUID.equals(UUID_IMU_SERVICE)) { // IMU Service
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                String characteristicUUID = characteristic.getUuid().toString();
                                Log.d(TAG, "Characteristic discovered: " + characteristicUUID);

                                if (characteristicUUID.equals(UUID_ACC_CHARACTERISTIC)) { // Acceleration characteristic
                                    Log.d(TAG, "onServicesDiscovered: Found acceleration characteristic!");
                                    accCharacteristic = characteristic;

                                    // Enable characteristic notification
                                    enableCharacteristicNotifications(bluetoothGattIMU, accCharacteristic);

                                    // Read characteristic
                                    bluetoothGattIMU.readCharacteristic(accCharacteristic);
                                    break;
                                }
                            }
                        }
                    }

                } else if (gatt == bluetoothGattThermo){
                    Log.d(TAG, "onServicesDiscovered: bluetoothGattThermo");
                    List<BluetoothGattService> services = bluetoothGattThermo.getServices();
                    for (BluetoothGattService service : services) {
                        String serviceUUID = service.getUuid().toString();
                        Log.d(TAG, "Service discovered: " + serviceUUID);

                        if (serviceUUID.equals(UUID_THERMO_SERVICE)) { // Health Thermometer Service
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                String characteristicUUID = characteristic.getUuid().toString();
                                Log.d(TAG, "Characteristic discovered: " + characteristicUUID);

                                if (characteristicUUID.equals(UUID_TEMP_CHARACTERISTIC)) { // Temperature Measurement characteristic
                                    Log.d(TAG, "onServicesDiscovered: Found Temperature Measurement characteristic!");
                                    tempCharacteristic = characteristic;

                                    // Enable characteristic notification
                                    enableCharacteristicIndication(bluetoothGattThermo, tempCharacteristic);

                                    // Read characteristic
                                    bluetoothGattThermo.readCharacteristic(tempCharacteristic);
                                    break;
                                }
                            }
                        }
                    }
                }


            } else {
                Log.e(TAG, "onServicesDiscovered received status: " + status);
                if(bluetoothGattIMU != null){
                    bluetoothGattIMU.close();
                }

                if(bluetoothGattThermo != null){
                    bluetoothGattThermo.close();
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();

            // Check the Android version before calling the appropriate method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 13 (or newer)
                onCharacteristicChanged_New(gatt, characteristic, value);
            } else {
                // For Android 12 (or older)
                onCharacteristicChanged_Old(gatt, characteristic);
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Descriptor write success
            } else {
                Log.e(TAG, "Descriptor write failure. Status: " + status);
            }
        }

    };

    // For Android 12 (or older)
    private void onCharacteristicChanged_Old(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        broadcastAcc(gatt, value);
    }

    // For Android 13 (or newer)
    private void onCharacteristicChanged_New(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        broadcastAcc(gatt, value);
    }

    private void broadcastAcc(BluetoothGatt gatt, byte[] value){
        String hexString = bytesToHexString(value);

        if(gatt == bluetoothGattIMU){
            // Reverse the byte order
            String reversedHexString = reverseByteOrder(hexString);

            // Extract digits
            String accXhexString = reversedHexString.substring(0, 4);
            String accYhexString = reversedHexString.substring(4, 8);
            String accZhexString = reversedHexString.substring(8, 12);

            // Convert extracted digits to decimal
            float[] acc_g = new float[3];
            acc_g[0] = Integer.parseInt(accXhexString, 16)/1000.0f;
            acc_g[1] = Integer.parseInt(accYhexString, 16)/1000.0f;
            acc_g[2] = Integer.parseInt(accZhexString, 16)/1000.0f;

            Log.d(TAG, "acc_g: " + hexString + " = " + acc_g[0] + ", " + acc_g[1] + ", " + acc_g[2]);

            // Broadcast acceleration data
            Intent intent = new Intent("ble-acc-update");
            intent.putExtra("acc_data", acc_g);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } else if (gatt == bluetoothGattThermo){
            // Extract digits at positions 3 to 6
            String extractedDigits = hexString.substring(2, 6);

            // Reverse the byte order
            String reversedHexString = reverseByteOrder(extractedDigits);

            // Convert extracted digits to decimal
            float temp = Integer.parseInt(reversedHexString, 16)/1000.0f;


            Log.d(TAG, "HEX: " + hexString + ", temp_acc: " + temp);

            // Broadcast acceleration data
            Intent intent = new Intent("ble-temp_acc-update");
            intent.putExtra("temp_data", temp);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @SuppressLint("MissingPermission")
    private void enableCharacteristicNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null || characteristic == null) {
            Log.e(TAG, "enableCharacteristicNotifications: BluetoothGatt or characteristic is null");
            return;
        }

        if (!gatt.setCharacteristicNotification(characteristic, true)) {
            Log.e(TAG, "enableCharacteristicNotifications: Failed to set characteristic notification");
            return;
        }

        // Get the descriptor for the characteristic
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID));

        if (descriptor != null) {
            // Set the value of the descriptor to enable notifications
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            // Write the descriptor to the GATT server
            if (!gatt.writeDescriptor(descriptor)) {
                Log.e(TAG, "enableCharacteristicNotifications: Failed to write descriptor");
            }
        } else {
            Log.e(TAG, "enableCharacteristicNotifications: Descriptor is null");
        }
    }

    @SuppressLint("MissingPermission")
    private void enableCharacteristicIndication(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null || characteristic == null) {
            Log.e(TAG, "enableCharacteristicNotifications: BluetoothGatt or characteristic is null");
            return;
        }

        if (!gatt.setCharacteristicNotification(characteristic, true)) {
            Log.e(TAG, "enableCharacteristicNotifications: Failed to set characteristic notification");
            return;
        }

        // Get the descriptor for the characteristic
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID));

        if (descriptor != null) {
            // Set the value of the descriptor to enable notifications
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

            // Write the descriptor to the GATT server
            if (!gatt.writeDescriptor(descriptor)) {
                Log.e(TAG, "enableCharacteristicNotifications: Failed to write descriptor");
            }
        } else {
            Log.e(TAG, "enableCharacteristicNotifications: Descriptor is null");
        }
    }

    @SuppressLint("MissingPermission")
    private void disableCharacteristicNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null || characteristic == null) {
            Log.e(TAG, "disableCharacteristicNotifications: BluetoothGatt or characteristic is null");
            return;
        }

        // Disable notifications for the characteristic
        if (!gatt.setCharacteristicNotification(characteristic, false)) {
            Log.e(TAG, "disableCharacteristicNotifications: Failed to set characteristic notification");
            return;
        }

        // Get the descriptor for the characteristic
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID));

        if (descriptor != null) {
            // Set the value of the descriptor to disable notifications
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

            // Write the descriptor to the GATT server
            if (!gatt.writeDescriptor(descriptor)) {
                Log.e(TAG, "disableCharacteristicNotifications: Failed to write descriptor");
            }
        } else {
            Log.e(TAG, "disableCharacteristicNotifications: Descriptor is null");
        }
    }

    private static int[] byteArrayToAcceleration(byte[] byteArray) {
        int[] acceleration = new int[3];

        // Assuming each component is represented as 16-bit signed integer
        acceleration[0] = byteArrayToDecimal(byteArray[0], byteArray[1]);
        acceleration[1] = byteArrayToDecimal(byteArray[2], byteArray[3]);
        acceleration[2] = byteArrayToDecimal(byteArray[4], byteArray[5]);

        return acceleration;
    }


    private static int byteArrayToDecimal(byte highByte, byte lowByte) {
        // Convert two bytes to a 16-bit signed integer
        return (highByte << 8) | (lowByte & 0xFF);
    }


    private static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((intent != null) && (intent.hasExtra("device"))) {
            // Get BLE device
            BluetoothDevice bleDevice = intent.getParcelableExtra("device");

            disconnect(); // In case still connected to some BLE devices, disconnect it first

            // connect to the GATT server on the device
            bluetoothGattIMU = bleDevice.connectGatt(this, false, bluetoothGattCallback);
            bluetoothGattThermo = bleDevice.connectGatt(this, false, bluetoothGattCallback);
        }


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    private void disconnect() {
        // Disable notifications before disconnecting
        if (accCharacteristic != null) {
            disableCharacteristicNotifications(bluetoothGattIMU, accCharacteristic);
        }
        if (tempCharacteristic != null) {
            disableCharacteristicNotifications(bluetoothGattThermo, tempCharacteristic);
        }

        // Disconnect bluetooth GATT
        if (bluetoothGattIMU != null) {
            // disconnect from the GATT Server
            bluetoothGattIMU.disconnect();
            bluetoothGattIMU.close();
            bluetoothGattIMU = null;
            Log.d(TAG, "disconnected IMU GATT");
        }

        if (bluetoothGattThermo != null) {
            // disconnect from the GATT Server
            bluetoothGattThermo.disconnect();
            bluetoothGattThermo.close();
            bluetoothGattThermo = null;
            Log.d(TAG, "disconnected Thermo GATT");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        stopSelf();
    }

    private static String reverseByteOrder(String hexString) {
        StringBuilder reversed = new StringBuilder();
        for (int i = hexString.length() - 2; i >= 0; i -= 2) {
            reversed.append(hexString, i, i + 2);
        }
        return reversed.toString();
    }
}
