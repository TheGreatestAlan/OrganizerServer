#!/bin/bash

cd ..
mkdir -p artifact
docker build . -t builder
