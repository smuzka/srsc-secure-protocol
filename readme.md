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
  javac -d . srscProject/src/main/java/main.java TestMulticast/MulticastReceiver.java && java -cp . TestMulticast.MulticastReceiver 224.0.0.5 8181
  ```


* compile and run MulticastSender
  ```
  javac -d . srscProject/src/main/java/main.java TestMulticast/MulticastSender.java && java -cp . TestMulticast.MulticastSender 224.0.0.5 8181 5
  ```