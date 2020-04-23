FROM golang:1.14 as builder

COPY go.mod go.sum /app/
WORKDIR /app
RUN go mod download

COPY . /app/
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo

FROM alpine:latest
COPY --from=builder /app/mult /usr/local/bin/mult
EXPOSE 8080
CMD ["/usr/local/bin/mult"]
