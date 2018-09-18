#!/bin/bash
pkill -f jar
sleep 5
Component1=$1
Component2=$2
Component3=$3
Component4=$4
Component5=$5

STARDOG=/Users/SyalMac/Downloads/stardog-4.1.3
QANARY=/Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/qanary_qa
QANARY_LOG=/Users/SyalMac/Desktop/Kunwar_Thesis/KunwarThesis/qanary_logs
ApplicationPrefix='spring.application.name='

#cat $QANARY/$Component1/src/main/resources/config/application.properties
#cat $QANARY/$Component2/src/main/resources/config/application.properties

App1=$(grep -i 'spring.application.name.*' $QANARY/$Component1/src/main/resources/config/application.properties)
ApplicationName1="${App1/$ApplicationPrefix/}"

App2=$(grep -i 'spring.application.name.*' $QANARY/$Component2/src/main/resources/config/application.properties)
ApplicationName2="${App2/$ApplicationPrefix/}"

App3=$(grep -i 'spring.application.name.*' $QANARY/$Component3/src/main/resources/config/application.properties)
ApplicationName3="${App3/$ApplicationPrefix/}"

App4=$(grep -i 'spring.application.name.*' $QANARY/$Component4/src/main/resources/config/application.properties)
ApplicationName4="${App4/$ApplicationPrefix/}"

App5=$(grep -i 'spring.application.name.*' $QANARY/$Component5/src/main/resources/config/application.properties)
ApplicationName5="${App5/$ApplicationPrefix/}"

rm /Users/SyalMac/Downloads/stardog-4.1.3/system.lock
$STARDOG/bin/stardog-admin server stop
sleep 2
$STARDOG/bin/stardog-admin server start
sleep 10

echo "-------------------------------------------------Starting Pipeline & Components------------------------------------"

cd $QANARY
nohup java -jar qanary_pipeline-template/target/qa.pipeline-1.1.1.jar &
sleep 15
nohup java -jar $Component1/target/*.jar 2>$QANARY_LOG/$ApplicationName1"_error".log 1>$QANARY_LOG/$ApplicationName1"_out".log &
if [ "$Component2" == "" ]; then
    echo "-------------------------------------------------No Component2 supplied------------------------------------"
else
    sleep 15
    nohup java -jar $Component2/target/*.jar 2>$QANARY_LOG/$ApplicationName2"_error".log 1>$QANARY_LOG/$ApplicationName2"_out".log &
fi
if [ "$Component3" == "" ]; then
    echo "-------------------------------------------------No Component3 supplied------------------------------------"
else
    sleep 15
    nohup java -jar $Component3/target/*.jar 2>$QANARY_LOG/$ApplicationName3"_error".log 1>$QANARY_LOG/$ApplicationName3"_out".log &
fi
if [ "$Component4" == "" ]; then
    echo "-------------------------------------------------No Component4 supplied------------------------------------"
else
    sleep 15
    nohup java -jar $Component4/target/*.jar 2>$QANARY_LOG/$ApplicationName4"_error".log 1>$QANARY_LOG/$ApplicationName4"_out".log &
fi
if [ "$Component5" == "" ]; then
    echo "-------------------------------------------------No Component5 supplied------------------------------------"
else
    sleep 15
    nohup java -jar $Component5/target/*.jar 2>$QANARY_LOG/$ApplicationName5"_error".log 1>$QANARY_LOG/$ApplicationName5"_out".log &
fi
sleep 20
echo "exit"