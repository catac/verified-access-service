#!/bin/bash

cd $(dirname $0)

mkdir -p newcerts work
touch work/index.txt
test -r work/serial || echo '01' > work/serial

echo "Checking or initialising ca.key ..."
test -r ca.key || openssl genrsa -out ca.key 2048

echo "Checking or initialising ca.crt ..."
test -r ca.crt || openssl req -new -x509 -key ca.key -out ca.crt
