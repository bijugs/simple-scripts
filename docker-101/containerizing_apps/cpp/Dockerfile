FROM alpine:3.10
RUN apk add --no-cache build-base libevent libevent-dev
WORKDIR /app
COPY app.cpp .
RUN g++ -o app app.cpp -levent
CMD ["./app"]
