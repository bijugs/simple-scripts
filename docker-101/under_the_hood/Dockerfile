FROM alpine:3.10 as build
RUN apk add --no-cache build-base \
      libevent-static libevent-dev
WORKDIR /app
COPY app.cpp .
RUN g++ -o app app.cpp -levent -static

FROM scratch
WORKDIR /
COPY --from=build /app/app .
CMD ["/app"]
