FROM composer:latest as builder

COPY composer.json composer.lock /app/
WORKDIR /app
RUN composer install
COPY . /app

FROM php:7-alpine

ENV docker=true

COPY --from=builder /app /app
WORKDIR /app
EXPOSE 8000
CMD [ "php", "-S", "0.0.0.0:8000", "-t", "public" ]