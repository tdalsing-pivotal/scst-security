#!/usr/bin/env bash

send() {
  echo "data = $1"
  curl -X POST -w "\n" -i -d "$1" -H "Authorization: Bearer $token" -H "Content-Type: application/json" http://localhost:8301/data
}

send '{"id":"id-1","name":"name-1"}'
send '{"id":"id-2","name":"name-2","invalidJwt":true}'
send '{"id":"id-3","name":"name-3","invalidSign":true}'
