FROM python:3-alpine

COPY requirements.txt /app/
WORKDIR /app
RUN pip install -r requirements.txt

COPY . /app

EXPOSE 5000
CMD ["python", "main.py"]