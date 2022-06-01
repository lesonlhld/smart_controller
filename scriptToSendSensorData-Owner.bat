@echo off
if NOT exist "C:\Program Files\mosquitto" (
	echo "mosquitto is not installed"
	exit
)

cd "C:\Program Files\mosquitto"

REM mosquitto_pub -h io.adafruit.com -p 1883 -u lesonlhld -P aio_WwjF84LarniCFSKcbFyhnFEFnqlG -t leson0108/feeds/sensor

:Loop
echo "Automatically send data every 40 seconds ..."
@echo off
set /a temperature = (%RANDOM%*20/32768) + 15
set /a humidity = (%RANDOM%*35/32768) + 35
set randomData=%temperature%-%humidity%

echo "{"id":"7","name":"TEMP-HUMID","data":"%randomData%","unit":"C-%%"}"
mosquitto_pub -h io.adafruit.com -p 1883 -u CSE_BBC -P aio_KXfp47zegx3CthMAEj6pB0ZeKoEm -t CSE_BBC/feeds/bk-iot-temp-humid -m "{""id"":""7"",""name"":""TEMP-HUMID"",""data"":""%randomData%"",""unit"":""C-%%""}"
timeout 40
GOTO :Loop