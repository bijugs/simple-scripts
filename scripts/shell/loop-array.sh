#!/bin/sh

declare -a arr=("flink-controller" "admission-webhook" "spaas-controller")

declare -a arr1=("flink-1.6.2" "java-1.8" "confluent-connect-5.3.1" "confluent-connect-5.0.0" "confluent-connect-4.1.2" "confluent-connect-4.0.0" "flink-1.7.2")

echo $1

if [ $1 = "c" ]
then
  for i in ${arr[@]}
  do 
    echo $2$i:$3
    docker image rm $2$i:$3
  done
elif [ $1 = "f" ]
  then
  for i in ${arr1[@]}
  do
    echo $2$i:$3
    docker image rm $2$i:$3
  done
fi
