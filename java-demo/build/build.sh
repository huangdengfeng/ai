#!/bin/bash
set -e

version=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
name=$(mvn -Dexec.executable='echo' -Dexec.args='${project.artifactId}' --non-recursive exec:exec -q)
mvn clean package
docker build --build-arg MODULE_NAME=${name} -t "${name}":"${version}" .
# 删除被覆盖的，这里需要先停止相关容器，正常镜像机器不会启动容器
docker images | grep '<none>.*<none>' | awk '{print $3 }' | xargs docker rmi
