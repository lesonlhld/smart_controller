package letrungson.com.smartcontroller.model;

public class LogState {
    private String datetime;
    private String deviceId;
    private String newState;
    private String userId;

    public LogState() {

    }

    public LogState(String datetime, String deviceId, String newState, String userId) {
        this.datetime = datetime;
        this.deviceId = deviceId;
        this.newState = newState;
        this.userId = userId;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
