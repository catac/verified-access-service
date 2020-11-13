#!/bin/bash

cd $(dirname $0)

mkdir -p files/newcerts files/work
touch files/work/index.txt
test -r files/work/serial || echo '01' > files/work/serial

echo "Checking or initialising files/ca.key ..."
test -r files/ca.key || openssl genrsa -out files/ca.key 2048

echo "Checking or initialising files/ca.crt ..."
test -r files/ca.crt || openssl req -new -x509 -days 3650 -key files/ca.key -out files/ca.crt

echo "Checking or initializing files/ca.cnf ..."
if [ ! -r files/ca.cnf ]; then
    read -p "VA-Service URL: " base_url
    cat <<EOT > files/ca.cnf
[ usr_cert ]
# This is the link where we can get the issuer certificate
issuerAltName = URI:${base_url}/ca.crt

# This is the link where to get the latest CRL
crlDistributionPoints = URI:${base_url}/ca.crl
EOT
fi
