# SRSC: Secure Protocol
Assignment 1

Project uses java 22

### To compile:
```
mvn clean install
```
### To run:
```
java -cp target/classes srscProject.src.main.java.main
```


## TestMulticast

* compile and run MulticastReceiver
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TestMulticast/MulticastReceiver.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TestMulticast MulticastReceiver 224.0.0.5 8181
  ```

* compile and run MulticastSender
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TestMulticast/MulticastSender.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TestMulticast MulticastSender 224.0.0.5 8181 2
  ```

## StreamingService

* compile and run StreamServer
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar StreamingService/hjStreamServer/*.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./StreamingService/hjStreamServer hjStreamServer StreamingService/hjStreamServer/movies/monsters.dat 127.0.0.1 10000
  ```

* compile and run Proxy
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar StreamingService/hjUDPproxy/*.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./StreamingService/hjUDPproxy hjUDPproxy 127.0.0.1:10000 127.0.0.1:8888
  ```


## TFTP-master

* compile and run TFTPServer
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TFTP-master/TFTPServer/src/*.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TFTP-master/TFTPServer/src TFTPServer
  ```

* compile and run TFTPClient
  ```
  javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TFTP-master/TFTPClient/src/*.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TFTP-master/TFTPClient/src TFTPClient 127.0.0.1 R server1.jpg
  ```