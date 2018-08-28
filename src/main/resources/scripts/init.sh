#!/bin/bash
pkill -f jar
sleep 5

declare -i num=0
comps=()
for item in "$@";
do
    ((num++))
    comps+=(Component$num)
done

echo $comps

Component1=$1
Component2=$2

STARDOG=/Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/stardog-5.3.3
QANARY=/Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/qanary_qa
QANARY_LOG=/Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/qanary_logs
ApplicationPrefix='spring.application.name='

#cat $QANARY/$Component1/src/main/resources/config/application.properties
#cat $QANARY/$Component2/src/main/resources/config/application.properties

App1=$(grep -i 'spring.application.name.*' $QANARY/$Component1/src/main/resources/config/application.properties)
echo $App1
ApplicationName1="${App1/$ApplicationPrefix/}"

App2=$(grep -i 'spring.application.name.*' $QANARY/$Component2/src/main/resources/config/application.properties)
echo $App2
ApplicationName2="${App2/$ApplicationPrefix/}"




# echo "-------------------------------------------------Git Updates-----------------------------------------------------------"
cd $QANARY
git checkout arun
git reset --hard
git pull

echo "-------------------------------------------------Stardog-----------------------------------------------------------"
rm /Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/stardog-5.3.3/system.lock
$STARDOG/bin/stardog-admin server stop
sleep 2
$STARDOG/bin/stardog-admin server start
sleep 10
cd $QANARY/qanary_pipeline-template
mvn clean install -DskipDockerBuild
cd $QANARY/$Component1
mvn clean install -DskipDockerBuild
cd $QANARY/$Component2
mvn clean install -DskipDockerBuild

echo "-------------------------------------------------Starting Pipeline and Component------------------------------------"
cd $QANARY
nohup java -jar qanary_pipeline-template/target/qa.pipeline-1.1.0.jar &
sleep 10
nohup java -jar $Component1/target/*.jar 2>$QANARY_LOG/$ApplicationName1"_error".log 1>$QANARY_LOG/$ApplicationName1"_out".log &
sleep 10
nohup java -jar $Component2/target/*.jar 2>$QANARY_LOG/$ApplicationName2"_error".log 1>$QANARY_LOG/$ApplicationName2"_out".log &