#!/usr/bin/env bash
pushd /usr/local/keycloak/keycloak-9.0.2/bin
./standalone.sh -Djboss.socket.binding.port-offset=1000