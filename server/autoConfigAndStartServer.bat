set path=%CD%\apache-maven-3.8.1\bin

cd %CD%\smartcontroller

call mvn clean install

cls

echo "Starting server........................................."

call mvn exec:java -Dexec.mainClass=letrungson.com.smartcontroller.Main

PAUSE >nul