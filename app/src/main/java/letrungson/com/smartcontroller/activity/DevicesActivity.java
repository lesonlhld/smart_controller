package letrungson.com.smartcontroller.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.service.Database;

public class DevicesActivity extends AppCompatActivity {
    private ListView listViewDevices;
    private Spinner spinnerDeviceType;
    private DatabaseReference dbRefDevices;
    private ArrayList<DeviceAdapter> deviceAdapterArrayList;
    private List<String> type;
    private String roomId;
    private ChildEventListener devicesDatabaseListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        //Setup database
        dbRefDevices = FirebaseDatabase.getInstance().getReference("devices");

        //Setup Toolbar
        Toolbar toolbar = findViewById(R.id.devices_toolbar);
        toolbar.setTitle("Devices");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Setup Room ID
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        //Set up Button Remove
//        ClipData.Item btn_remove= findViewById(R.menu.menu_devices);
        //Set up Button Add
        Button btn_add = findViewById(R.id.btn_add_devices);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DevicesActivity.this, AddDevicesActivity.class);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
            }
        });

        //Setup List View
        type = SplashActivity.typeDevices;
        deviceAdapterArrayList = new ArrayList<DeviceAdapter>();
        for (String ty : type) {
            deviceAdapterArrayList.add(new DeviceAdapter(DevicesActivity.this, R.layout.list_devices_item, new ArrayList<Device>()));
            if (ty.equals("Sensor")) {
                deviceAdapterArrayList.get(deviceAdapterArrayList.size() - 1).setSensor(true);
            }
        }
        listViewDevices = findViewById(R.id.list_devices);

        //Setup spinner
        spinnerDeviceType = findViewById(R.id.spinner_devices);
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(DevicesActivity.this, R.layout.spinner_item, type);
        spinnerDeviceType.setAdapter(spinnerAdapter);
        spinnerDeviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listViewDevices.setAdapter(deviceAdapterArrayList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Setup database listener
        devicesDatabaseListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                device.setDeviceId(snapshot.getKey());
                boolean is_sensor = device.getType().equals("Sensor");
                int currentType = 0;
                if (!is_sensor) deviceAdapterArrayList.get(0).add(device);
                for (int i = 1; i < type.size(); i++) {
                    if (type.get(i).equals(device.getType())) {
                        deviceAdapterArrayList.get(i).add(device);
                        currentType = i;
                        break;
                    }
                }
                int currentViewPos = spinnerDeviceType.getSelectedItemPosition();
                if ((currentViewPos == currentType) || (currentViewPos == 0 && !is_sensor)) {
                    listViewDevices.setAdapter(deviceAdapterArrayList.get(currentViewPos));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                boolean is_sensor = device.getType().equals("Sensor");
                int currentViewPos = spinnerDeviceType.getSelectedItemPosition();
                int currentType = 0;
                for (int i = 1; i < type.size(); i++) {
                    if (type.get(i).equals(device.getType())) {
                        currentType = i;
                        break;
                    }
                }
                if ((currentViewPos == 0 && !is_sensor) || (currentViewPos == currentType)) {
                    int j;
                    DeviceAdapter deviceAdapter_current = deviceAdapterArrayList.get(currentViewPos);
                    for (j = 0; j < deviceAdapter_current.getCount(); j++) {
                        if (deviceAdapter_current.getItem(j).getDeviceId().equals(deviceID)) {
                            deviceAdapter_current.getItem(j).assign(device);
                            break;
                        }
                    }

                    if (j < deviceAdapter_current.getCount()) {
                        View convertView = getViewByPosition(j, listViewDevices);
                        SwitchCompat switchCompat = (SwitchCompat) convertView.findViewById(R.id.device_item_switch);
                        TextView textView = (TextView) convertView.findViewById(R.id.device_item_title);
                        textView.setText(device.getDeviceName());
                        switchCompat.setChecked(device.getState().equals("1"));
                    }
                } else {
                    DeviceAdapter deviceAdapter_current = deviceAdapterArrayList.get(currentType);
                    for (int j = 0; j < deviceAdapter_current.getCount(); j++) {
                        if (deviceAdapter_current.getItem(j).getDeviceId().equals(deviceID))
                            deviceAdapter_current.getItem(j).assign(device);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                boolean is_sensor = device.getType().equals("Sensor");
                int currentType = 0;
                for (int i = 1; i < type.size(); i++) {
                    if (type.get(i).equals(device.getType())) {
                        currentType = i;
                        break;
                    }
                }
                DeviceAdapter deviceAdapter_current = deviceAdapterArrayList.get(currentType);
                int j;
                for (j = 0; j < deviceAdapter_current.getCount(); j++) {
                    if (deviceAdapter_current.getItem(j).getDeviceId().equals(deviceID)) {
                        Device del_device = deviceAdapter_current.getItem(j);
                        deviceAdapter_current.remove(del_device);
                        deviceAdapterArrayList.get(0).remove(del_device);
                        break;
                    }
                }
                int currentViewPos = spinnerDeviceType.getSelectedItemPosition();
                if (currentViewPos == currentType) {
                    listViewDevices.setAdapter(deviceAdapter_current);
                } else if ((currentViewPos == 0 && !is_sensor)) {
                    listViewDevices.setAdapter(deviceAdapterArrayList.get(0));
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        dbRefDevices.orderByChild("roomId").equalTo(roomId).addChildEventListener(devicesDatabaseListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbRefDevices.orderByChild("roomId").equalTo(roomId).removeEventListener(devicesDatabaseListener);
        for (int i = 0; i < deviceAdapterArrayList.size(); i++) {
            deviceAdapterArrayList.get(i).clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.btn_remove) {
            if (item.getIcon().getConstantState() == ContextCompat.getDrawable(this, R.drawable.ic_trash).getConstantState()) {
                for (int i = 0; i < deviceAdapterArrayList.size(); i++) {
                    deviceAdapterArrayList.get(i).setEditModeEnabled(true);
                }
                deviceAdapterArrayList.get(spinnerDeviceType.getSelectedItemPosition()).notifyDataSetChanged();
                item.setIcon(R.drawable.ic_rep_arrow);

            } else {
                for (int i = 0; i < deviceAdapterArrayList.size(); i++) {
                    deviceAdapterArrayList.get(i).setEditModeEnabled(false);
                }
                deviceAdapterArrayList.get(spinnerDeviceType.getSelectedItemPosition()).notifyDataSetChanged();
                item.setIcon(R.drawable.ic_trash);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private class DeviceAdapter extends ArrayAdapter<Device> {
        private int layout;
        private boolean isEditModeEnabled = false;
        private boolean isSensor = false;

        public DeviceAdapter(Context context, int resource, ArrayList<Device> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
            }
            Device device = getItem(position);
            DeviceHolder deviceHolder = new DeviceHolder();
            deviceHolder.title = (TextView) convertView.findViewById(R.id.device_item_title);
            deviceHolder.title.setText(device.getDeviceName());
            deviceHolder.switchCompat = (SwitchCompat) convertView.findViewById(R.id.device_item_switch);
            deviceHolder.imageView = (ImageView) convertView.findViewById(R.id.device_item_x);

            if (isEditModeEnabled) {
                deviceHolder.switchCompat.setVisibility(View.INVISIBLE);
                deviceHolder.imageView.setVisibility(View.VISIBLE);
                deviceHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialog = new AlertDialog.Builder(DevicesActivity.this).create();
                        alertDialog.setIcon(R.drawable.ic_alert);
                        alertDialog.setTitle("Remove device: " + device.getDeviceName());
                        alertDialog.setMessage("Are you sure?");

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Database.removeDevice(device.getDeviceId());
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                        layoutParams.weight = 10;
                        btnPositive.setLayoutParams(layoutParams);
                        btnNegative.setLayoutParams(layoutParams);
                    }
                });
            } else {
                deviceHolder.imageView.setVisibility(View.INVISIBLE);
                if (!isSensor) {
                    deviceHolder.switchCompat.setVisibility(View.VISIBLE);
                    deviceHolder.switchCompat.setChecked(device.getState().equals("1"));
                    deviceHolder.switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (!buttonView.isPressed()) {
                                return;
                            }
                            String state = "0";
                            if (isChecked) {
                                state = "1";
                            }
                            Database.updateDevice(device.getDeviceId(), state);
                            Database.addLog(device.getDeviceId(), state);
                        }
                    });
                } else {
                    deviceHolder.switchCompat.setVisibility(View.INVISIBLE);
                }

            }
            convertView.setTag(deviceHolder);

            return convertView;
        }

        public void setSensor(boolean isSensor) {
            this.isSensor = isSensor;
        }

        public void setEditModeEnabled(boolean isEditModeEnabled) {
            this.isEditModeEnabled = isEditModeEnabled;
        }
    }

    public class DeviceHolder {
        TextView title;
        SwitchCompat switchCompat;
        ImageView imageView;
    }


    //Spinner Adapter
    private class SpinnerAdapter extends ArrayAdapter<String> {
        private int layout;

        public SpinnerAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerViewHolder mainSpinnerViewholder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                SpinnerViewHolder spinnerViewHolder = new SpinnerViewHolder();
                String title = getItem(position);
                spinnerViewHolder.title = (TextView) convertView.findViewById(R.id.spinner_title);
                spinnerViewHolder.title.setText(title);
                spinnerViewHolder.imageView = (ImageView) convertView.findViewById(R.id.spinner_image);
                switch (position) {
                    case 0:
                        spinnerViewHolder.imageView.setImageResource(R.drawable.ic_baseline_clear_all_24);
                        break;
                    case 1:
                        spinnerViewHolder.imageView.setImageResource(R.drawable.air_conditioner);
                        break;
                    case 2:
                        spinnerViewHolder.imageView.setImageResource(R.drawable.fan);
                        break;
                    case 3:
                        spinnerViewHolder.imageView.setImageResource(R.drawable.ic_baseline_whatshot_24);
                        break;
                    case 4:
                        spinnerViewHolder.imageView.setImageResource(R.drawable.ic_sensor);
                        break;
                    default:
                        spinnerViewHolder.imageView.setImageResource(R.drawable.ic_baseline_device_unknown_24);
                        break;
                }
                convertView.setTag(spinnerViewHolder);
            } else {
                mainSpinnerViewholder = (SpinnerViewHolder) convertView.getTag();
            }
            return convertView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    public class SpinnerViewHolder {
        ImageView imageView;
        TextView title;
    }
}
