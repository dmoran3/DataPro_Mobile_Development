package com.example.bluetest.ui.search;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bluetest.R;

import java.util.Set;
import java.util.Vector;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private BluetoothAdapter mBlueAdapter;
    Button scanButton;
    ListView bluetoothList;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
        scanButton = root.findViewById(R.id.scan_button);
        bluetoothList = root.findViewById(R.id.bluethoothlist);
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        exeButton();

//        searchViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }

    private void exeButton(){
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set<BluetoothDevice> bt = mBlueAdapter.getBondedDevices();
                BluetoothLeScanner bt = mBlueAdapter.getBluetoothLeScanner();
                ScanFilter.Builder builder = new ScanFilter.Builder();
                Vector<ScanFilter> filter = new Vector<ScanFilter>();
                filter.add(builder.build());
                ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
                builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
                builderScanSettings.setReportDelay(0);
                bt.startScan(filter, builderScanSettings.build(), scannerCallback);
                String[] bluetoothDeviceList = new String[bt.size()];
                int index = 0;
                if(bt.size() > 0){
                    for(BluetoothDevice device:bt){
                        bluetoothDeviceList[index] = device.getName();
                        index++;
                        scanButton.setText("Scanning...");
                    }
                    //stop scan
                    //scanButton.setText("Scan For Tape")
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, bluetoothDeviceList);
                    bluetoothList.setAdapter(arrayAdapter);
                }
            }
        });
    }
}
