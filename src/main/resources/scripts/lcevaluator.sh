#!/bin/bash
pkill -f jar
sleep 5

QANARY_LOG=/Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/qanary_logs

echo "-------------------------------------------------Starting Lcevaluator------------------------------------------------"
sleep 10
cd /Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/qanary_qa/lcevaluator
nohup mvn clean install -DskipDockerBuild >$QANARY_LOG/"AmbiverseNed_EarlRelationLinking_QueryBuilder""_lcevaluator".log 2>&1 &
echo "exit"