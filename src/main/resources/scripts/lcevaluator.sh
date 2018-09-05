#!/bin/bash
pkill -f jar
sleep 5

echo "-------------------------------------------------Starting Lcevaluator------------------------------------------------"
sleep 10
if [ "$Component2" == "" ]; then
  echo "0,\"$ApplicationName1\"" > /qanarySetup/Applications/workspace/qanary_qa/lcevaluator/src/main/resources/pipelines.csv
else
  echo "0,\"$ApplicationName1,$ApplicationName2\"" > /qanarySetup/Applications/workspace/qanary_qa/lcevaluator/src/main/resources/pipelines.csv
fi

sleep 2
cd /qanarySetup/Applications/workspace/qanary_qa/lcevaluator
nohup mvn clean install -DskipDockerBuild >$QANARY_LOG/$ApplicationName1"_$ApplicationName2""_lcevaluator".log 2>&1 &