IMAGE_PREFIX := foly/microcalc
SERVICES := add sub mult div neg mod pow parser

.PHONY: all build push clean
all: build

build:
	@echo "\nBuilding service [$@]"
	@make $(addprefix build-, $(SERVICES))

build-%:
	@docker build -t "${IMAGE_PREFIX}-$*" "services/$*"

push:
	@echo "\nPushing service [$@]"
	@make $(addprefix push-, $(SERVICES))

push-%: build-%
	@docker push "${IMAGE_PREFIX}-$*"

clean:
	@echo "\nCleaning service [$@]"
	@make $(addprefix clean-, $(SERVICES))

clean-%:
	@docker rmi "${IMAGE_PREFIX}-$*" || true