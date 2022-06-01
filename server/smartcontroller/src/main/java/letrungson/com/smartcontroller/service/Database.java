package letrungson.com.smartcontroller.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import letrungson.com.smartcontroller.Main;
import letrungson.com.smartcontroller.model.Data;
import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.model.LogState;
import letrungson.com.smartcontroller.model.Value;
import letrungson.com.smartcontroller.tools.AutoSchedule;
import letrungson.com.smartcontroller.tools.Check;

import static letrungson.com.smartcontroller.tools.Transform.convertToCurrentTimeZone;

public class Database {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference sensors = database.getReference("sensors");
    private static final DatabaseReference logs = database.getReference("logs");
    private static final DatabaseReference devices = database.getReference("devices");
    ;
    private static final DatabaseReference rooms = database.getReference("rooms");

    public static void addSensorLog(String roomId, Data data, Value value) {
        String id = "Sensor" + sensors.push().getKey();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("roomId", roomId);
        hashMap.put("deviceId", data.getKey());
        hashMap.put("last_value", value.getData());
        hashMap.put("updated_at",
                convertToCurrentTimeZone(data.getUpdated_at().substring(0, data.getUpdated_at().length() - 4)));
        sensors.child(id).setValueAsync(hashMap);
    }

    public static void addLog(String deviceId, String newState) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String datetime = formatter.format(LocalDateTime.now());
        String userId = "System";
        LogState log = new LogState(datetime, deviceId, newState, userId);
        String id = "Log" + logs.push().getKey();
        logs.child(id).setValueAsync(log);
    }

    public static void addRoom(String roomName) {
        String id = "Room" + rooms.push().getKey();
        rooms.child(id).child("roomName").setValueAsync(roomName);
    }

    public static void updateRoom(String roomId, String temp, String humid) {
        rooms.child(roomId).child("roomCurrentTemp").setValueAsync(temp);
        rooms.child(roomId).child("roomCurrentHumidity").setValueAsync(humid);
    }

    public static void removeLog() {
        logs.removeValueAsync();
        System.out.println("removed successfully");
    }

    public static void addDevice(String deviceId, String deviceName, String type, String roomId) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("deviceName", deviceName);
        hashMap.put("roomId", roomId);
        if (type.equals("Sensor"))
            hashMap.put("state", "0-0");
        else
            hashMap.put("state", "0");
        hashMap.put("type", type);
        hashMap.put("server", Check.checkAndGetServerNameOfDevice(deviceId));
        devices.child(deviceId).setValueAsync(hashMap);
    }

    public static void removeDevice(String deviceId) {
        devices.child(deviceId).removeValueAsync();
    }

    public static void updateDevice(String deviceId, String currentState) {
        devices.child(deviceId).child("state").setValueAsync(currentState);
    }

    public static void processDataMQTT(final String deviceId, final Data dataMqtt, final Value value) {
        Query device = database.getReference("devices").child(deviceId);
        device.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Device cDevice = dataSnapshot.getValue(Device.class);
                if (cDevice.getType() != null) {
                    if (cDevice.getType().equals("Sensor")) {
                        String roomId = cDevice.getRoomId();
                        String data = value.getData();
                        String temp = data.substring(0, data.lastIndexOf('-')).trim();
                        String humid = data.substring(data.lastIndexOf('-') + 1).trim();
                        Database.addSensorLog(roomId, dataMqtt, value);
                        Database.updateRoom(roomId, temp, humid);
                        Database.updateDevice(deviceId, value.getData());
                        AutoSchedule.autoTurnOnOffDevicebySchedule(roomId);
                    } else {// Others devices
                        if (!cDevice.getState().equals(value.getData())) {
                            for (Device device0 : Main.allDevices) {
                                if (device0.getDeviceId().equals(deviceId)
                                        && cDevice.getState().equals(device0.getState())) {
                                    device0.setState(value.getData());
                                    break;
                                }
                            }
                            Database.updateDevice(deviceId, value.getData());
                        }
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
