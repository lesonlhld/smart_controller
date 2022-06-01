package letrungson.com.smartcontroller.util;

import java.util.HashMap;

import letrungson.com.smartcontroller.model.Value;

public class Constant {
    public static final String MY_SERVER_NAME = "leson0108";
    public static final String MY_SERVER_KEY = "aio_rHhv85FXuO6uVO2wgnOrl0FWF7az";
    public static HashMap<String, Value> server_CSE_BBC = new HashMap<String, Value>();
    public static HashMap<String, Value> server_CSE_BBC1 = new HashMap<String, Value>();
    public static HashMap<String, String> severInfo = new HashMap<String, String>();

    public static void initServer() {
        server_CSE_BBC.put("bk-iot-led", new Value("1", "LED", ""));
        server_CSE_BBC.put("bk-iot-speaker", new Value("2", "SPEAKER", ""));
        server_CSE_BBC.put("bk-iot-lcd", new Value("3", "LCD", ""));
        server_CSE_BBC.put("bk-iot-button", new Value("4", "BUTTON", ""));
        server_CSE_BBC.put("bk-iot-touch", new Value("5", "TOUCH", ""));
        server_CSE_BBC.put("bk-iot-traffic", new Value("6", "TRAFFIC", ""));
        server_CSE_BBC.put("bk-iot-temp-humid", new Value("7", "TEMP-HUMID", "C-%"));
        server_CSE_BBC.put("bk-iot-magnetic", new Value("8", "MAGNETIC", ""));
        server_CSE_BBC.put("bk-iot-soil", new Value("9", "SOIL", ""));
        server_CSE_BBC.put("bk-iot-drv", new Value("10", "DRV_PWM", ""));

        server_CSE_BBC1.put("bk-iot-relay", new Value("11", "RELAY", ""));
        server_CSE_BBC1.put("bk-iot-sound", new Value("12", "SOUND", ""));
        server_CSE_BBC1.put("bk-iot-light", new Value("13", "LIGHT", ""));
        server_CSE_BBC1.put("bk-iot-infrared", new Value("16", "INFRARED", ""));
        server_CSE_BBC1.put("bk-iot-servo", new Value("17", "SERVO", ""));
        server_CSE_BBC1.put("bk-iot-accelerometer", new Value("19", "ACCELEROMETER", ""));
        server_CSE_BBC1.put("bk-iot-time", new Value("22", "TIME", ""));
        server_CSE_BBC1.put("bk-iot-gas", new Value("23", "GAS", ""));

        severInfo.put("CSE_BBC", "");
        severInfo.put("CSE_BBC1", "");
        severInfo.put(MY_SERVER_NAME, MY_SERVER_KEY);
    }

    public static HashMap<String, Value> getServer_CSE_BBC() {
        return server_CSE_BBC;
    }

    public static HashMap<String, Value> getServer_CSE_BBC1() {
        return server_CSE_BBC1;
    }

    public static HashMap<String, String> getServerInfo() {
        return severInfo;
    }
}
