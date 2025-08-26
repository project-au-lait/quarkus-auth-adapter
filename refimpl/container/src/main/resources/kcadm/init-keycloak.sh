#!/bin/bash
set -x
SCRIPT_DIR=$(cd $(dirname $0); pwd)

export KEYCLOAK_HOME=/opt/keycloak
export PATH=$PATH:$KEYCLOAK_HOME/bin

. $SCRIPT_DIR/keycloak.env

kcadm.sh config credentials --server http://localhost:8080 --realm master --user $ADMIN_USER --password ${ADMIN_PASSWORD}

function create_realms() {
  sed 1d $SCRIPT_DIR/realms.csv | while IFS=, read REALM CONFIG_PATH FRONTEND_PORT CLIENT_ID CLIENT_SECRET || [ -n "$REALM" ]
  do
    kcadm.sh create realms -f $SCRIPT_DIR/$CONFIG_PATH -o

    kcadm.sh create clients -r $REALM -s clientId=$CLIENT_ID -s enabled=true \
    -s rootUrl=http://127.0.0.1:$FRONTEND_PORT \
    -s 'redirectUris=["*"]' -s 'webOrigins=["*"]' -s directAccessGrantsEnabled=true \
    -s secret=$CLIENT_SECRET -s publicClient=false -s authorizationServicesEnabled=true -s serviceAccountsEnabled=true
  done
}

function create_roles() {
  sed 1d $SCRIPT_DIR/roles.csv | while IFS=, read REALM ROLE || [ -n "$REALM" ]
  do
    kcadm.sh create roles -r $REALM -s name=$ROLE
  done
}

function create_users() {
  sed 1d $SCRIPT_DIR/users.csv | while IFS=, read REALM USER_NAME EMAIL PASSWORD ROLE || [ -n "$REALM" ]
  do
    kcadm.sh create users -r $REALM -s username=$USER_NAME -s enabled=true -s email=$EMAIL -s emailVerified=true -s firstName="First" -s lastName="Last" -s 'requiredActions=[]'
    kcadm.sh set-password -r $REALM --username $USER_NAME --new-password $PASSWORD
    kcadm.sh add-roles --uusername $USER_NAME --rolename $ROLE -r $REALM
  done
}

function install_theme() {
  mkdir -p $KEYCLOAK_HOME/themes/$THEME
  cp -r $SCRIPT_DIR/theme/* $KEYCLOAK_HOME/themes/$THEME
}

function create_all() {
  create_realms
  create_roles
  create_users
  install_theme
}

$1