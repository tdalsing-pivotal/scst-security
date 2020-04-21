#!/usr/bin/env bash

data="{\"id\":\"id-1\",\"name\":\"name-1\"}"
curl -X POST -w "\n" -i -d "$data" -H "Authorization: Bearer $token" -H "Content-Type: application/json" http://localhost:8301/data