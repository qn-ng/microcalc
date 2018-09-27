FROM python:3-alpine

RUN pip install requests robotframework robotframework-requests

COPY . /app
WORKDIR /app

CMD ["robot", "--outputdir=/results", "tests.robot"]