#!/usr/bin/env bash

set -euo pipefail

# container running with no volumes
docker run -d -p 1234:80 --name=volumeless superorbital/color

# container with bind mount, volume and tmpfs
docker run -d \
  --name=volumeful \
  -p 4321:80 \
  --mount type=bind,src=/home/student,dst=/mnt/home \
  --mount type=bind,src=/etc,dst=/mnt/etc \
  superorbital/color

# container that dies clean
docker run --name=happy ubuntu true

# container that dies dirty
docker run --name=sad ubuntu bash -c 'exit 123'
