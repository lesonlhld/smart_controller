package letrungson.com.smartcontroller.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import letrungson.com.smartcontroller.Main;
import letrungson.com.smartcontroller.model.Data;
import letrungson.com.smartcontroller.model.Value;
import letrungson.com.smartcontroller.tools.Check;
import letrungson.com.smartcontroller.util.Constant;

public class MQTTService {
    final String serverUri = "tcp://io.adafruit.com:1883";
    final HashMap<String, String> clientId;
    public HashMap<String, IMqttClient> listMqttClient = new HashMap<String, IMqttClient>();

    public MQTTService() {
        clientId = new HashMap<String, String>();
        for (String name : Constant.getServerInfo().keySet()) {
            String username = name.toString();
            String password = Constant.getServerInfo().get(username).toString();
            clientId.put(username, MqttClient.generateClientId());
            IMqttClient publisher;
            try {
                publisher = new MqttClient(serverUri, clientId.get(username), new MqttDefaultFilePersistence("/tmp"));
                connectAndSubscribe(publisher, username, password);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectAndSubscribe(IMqttClient publisher, String username, String password) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            publisher.connect(mqttConnectOptions);
            subscribeToTopic(publisher, username);
        } catch (MqttException ex) {
        	System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Failed to connect: " + username);
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic(IMqttClient publisher, final String server) {
        String subscriptionTopic = server + "/feeds/" + "#";
        try {
            publisher.subscribe(subscriptionTopic, new IMqttMessageListener() {
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // TODO Auto-generated method stub
                    if (topic.indexOf("/json") != -1) {
                        // byte[] payload = message.getPayload();
                        String data_to_microbit = message.toString();
                        String feedName = topic.substring(topic.lastIndexOf('/', topic.lastIndexOf('/') - 1) + 1,
                                topic.lastIndexOf('/'));
                        String serverName = topic.substring(0, topic.indexOf('/'));
                        Data dataMqtt = new Gson().fromJson(data_to_microbit, new TypeToken<Data>() {
                        }.getType());
                        if (Check.checkExistDevice(Main.allDevices, dataMqtt.getKey())
                                && feedName.equals(dataMqtt.getKey()) && server.equals(serverName)) {
                        	System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Write MQTT data to database: topic: " + topic + ", data: " + data_to_microbit);
                            Value valueMqtt = new Gson().fromJson(dataMqtt.getLast_value(), new TypeToken<Value>() {

                            }.getType());
                            Database.processDataMQTT(dataMqtt.getKey(), dataMqtt, valueMqtt);
                        }
                    }
                }
            });

            listMqttClient.put(server, publisher);
            System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Subscribed: " + subscriptionTopic);
        } catch (MqttException ex) {
        	System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Failed to subscribe: " + subscriptionTopic);
            ex.printStackTrace();
        }
    }

    public void sendDataMQTT(String server, String deviceId, String data) {
        String topicOfDevice = server + "/feeds/" + deviceId;
        try {
            Value value = Check.checkAndGetValueOfDevice(deviceId);
            value.setData(data);

            MqttMessage message = new MqttMessage(value.convertToJson().getBytes(Charset.forName("UTF-8")));
            message.setQos(0);
            message.setRetained(true);
            // publish message to broker
            System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Message \"" + deviceId + ": " + data + "\" published to server: " + server);
            listMqttClient.get(server).publish(topicOfDevice, message);
        } catch (Exception e) {
        	System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + ": Failed to publish message to server: " + server);
            e.printStackTrace();
        }
    }
}
