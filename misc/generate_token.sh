#!/usr/bin/env bash

resp=$(curl -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spa-heroes" \
  -d "client_secret=28c2cc01-3cb4-4266-96c6-57556d742a69" \
  -d "username=jeroen" \
  -d "password=jeroen" \
  -d "grant_type=password" \
  -X POST http://localhost:9080/auth/realms/heroes/protocol/openid-connect/token)

echo "resp = $resp"
token=`echo ${resp} | jq -r .access_token`

echo "$token"
export token