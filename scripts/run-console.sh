#!/bin/bash

sudo systemctl restart postgresql.service
sudo systemctl restart redis-server.service

cd ../

gradle clean build -xTest
nohup gradle run -xTest &
