#!/bin/sh

~/Downloads/protoc-3/bin/protoc -I=. --java_out=../src/main/java ./addressbook.proto
