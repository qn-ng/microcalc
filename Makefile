DOCO := @docker-compose -f docker-compose.yml -f docker-compose.ci.yml

.PHONY: all build push clean helm helm-clean
all: build

build:
	${DOCO} build

build-%:
	${DOCO} build $*

push:
	${DOCO} push

push-%:
	${DOCO} push $*

clean:
	${DOCO} down -v --rmi all

clean-%:
	${DOCO} down -v --rmi all $*

helm:
	@mkdir -p helm/dist
	@helm serve --repo-path helm/dist &
	@helm package -d helm/dist helm/microcalc
	@helm repo index helm/dist

helm-clean:
	@pkill helm
	@rm -rf helm/dist