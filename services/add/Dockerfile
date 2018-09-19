FROM node:8-alpine as builder

COPY . /app
WORKDIR /app

RUN npm install

FROM node:8-alpine

ENV APP_PORT=3000
EXPOSE ${APP_PORT}

RUN mkdir /app && chown node:node /app
COPY --from=builder --chown=node:node /app /app
WORKDIR /app

USER node

CMD [ "npm", "start" ]