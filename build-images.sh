#!/bin/bash

PROJECT_ID="orange-api-214308"
IMAGE_PREFIX="gcr.io/$PROJECT_ID/microcalc"
MODULES=(add sub mult div neg mod pow parser)
CUR_DIR=$PWD

# build
for MODULE in $MODULES; do
    cd $CUR_DIR/services/$MODULE
    docker build -t "${IMAGE_PREFIX}-${MODULE}" . || exit 1
done

# push
for MODULE in $MODULES; do
    docker push "${IMAGE_PREFIX}-${MODULE}" || exit 1
done

docker rm $(docker ps -q -f status=exited)
docker rmi $(docker images -q -f dangling=true)
