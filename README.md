# portmap
Proxies data from one port to another with an optional delay.  Great for testing
and debugging communication.

## Examples of how portmap has been used
  *  Simulate lag when taking to a web service
  *  Communicate with an ldap server that is not directly accessible by the client
  *  Debug communication between two services
  *  Create a Cassandra cluster where one node is significantly slower than the others


Usage:> java -jar portmap.jar portmap.properties

Properties in portmap.properties

``` 
listenPort: Local port to listen on for connections. (required)
listenAddr: Local address to listen on. (required)
destPort: Destination port to connect to. (required)
destAddr: Destination address to connect to. (required)
destSecure: Identifies if the destination connection should be made using ssl (true/false, default: false)
sendFile: Name of file to write all sent data to. (optional)
receiveFile: Name of file to write all received data to. (optional)
delay: Millisecond delay to add to each packet. (optional)
```