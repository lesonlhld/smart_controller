package letrungson.com.smartcontroller.tools;

import java.util.List;

import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.model.Value;
import letrungson.com.smartcontroller.util.Constant;


public class Check {
    public static boolean checkExistDevice(List<Device> c, String id) {
        if (c == null || c.size() == 0) return false;
        for (Device o : c) {
            if (o != null && o.getDeviceId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkExistDeviceId(List<String> c, String id) {
        if (c == null || c.size() == 0) return false;
        for (String o : c) {
            if (o != null && o.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static String checkAndGetServerNameOfDevice(String deviceId) {
        if (Constant.getServer_CSE_BBC().containsKey(deviceId)) {
            return "CSE_BBC";
        } else if (Constant.getServer_CSE_BBC1().containsKey(deviceId)) {
            return "CSE_BBC1";
        } else {
            return Constant.MY_SERVER_NAME;
        }
    }

    public static Value checkAndGetValueOfDevice(String deviceId) {
        Value value = Constant.getServer_CSE_BBC().get(deviceId);
        if (value == null) {
            value = Constant.getServer_CSE_BBC1().get(deviceId);
            if (value == null) {
                value = new Value("", "", "", "");
            }
        }
        return value;
    }
}
