gen-docker:
	docker build \
		-f workivabuild.Dockerfile \
		-t eva-catalog:local .

run-docker:
	- docker rm -f eva-catalog-local
	docker run \
		-p 3000:3000 \
		-e EVA_CATALOG_DATA=/shared/mock-catalog-data.edn \
		-v ${PWD}/common.alpha/test-data/eva/catalog/common/alpha:/shared \
		--name eva-catalog-local \
		eva-catalog:local

update-tocs:
	./.circleci/scripts/update-tocs.sh

docs:
	lein modules :dirs "client.alpha:common.alpha:server.alpha" docs
