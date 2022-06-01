package letrungson.com.smartcontroller.tools;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.model.Room;
import letrungson.com.smartcontroller.model.Schedule;
import letrungson.com.smartcontroller.service.Database;

public class AutoSchedule {
    public static List<Schedule> listSchedule;
    public static List<Device> listDevice;
    public static Room room;
    public static String finalIsTurnDeviceOn;
    public static Schedule finalSchedule;

    public static void autoTurnOnOffDevicebySchedule(final String roomId) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query roomsQuery = database.getReference("rooms").child(roomId);
        roomsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot roomsSnapshot) {
                final Room room = roomsSnapshot.getValue(Room.class);
                room.setRoomId(roomId);
                // tạo list schedule trong phòng đó
                listSchedule = new ArrayList<Schedule>();
                Query schedulesQuery = database.getReference("schedules").orderByChild("roomId")
                        .equalTo(roomId);
                schedulesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot schedulesSnapshot) {
                        listSchedule.clear();
                        for (DataSnapshot scheduleData : schedulesSnapshot.getChildren()) {
                            Schedule schedule = scheduleData.getValue(Schedule.class);
                            if (schedule.getState().equals("1")) {
                                String scheduleId = scheduleData.getKey();
                                schedule.setScheduleId(scheduleId);
                                listSchedule.add(schedule);
                            }
                        }
                        // đã có list schedule, cần duyệt các schedule để tìm ra schedule
                        // tương ứng thời gian hiện tại, vì chỉ có 1 schedule như thế nên khi tìm ra,
                        // tính toán và tạo một biến boolean lưu bật hoặc tắt, sau đó break vòng duyệt
                        // schedule

                        // thiết lập các biến thời gian hiện tại
                        Calendar nowTime = Calendar.getInstance();
                        int dayofWeek = nowTime.get(Calendar.DAY_OF_WEEK);
                        // sửa lại dayofWeek cho đúng format (dayofWeek cũ thứ 2 = 2, dayofWeek mới thứ
                        // 2 = 0)
                        if (dayofWeek == 1)
                            dayofWeek = 8;
                        dayofWeek -= 2;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        String nowTimeHHmm = simpleDateFormat.format(nowTime.getTime());
                        int hour, minute;
                        hour = Integer.parseInt(nowTimeHHmm.substring(0, 2));
                        minute = Integer.parseInt(nowTimeHHmm.substring(3, 5));
                        String isTurnDeviceOn = "0";
                        // duyệt các schedule
                        boolean setTargetTemp = false;
                        finalSchedule = null; 
                        for (Schedule sche : listSchedule) {
                            if (sche.getRepeatDay().charAt(dayofWeek) == '1') {
                                int startHour, startMinute;
                                int endHour, endMinute;
                                startHour = Integer.parseInt(sche.getStartTime().substring(0, 2));
                                startMinute = Integer.parseInt(sche.getStartTime().substring(3, 5));
                                endHour = Integer.parseInt(sche.getEndTime().substring(0, 2));
                                endMinute = Integer.parseInt(sche.getEndTime().substring(3, 5));
                                boolean isRightTime = false;
                                if (startHour < hour && hour < endHour) {
                                    isRightTime = true;
                                } else if (startHour == hour && hour != endHour) {
                                    if (startMinute <= minute) {
                                        isRightTime = true;
                                    }
                                } else if (startHour != hour && hour == endHour) {
                                    if (minute <= endMinute) {
                                        isRightTime = true;
                                    }
                                } else if (startHour == hour && hour == endHour) {
                                    if (startMinute <= minute && minute <= endMinute) {
                                        isRightTime = true;
                                    }
                                }
                                if (isRightTime) {
                                    finalSchedule = sche;
                                    if(!sche.getTemp().equals("")) {
                                        // set target temp của phòng theo schedule
                                        if (!room.getRoomTargetTemp().equals(sche.getTemp())) {
                                            room.setRoomTargetTemp(sche.getTemp());
                                            database.getReference().child("rooms").child(room.getRoomId())
                                                    .child("roomTargetTemp")
                                                    .setValueAsync(sche.getTemp());
                                        }
                                        setTargetTemp = true;
                                        if (Integer.parseInt(room.getRoomTargetTemp()) < Integer
                                                .parseInt(room.getRoomCurrentTemp())) {
                                            isTurnDeviceOn = "1";
                                        }
                                    } else {
                                    	isTurnDeviceOn = "1";
                                    }
                                    System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Active Schedule: " + sche.getScheduleId());
                                    // vì đã đúng schedule nên sau khi lưu biến isTurnDeviceOn, break khỏi vòng lặp
                                    // schedule
                                    break;
                                }
                                // không break, duyệt tiếp những schedule còn lại
                            }
                        }
                        if (setTargetTemp == false) {
                            room.setRoomTargetTemp("");
                            database.getReference().child("rooms").child(room.getRoomId())
                                    .child("roomTargetTemp").setValueAsync("");
                        }
                        if (setTargetTemp == false && isTurnDeviceOn == "0") {
                            System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": No Schedule");                        	
                        }
                        // tạo list device trong phòng đó
                        listDevice = new ArrayList<Device>();
                        Query devicesQuery = database.getReference("devices").orderByChild("roomId")
                                .equalTo(room.getRoomId());
                        finalIsTurnDeviceOn = isTurnDeviceOn;
                        devicesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot devicesSnapshot) {
                                listDevice.clear();
                                for (DataSnapshot deviceData : devicesSnapshot.getChildren()) {
                                    String deviceId = deviceData.getKey();
                                    if (Check.checkExistDeviceId(finalSchedule.getListDevice(),
                                    deviceId)){
                                        Device device = deviceData.getValue(Device.class);
                                        device.setDeviceId(deviceId);
                                        listDevice.add(device);
                                    }
                                }
                                // đã có list device, duyệt list device, mỗi device thay đổi trên firebase và
                                // adafruit theo biến IsTurnDeviceOn đã lưu
                                for (Device device : listDevice) {
                                    if (device.getState().compareTo(finalIsTurnDeviceOn) != 0) {
                                        Database.updateDevice(device.getDeviceId(), finalIsTurnDeviceOn);
                                        Database.addLog(device.getDeviceId(), finalIsTurnDeviceOn);
                                    }
                                }
                            }

                            public void onCancelled(DatabaseError error) {
                            }
                        });
                    }

                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            public void onCancelled(DatabaseError error) {

            }
        });
    }
}
