#!/usr/bin/env bash

resp=$(curl -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spa-heroes" \
  -d "client_secret=e227337d-a832-40c4-95ea-2fd45a921c08" \
  -d "username=jeroen" \
  -d "password=jeroen" \
  -d "grant_type=password" \
  -X POST http://localhost:9080/auth/realms/heroes/protocol/openid-connect/token)

echo "resp = $resp"
token=`echo ${resp} | jq -r .access_token`

echo "$token"
export token