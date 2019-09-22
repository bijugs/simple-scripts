FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates \
      curl

# Intended to be run with
# --mount type=bind,dst=/host,src=$PWD
WORKDIR /host

ENTRYPOINT ["curl", "-sSLO"]
CMD ["https://httpbin.org/json"]
