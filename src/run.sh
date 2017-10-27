#!/bin/bash

echo "Starting AIAD runner..."

mainClass="PingPong"
compRes=$(sudo javac -classpath "./jade/lib/jade.jar" *.java)

if [ $? -eq 0 ]
then
	sudo java -classpath "./jade/lib/jade.jar" jade.Boot -gui
else
	echo "could not execute $mainClass because there were compilation errors"
fi
