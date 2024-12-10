# SRSC: Secure Protocol
Assignment 1

Project uses java 22

### To compile package:
```
mvn package
```

## TestMulticast

* compile and run MulticastReceiver
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TestMulticast/MulticastReceiver.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TestMulticast MulticastReceiver 224.0.0.5 8181
  ```
If it gets stuck, just run it again. 

* compile and run MulticastSender
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TestMulticast/MulticastSender.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TestMulticast MulticastSender 224.0.0.5 8181 2
  ```

## StreamingService

* compile and run StreamServer
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar StreamingService/hjStreamServer/*.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./StreamingService/hjStreamServer hjStreamServer 
  ```


* compile and run Proxy
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar StreamingService/hjUDPproxy/*.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./StreamingService/hjUDPproxy hjUDPproxy user0 Password!0 127.0.0.1 12345 monsters.dat 10000 8888
  ```

## TFTP-master

* Run from /TFTP-master/TFTPServer/src
  ```
  javac -cp ../../../srscProject/target/SRSCProject-1.0-SNAPSHOT.jar ./*.java && java -cp ../../../srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./ TFTPServer  
  ```
  
* Run from /TFTP-master/TFTPClient/src
  ```
  javac -cp ../../../srscProject/target/SRSCProject-1.0-SNAPSHOT.jar ./*.java && java -cp ../../../srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./ TFTPClient 127.0.0.1 R server1.jpg
  ```