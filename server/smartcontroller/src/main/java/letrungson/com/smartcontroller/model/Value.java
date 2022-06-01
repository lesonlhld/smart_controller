package letrungson.com.smartcontroller.model;

public class Value {
    private String id;
    private String name;
    private String data;
    private String unit;

    public Value() {

    }

    public Value(String id, String name, String data, String unit) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.unit = unit;
    }

    public Value(String id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "\"name\":\"" + this.name + "\",\"data\":\"" + this.data + "\"";
    }

    public String convertToJson() {
        return "{\"id\":\"" + this.id + "\",\"name\":\"" + this.name + "\",\"data\":\"" + this.data + "\",\"unit\":\""
                + this.unit + "\"}";
    }
}
