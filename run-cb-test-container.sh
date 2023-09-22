#!/usr/bin/env bash

docker run -d \
  --ulimit nofile=40960:40960 \
  --ulimit core=100000000:100000000 \
  --ulimit memlock=100000000:100000000 \
  --name cb \
  -p 8091-8094:8091-8094 -p 11210:11210 \
  -v "$(pwd)"/cb-test-prepare.sh:/cb-test-prepare.sh \
  "couchbase:enterprise-$CB_VERSION"

for _ in {1..5}; do
  docker exec cb /cb-test-prepare.sh && break || sleep 8;
done
