FROM golang:1.11 as builder

RUN go get -u github.com/golang/dep/...

COPY Gopkg.toml Gopkg.lock /go/src/app/
WORKDIR /go/src/app
RUN dep ensure -vendor-only

COPY . /go/src/app/
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo

FROM alpine:latest
ENV GIN_MODE=release
COPY --from=builder /go/src/app/app /app
EXPOSE 8080
CMD ["/app"]