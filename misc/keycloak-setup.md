# Using Keycloak

## Keycloak Setup

1. Download Keycloak server here: https://www.keycloak.org/downloads
1. Unzip or untar to /usr/local
1. Start server via the `run_keycloak.sh` script.  Note that the server is listening on 9080 rather than the default 8080.  The default user/pass
 is `admin`/`admin`.
1. Configure via directions here: https://ordina-jworks.github.io/security/2019/08/22/Securing-Web-Applications-With-Keycloak.html.  The redirect URI
 is not used, but it must be set to something.  So use `http://localhost:8301`.  Also, set the Access Type to `confidential` and
 ensure that Direct Access Grants Enabled is On.  For the `jeroen` user set the password to match the username for simplicity.
1. Get the client secret from `spa-heroes` from the `Credentials` tab in the Client view.  Copy this value to the `generate_token.sh` script.
1. Generate a JWT token by executing `source ./generate_token.sh`.  This will set the JWT token in a `token` bash variable.  Note that the token
 will expire after a few minutes so re-run this command before sending data to the client.