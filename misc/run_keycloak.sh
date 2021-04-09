#!/usr/bin/env bash
pushd /usr/local/keycloak/keycloak-12.0.4/bin
./standalone.sh -Djboss.socket.binding.port-offset=1000