# MicroCalc: A dead-simple, polyglot, microservice-oriented calculator

*Built with Istio and ❤️*

## Local run

**Requirements:** docker, docker-compose

**Commands:** `$ docker-compose up`

The application will be accessible at:

- Parser service: http://localhost:8080

## Deploy to Kubernetes (Istio required)

**Requirements:** docker, nodejs, kubectl

**Commands:**

- Modify [build-images.sh](build-images.sh) and replace the **PROJECT_ID** and **IMAGE_PREFIX** variables with proper values
- Modify [gen-k8s.js](gen-k8s.js) and replace the **IMG_PREFIX** and **APP_HOSTNAME** with proper values
- Execute the following commands to build application images, push it to the private docker registry and generate the deployment template for Kubernetes:
```shell
$ ./build-images
$ node gen-k8s.js > deploy.yaml
$ kubectl create namespace microcalc
$ kubectl label namespace microcalc -l istio-injection=enabled
$ kubectl apply -f deploy.yaml -n microcalc
```

The application will be accessible at:

- Parser service: http://APP_HOSTNAME

## Usage guide

- Parser service

```
POST /api/v1/calculate HTTP/1.1
Content-Type: application/json
[...]

{
    "input": "1+1"
}

HTTP/1.1 200 OK
content-type: application/json
[...]

{
    "operands": [
        1,
        1
    ],
    "origins": [
        {
            "result": 1,
            "service": "name: parser, version: v1"
        },
        {
            "result": 1,
            "service": "name: parser, version: v1"
        }
    ],
    "result": 2,
    "service": "name: add, version: v1"
}
```