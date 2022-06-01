package letrungson.com.smartcontroller.model;

public class Data {
    private String id;
    private String last_value;
    private String updated_at;
    private String key;

    public Data(String id, String last_value, String updated_at, String key) {
        this.id = id;
        this.last_value = last_value;
        this.updated_at = updated_at;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getLast_value() {
        return last_value;
    }

    public void setLast_value(String last_value) {
        this.last_value = last_value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
