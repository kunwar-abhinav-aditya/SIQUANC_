#!/bin/bash
INPUT=queries.csv
OLDIFS=$IFS
IFS=,
[ ! -f $INPUT ] && { echo "$INPUT file not found"; exit 99; }
while read id query
do
	echo "id : $id"
	echo "query : $query"
done < $INPUT
IFS=$OLDIFS