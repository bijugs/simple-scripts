#!/usr/bin/env bash

. ~/.bashrc

GC="gcloud --quiet --no-user-output-enabled"

$GC config set core/disable_prompts True                   > /dev/null
$GC config set component_manager/disable_update_check true > /dev/null
$GC config set core/verbosity error                        > /dev/null
$GC config set filestore/location "$CLOUDSDK_COMPUTE_ZONE" > /dev/null

/usr/local/bin/docker-credential-gcr configure-docker > /dev/null

lab-cluster login || true
