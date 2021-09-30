#!/usr/bin/env bash

# https://developer.couchbase.com/documentation/server/5.0/cli/cbcli-intro.html
/opt/couchbase/bin/couchbase-cli cluster-init \
    -c 127.0.0.1:8091 \
    --cluster-username=Administrator \
    --cluster-password=password \
    --cluster-ramsize=256

if [ "${CB:0:1}" != "4" ]; then
/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=fodi \
    --bucket-type=ephemeral \
    --bucket-ramsize=100 \
    --bucket-replica=0

# https://developer.couchbase.com/documentation/server/5.0/cli/cbcli/couchbase-cli-user-manage.html
/opt/couchbase/bin/couchbase-cli user-manage \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --set \
    --rbac-username fodi \
    --rbac-password fodi_pw \
    --roles bucket_admin[fodi] \
    --auth-domain local

/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=acc \
    --bucket-type=couchbase \
    --bucket-ramsize=100 \
    --bucket-replica=0

/opt/couchbase/bin/couchbase-cli user-manage \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --set \
    --rbac-username acc \
    --rbac-password acc_pw \
    --roles bucket_admin[acc] \
    --auth-domain local

else

/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=fodi \
    --bucket-password=fodi_pw \
    --bucket-type=couchbase \
    --bucket-ramsize=100 \
    --bucket-replica=0

/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=acc \
    --bucket-password=acc_pw \
    --bucket-type=couchbase\
    --bucket-ramsize=100 \
    --bucket-replica=0
fi

#/opt/couchbase/bin/couchbase-cli bucket-delete \
#    -c 192.168.0.1:8091 \
#    -u Administrator -p password \
#    --bucket=default
