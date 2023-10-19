#!/usr/bin/env bash

resp=$(curl -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=test-client" \
  -d "username=test" \
  -d "password=test" \
  -d "grant_type=password" \
  -X POST http://localhost:8080/realms/test-realm/protocol/openid-connect/token)

echo "resp = $resp"
token=`echo ${resp} | jq -r .access_token`

echo "$token"
export token