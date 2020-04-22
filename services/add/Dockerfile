FROM node:10-alpine

ENV APP_PORT=3000
ENV NODE_ENV=production
EXPOSE ${APP_PORT}

RUN mkdir /app && chown node:node /app
USER node

COPY package.json package-lock.json /app/
WORKDIR /app
RUN npm install

COPY . /app
CMD [ "npm", "start" ]