package letrungson.com.smartcontroller.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId;
    private String roomName;
    private String roomState;
    private String roomTargetTemp;
    private String roomCurrentTemp;
    private String roomCurrentHumidity;

    public Room() {

    }

    public Room(String roomName, String roomState, String roomTargetTemp, String roomCurrentTemp,
                String roomCurrentHumidity) {
        this.roomName = roomName;
        this.roomState = roomState;
        this.roomTargetTemp = roomTargetTemp;
        this.roomCurrentTemp = roomCurrentTemp;
        this.roomCurrentHumidity = roomCurrentHumidity;
    }

    public static List<String> getAllRoomName(List<Room> lstRoom) {
        List<String> lstRoomName = new ArrayList<String>();
        for (Room room : lstRoom) {
            lstRoomName.add(room.getRoomName());
        }
        return lstRoomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomState() {
        return roomState;
    }

    public void setRoomState(String roomState) {
        this.roomState = roomState;
    }

    public String getRoomTargetTemp() {
        return roomTargetTemp;
    }

    public void setRoomTargetTemp(String roomTargetTemp) {
        this.roomTargetTemp = roomTargetTemp;
    }

    public String getRoomCurrentTemp() {
        return roomCurrentTemp;
    }

    public void setRoomCurrentTemp(String roomCurrentTemp) {
        this.roomCurrentTemp = roomCurrentTemp;
    }

    public String getRoomCurrentHumidity() {
        return roomCurrentHumidity;
    }

    public void setRoomCurrentHumidity(String roomCurrentHumidity) {
        this.roomCurrentHumidity = roomCurrentHumidity;
    }

    @Override
    public String toString() {
        return this.roomName;
    }
}
