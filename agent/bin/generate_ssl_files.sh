#! /bin/bash

set -x
set -e
set -o pipefail

SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TARGET_PATH=$1

# generate private keys
openssl genrsa -out ${TARGET_PATH}/ca.key 4096
openssl genrsa -out ${TARGET_PATH}/server.key 4096
openssl genrsa -out ${TARGET_PATH}/client.key 4096

# generate root certificate
openssl req -new -x509 -key ${TARGET_PATH}/ca.key -out ${TARGET_PATH}/ca.crt -subj "/CN=some.net.com"

# generate server certificate by root certification
openssl req -new -key ${TARGET_PATH}/server.key -out ${TARGET_PATH}/server.csr -subj "/CN=localhost"
openssl x509 -req \
    -in ${TARGET_PATH}/server.csr \
    -CA ${TARGET_PATH}/ca.crt \
    -CAkey ${TARGET_PATH}/ca.key \
    -set_serial 01 \
    -out ${TARGET_PATH}/server.crt

# generate client certificate by root certification
openssl req -new -key ${TARGET_PATH}/client.key -out ${TARGET_PATH}/client.csr -subj "/CN=localhost"
openssl x509 -req \
    -in ${TARGET_PATH}/client.csr \
    -CA ${TARGET_PATH}/ca.crt \
    -CAkey ${TARGET_PATH}/ca.key \
    -set_serial 01 -out ${TARGET_PATH}/client.crt

# change format with pkcs8 to keep private keys
openssl pkcs8 -topk8 -nocrypt -in ${TARGET_PATH}/client.key -out ${TARGET_PATH}/client.pem
openssl pkcs8 -topk8 -nocrypt -in ${TARGET_PATH}/server.key -out ${TARGET_PATH}/server.pem