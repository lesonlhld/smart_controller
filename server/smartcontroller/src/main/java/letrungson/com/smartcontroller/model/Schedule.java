package letrungson.com.smartcontroller.model;

import java.util.List;

public class Schedule {
    private String repeatDay;
    private String temp;
    private String humid;
    private String startTime;
    private String endTime;
    private String scheduleId;
    private String roomId;
    private String state;
    private List<String> listDevice;

    public Schedule() {
        this.temp = "25";
        this.humid = "60";
        this.startTime = "00:00";
        this.endTime = "00:00";
        this.repeatDay = "0000000";
        this.roomId = "";
        this.state = "0";
        this.listDevice = null;
    }

    public Schedule(String temp, String humid, String startTime, String endTime, String repeatDay, List<String> listDevice) {
        this.temp = temp;
        this.humid = humid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatDay = repeatDay;
        this.listDevice = listDevice;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumid() {
        return humid;
    }

    public void setHumid(String humid) {
        this.humid = humid;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRepeatDay() {
        return this.repeatDay;
    }

    public void setRepeatDay(String repeatDay) {
        this.repeatDay = repeatDay;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getListDevice() {
        return listDevice;
    }

    public void setListDevice(List<String> listDevice) {
        this.listDevice = listDevice;
    }
}
