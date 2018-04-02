#!/bin/bash
function main {
    local databaseName="money-observer"
    local hostAddress="localhost"
    local portNumber="27017"

    mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval 'db.dropDatabase()'
    mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval 'db.createCollection("user")'
    mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval 'db.createCollection("password")'
    mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval 'db.createCollection("passwordInfo")'
    mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval 'db.createCollection("accounts")'
    mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval 'db.createCollection("transactions")'
}
main