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
    --bucket-type=ephemeral \
    --bucket-ramsize=100 \
    --bucket-replica=0

/opt/couchbase/bin/couchbase-cli bucket-create \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --bucket=acc \
    --bucket-type=couchbase \
    --bucket-ramsize=100 \
    --bucket-replica=0

# https://developer.couchbase.com/documentation/server/5.0/cli/cbcli/couchbase-cli-user-manage.html
/opt/couchbase/bin/couchbase-cli user-manage \
    -c 127.0.0.1:8091 \
    -u Administrator -p password \
    --set \
    --rbac-username cb \
    --rbac-password cb_password \
    --roles data_reader[fodi],data_writer[fodi],data_reader[acc],data_writer[acc] \
    --auth-domain local
