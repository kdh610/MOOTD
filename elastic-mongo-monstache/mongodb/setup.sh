#!/bin/bash               
sleep 10 | echo Sleeping
mongosh mongodb://username:password@ip:27018/admin replicaSet.js
