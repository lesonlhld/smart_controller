package letrungson.com.smartcontroller.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class AddDevicesActivity extends AppCompatActivity {
    private ArrayList<Device> arrayListDevice;
    private EditText textDeviceName;
    private EditText textDeviceId;
    private FirebaseDatabase db;
    private DatabaseReference dbRefDevices;
    private Spinner spinnerAddDevice;
    private Button buttonAddDevices;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_devices);

        db = FirebaseDatabase.getInstance();
        dbRefDevices = db.getReference("devices");
        //Setup List device name
        arrayListDevice = new ArrayList<Device>();
        //Get intent
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");

        //Setup DeviceName EditText
        textDeviceName = findViewById(R.id.edit_text_device_name);
        textDeviceId = findViewById(R.id.edit_text_device_id);

        //Setup DeviceType Spinner
        spinnerAddDevice = findViewById(R.id.spinner_add_devices);

        List<String> type = SplashActivity.typeDevices;
        type.set(0, "Select a device type");
        SpinnerAddDeviceAdapter spinnerAddDeviceAdapter = new SpinnerAddDeviceAdapter(this, R.layout.spinner_add_device_item, type);
        spinnerAddDeviceAdapter.setDropDownViewResource(R.layout.spinner_add_device_item);
        spinnerAddDevice.setAdapter(spinnerAddDeviceAdapter);
        spinnerAddDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
//                    Toast.makeText
//                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
//                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Setup Add button
        buttonAddDevices = (Button) findViewById(R.id.btn_add_devices2);
        buttonAddDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateDeviceId() && validateDeviceName() && validateDeviceType()) {
                    String deviceId = textDeviceId.getText().toString().trim();
                    String deviceName = textDeviceName.getText().toString().trim();
                    String type = spinnerAddDevice.getSelectedItem().toString().trim();
                    Database.addDevice(deviceId, deviceName, type, roomId);
                    textDeviceId.setText("");
                    textDeviceName.setText("");
                    spinnerAddDevice.setAdapter(spinnerAddDeviceAdapter);
                    Toast.makeText(getApplicationContext(), "Device has been added to your room", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Setup GO BACK textview
        TextView textViewGoBack = findViewById(R.id.text_view_go_back);
        textViewGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Setup database device name listener
        dbRefDevices.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                device.setDeviceId(snapshot.getKey());
                arrayListDevice.add(device);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                for (Device device0 : arrayListDevice) {
                    if (device0.getDeviceId().equals(deviceID)) {
                        device0.assign(device);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                for (Device device0 : arrayListDevice) {
                    if (device0.getDeviceId().equals(deviceID)) {
                        arrayListDevice.remove(device0);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateDeviceId() {
        String inputDeviceId = textDeviceId.getText().toString().trim();
        if (inputDeviceId.isEmpty()) {
            textDeviceId.setError("Field can't be empty");
            return false;
        } else if (!inputDeviceId.matches("[a-zA-Z0-9.-]*")) {
            textDeviceId.setError("Invalid format of ID");
            return false;
        } else if (isDeviceIdExist(inputDeviceId)) {
            textDeviceId.setError("This ID is already used");
            return false;
        }
        textDeviceId.setError(null);
        return true;
    }

    private boolean validateDeviceName() {
        String inputDeviceName = textDeviceName.getText().toString().trim();
        if (inputDeviceName.isEmpty()) {
            textDeviceName.setError("Field can't be empty");
            return false;
/*        } else if (!inputDeviceName.matches("[a-zA-Z0-9 -]*")) {
            textDeviceName.setError("Only contain letters, numbers and WS");
            return false;*/
        } else if (inputDeviceName.length() > 50) {
            textDeviceName.setError("Device name too long");
            return false;
        } else if (isDeviceNameExist(inputDeviceName)) {
            textDeviceName.setError("Name already exists for this room");
            return false;
        }
        textDeviceName.setError(null);
        return true;
    }

    private boolean isDeviceNameExist(String input) {
        for (Device device : arrayListDevice) {
            if (device.getRoomId() != null && device.getDeviceName() != null)
                if (device.getRoomId().equals(roomId))
                    if (device.getDeviceName().equals(input))
                        return true;
        }
        return false;
    }

    private boolean isDeviceIdExist(String input) {
        for (Device device : arrayListDevice) {
            if (device.getDeviceId().equals(input))
                return true;
        }
        return false;
    }

    private boolean validateDeviceType() {
        if (spinnerAddDevice.getSelectedItemPosition() == 0) {
            View convertView = spinnerAddDevice.getSelectedView();
            TextView title = convertView.findViewById(R.id.spinner_add_devices_title);
            title.setError("");
            title.setTextColor(Color.RED);//just to highlight that this is an error
            title.setText("Must pick a type");//changes the selected item text to this
            return false;
        }
        return true;
    }

    private class SpinnerAddDeviceAdapter extends ArrayAdapter<String> {
        private int layout;

        public SpinnerAddDeviceAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerAddDeviceHolder mainSpinnerViewholder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                SpinnerAddDeviceHolder spinnerAddDeviceHolder = new SpinnerAddDeviceHolder();
                String title = getItem(position);
                spinnerAddDeviceHolder.title = (TextView) convertView.findViewById(R.id.spinner_add_devices_title);
                spinnerAddDeviceHolder.title.setText(title);
                spinnerAddDeviceHolder.imageView = (ImageView) convertView.findViewById(R.id.spinner_add_devices_image);
                switch (position) {
                    case 0:
                        spinnerAddDeviceHolder.imageView.setImageResource(R.drawable.ic_baseline_device_unknown_24);
                        spinnerAddDeviceHolder.title.setTextColor(Color.rgb(156, 152, 152));
                        spinnerAddDeviceHolder.imageView.setColorFilter(Color.rgb(156, 152, 152));
                        break;
                    case 1:
                        spinnerAddDeviceHolder.imageView.setImageResource(R.drawable.air_conditioner);
                        break;
                    case 2:
                        spinnerAddDeviceHolder.imageView.setImageResource(R.drawable.fan);
                        break;
                    case 3:
                        spinnerAddDeviceHolder.imageView.setImageResource(R.drawable.ic_baseline_whatshot_24);
                        break;
                    case 4:
                        spinnerAddDeviceHolder.imageView.setImageResource(R.drawable.ic_sensor);
                        break;
                    default:
                        spinnerAddDeviceHolder.imageView.setImageResource(R.drawable.ic_baseline_device_unknown_24);
                        break;
                }
                convertView.setTag(spinnerAddDeviceHolder);
            } else {
                mainSpinnerViewholder = (SpinnerAddDeviceHolder) convertView.getTag();
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            if (position == 0) {
                // Disable the first item from Spinner
                // First item will be use for hint
                return false;
            } else {
                return true;
            }
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    public class SpinnerAddDeviceHolder {
        ImageView imageView;
        TextView title;
    }
}