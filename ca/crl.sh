#!/bin/bash

cd $(dirname $0)

openssl ca -config ca.cnf -gencrl -out ca.crl
