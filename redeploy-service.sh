#!/bin/bash

if [ -z "$1" ]; then echo "./redeploy-service.sh \${PORT}" && exit 1; fi
PORT=$1

# TODO detect change in repo
md5=$(find pom.xml src/ frontend/ -ls | md5sum)
echo $md5

if [ "$(cat .md5)" = "$md5" ]
then
	echo "already up-to-date"
else
	echo "redeploy"
	echo "$md5" > ./.md5

	mvn install -Pproduction

	docker build -t databus-moss .

	docker stop "databus-moss-cd"
	docker rm "databus-moss-cd"

	docker run -d -p $PORT:8080 --name "databus-moss-cd" databus-moss
fi

