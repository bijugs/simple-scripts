#!/bin/sh
cp ../target/flink-data-generator-1.0-SNAPSHOT.jar .
docker image rm artprod.dev.bloomberg.com/spaas/bnair10/datagen-client:1.0
docker image rm artprod.dev.bloomberg.com/spaas/bnair10/datagen:1.0
docker build -t artprod.dev.bloomberg.com/spaas/bnair10/datagen:1.0 -f Dockerfile .
docker build -t artprod.dev.bloomberg.com/spaas/bnair10/datagen-client:1.0 -f Dockerfile.client .
