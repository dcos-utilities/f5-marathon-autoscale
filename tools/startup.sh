#!/bin/bash

export NODE_NAME=`echo $HOST`'-'`echo $HOSTNAME`
java $JAVA_OPTS -jar /f5-marathon-autoscale.jar