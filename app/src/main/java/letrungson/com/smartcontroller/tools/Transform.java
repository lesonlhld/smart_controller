package letrungson.com.smartcontroller.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import letrungson.com.smartcontroller.model.Device;

public class Transform {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    static public String BinaryToDaily(String input) {
        char[] A = input.toCharArray();
        if (input.equals("1111111")) {
            return "Daily";
        }

        StringBuilder result = new StringBuilder("");
        if (A[0] == '1') {
            result.append("Mon, ");
        }
        if (A[1] == '1') {
            result.append("Tue, ");
        }
        if (A[2] == '1') {
            result.append("Wed, ");
        }
        if (A[3] == '1') {
            result.append("Thu, ");
        }
        if (A[4] == '1') {
            result.append("Fri, ");
        }
        if (A[5] == '1') {
            result.append("Sat, ");
        }
        if (A[6] == '1') {
            result.append("Sun, ");
        }

        if (result.length() == 0) {
            return "No day";
        } else {
            return result.substring(0, result.length() - 2);
        }
    }

    static public String toListNameFromDeviceId(List<Device> listDevice, List<String> listId) {
        if (listId == null || listId.size() == 0) {
            return "No device";
        }
        String listName = "";
        for (Device device : listDevice) {
            for (String id : listId) {
                if (device.getDeviceId().equals(id)) {
                    listName += device.getDeviceName() + ", ";
                    break;
                }
            }
        }
        return listName.substring(0, listName.length() - 2);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String convertToCurrentTimeZone(String Date) {
        String converted_date = "";
        try {

            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            java.util.Date date = utcFormat.parse(Date);

            DateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            currentFormat.setTimeZone(TimeZone.getTimeZone(getCurrentTimeZone()));

            converted_date = currentFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return converted_date;
    }


    //get the current time zone

    public static String getCurrentTimeZone() {
        TimeZone tz = Calendar.getInstance().getTimeZone();
        return tz.getID();
    }
}
