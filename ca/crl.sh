#!/bin/bash

cd $(dirname $0)

openssl ca -config ca.cnf -gencrl -out files/ca.crl.pem
openssl crl -in files/ca.crl.pem -out files/ca.crl -outform der
rm files/ca.crl.pem
