FROM composer:latest as builder
COPY . /app
RUN composer install

FROM php:7-alpine

ENV docker=true

RUN mkdir /app
COPY --from=builder /app /app
WORKDIR /app
EXPOSE 8000
CMD [ "php", "-S", "0.0.0.0:8000", "-t", "public" ]