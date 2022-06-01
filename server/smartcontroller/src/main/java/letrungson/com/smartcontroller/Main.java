package letrungson.com.smartcontroller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.service.MQTTService;
import letrungson.com.smartcontroller.util.Constant;

public class Main {
    public static final String DATABASE_URL = "https://smart-controller-ab851-default-rtdb.asia-southeast1.firebasedatabase.app";
    public static List<Device> allDevices;
    public static MQTTService mqttService;

    public static void main(String[] args) throws IOException, InterruptedException {
        // BasicConfigurator.configure();
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        FileInputStream serviceAccount = new FileInputStream(
                s + "\\src\\main\\java\\letrungson\\com\\smartcontroller\\serviceAccountKey.json");

//		URL url = Main.class.getResource("serviceAccountKey.json");
//		InputStream serviceAccount = new URL(url.toString()).openStream();
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(DATABASE_URL).build();

        FirebaseApp defaultApp = FirebaseApp.initializeApp(options);
        FirebaseDatabase defaultDatabase = FirebaseDatabase.getInstance(defaultApp);

        Constant.initServer();

        allDevices = new ArrayList<Device>();
        Query allDevice = defaultDatabase.getReference("devices");
        allDevice.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                // TODO Auto-generated method stub
                Device device = snapshot.getValue(Device.class);
                device.setDeviceId(snapshot.getKey());
                allDevices.add(device);
            }

            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                // TODO Auto-generated method stub
                Device device = snapshot.getValue(Device.class);
                String deviceId = snapshot.getKey();
                if (!device.getType().equals("Sensor")) {
                    for (Device device0 : allDevices) {
                        if (device0.getDeviceId().equals(deviceId) && !device.getState().equals(device0.getState())) {
                            device0.assign(device);
                            mqttService.sendDataMQTT(device.getServer(), deviceId, device.getState());
                            break;
                        }
                    }
                }
            }

            public void onChildRemoved(DataSnapshot snapshot) {
                // TODO Auto-generated method stub
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                for (Device device0 : allDevices) {
                    if (device0.getDeviceId().equals(deviceID)) {
                        allDevices.remove(device0);
                        break;
                    }
                }
            }

            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                // TODO Auto-generated method stub

            }

            public void onCancelled(DatabaseError error) {
                // TODO Auto-generated method stub

            }
        });

        mqttService = new MQTTService();
    }
}