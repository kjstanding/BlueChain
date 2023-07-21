mvn clean install ;
rm src/main/java/node/nodeRegistry/*.txt ;
java -cp "target/network-1.0-SNAPSHOT.jar:target/commons-math3-3.6.1.jar" NetworkLauncher
