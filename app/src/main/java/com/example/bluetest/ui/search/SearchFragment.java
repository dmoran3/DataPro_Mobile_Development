package com.example.bluetest.ui.search;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bluetest.R;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BLUETOOTH_SERVICE;
















//DELETE THIS
//  https://stackoverflow.com/questions/45935067/what-method-i-must-use-instead-of-devicelistadapter-in-android-studio
//














public class SearchFragment<DeviceListAdapter> extends Fragment {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    private SearchViewModel searchViewModel;
    private BluetoothAdapter mBluetoothAdapter;
    Button scanButton;
    Button enable_BT_Button;
    final public ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView bluetoothList;

    /*This receiver is used to enable bluetooth on a device with a button in the app. If bluetooth
    is already enabled, nothing will happen.  In later versions, we should add a message box that
    lets the user know BT is already enabled.  Perhaps also grey out the button if BT is enabled.
     */
    private final BroadcastReceiver mBroadcastReceiver_BT_Enable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("BT_ENABLE","Bluetooth Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("BT_ENABLE","Bluetooth Turning On");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("BT_ENABLE","Bluetooth On");
                        break;
                }
            }
        }
    };

    /*This receiver is used to enable connection and print useful information about connection
    status toi the log. I would like to have it display this stay often, maybe on every action after
    connection, or have some clear way to display the connection status on the app, so we can make
    sure the device is no losing connection at any point.
     */
    private final BroadcastReceiver mBroadcastRecevier_Connection_Status = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);
                switch(mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("CONNECTION_STATUS","Bluetooth CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("CONNECTION_STATUS","Bluetooth CONNECTABLE. Able to receive" +
                                " connections, not discoverable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("CONNECTION_STATUS","Bluetooth Discoverability Disabled");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("CONNECTION_STATUS","Bluetooth Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("CONNECTION_STATUS","Bluetooth Connected");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver_Device_Discovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevices.add(device);
                Log.d("RECEIVED",device.getName() + ":" + device.getAddress());

                mDeviceListAdapter = new DeviceListAdapter(context, R.id.bluethoothlist, mDevices);
                bluetoothList.setAdapter((ListAdapter) mDeviceListAdapter);


            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
        scanButton = root.findViewById(R.id.scan_button);
        bluetoothList = root.findViewById(R.id.bluethoothlist);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        enable_BT();
        enable_Connection();

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        exeButton();


        return root;
    }

    private void exeButton(){


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enable_discovery_scan();

            }
        });
    }

    public void enable_BT(){
        if(mBluetoothAdapter == null){
            Log.d("MSG","Bluetooth Cannot Be Enabled, Check Device");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enable_BT_Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enable_BT_Intent);

            IntentFilter BT_Intent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(mBroadcastReceiver_BT_Enable,BT_Intent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d("MSG","Bluetooth Enabled");
        }
    }

    public void enable_Connection(){
        Intent enable_connection_Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(enable_connection_Intent);

        IntentFilter Connection_Intent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        getActivity().registerReceiver(mBroadcastRecevier_Connection_Status,Connection_Intent);

    }

    public void enable_discovery_scan(){
        check_OS_Permissions();
        Log.d("SCAN","Starting Device Scan");

        mBluetoothAdapter.startDiscovery();

        IntentFilter discover_Devices_Intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver_Device_Discovery, discover_Devices_Intent);

    }

    public void check_OS_Permissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
            if( permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                Log.d("MSG","Permissions OK");

            }
        }
        else
        {
            Log.d("MSG","Permissions OK");
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                switch (requestCode){
                    case REQUEST_ENABLE_BT:
                        if (resultCode == RESULT_OK){
                            //bluetooth is on

                            Log.d("MSG","Bluetooth On");
                        }
                        else {
                            Log.d("ERROR","Couldn't turn on Bluetooth");
                        }
                        break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
