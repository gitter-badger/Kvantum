#!/bin/sh
set +v

# Configuration
version=1.1-SNAPSHOT
minRam=256
maxRam=1024
# End of configuration

mkdir ./server/plugins
echo Starting the server! Use /stop or CTRL+C to stop it
sleep 5
java -Xms${minRam}M -Xmx${maxRam}M -jar Kvantum-${version}-all.jar
