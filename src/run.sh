#!/bin/bash

echo "[run.sh] Starting AIAD runner..."

compRes=$(javac -classpath "./jade/lib/jade.jar" *.java)

if [ $? -eq 0 ]
then
	echo "[run.sh] Starting Jade..."
	java -classpath "./jade/lib/jade.jar:." jade.Boot -agents agent1:PingPong\(dat-argument\)
else
	echo "[run.sh] There were compilation errors, aborting..."
fi
