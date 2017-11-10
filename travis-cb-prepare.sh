#!/usr/bin/env bash

# https://developer.couchbase.com/documentation/server/5.0/cli/cbcli-intro.html
/opt/couchbase/bin/couchbase-cli cluster-init \
    -c 127.0.0.1:8091 \
    --cluster-username=Administrator \
    --cluster-password=password \
    --cluster-ramsize=256

/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=fodi \
    --bucket-password=fodi_pw \
    --bucket-type=couchbase \
    --bucket-port=11211 \
    --bucket-ramsize=100 \
    --bucket-replica=0

#/opt/couchbase/bin/couchbase-cli bucket-delete \
#    -c 192.168.0.1:8091 \
#    -u Administrator -p password \
#    --bucket=default

/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=acc \
    --bucket-password=acc_pw \
    --bucket-type=couchbase\
    --bucket-port=11211 \
    --bucket-ramsize=100 \
    --bucket-replica=0
