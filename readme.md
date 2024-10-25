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
 javac -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar TestMulticast/MulticastSender.java && java -cp srscProject/target/SRSCProject-1.0-SNAPSHOT.jar:./TestMulticast MulticastSender 224.0.0.5 8181
  ```