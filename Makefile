.PHONY: clean
clean:
	rm -rf target

.PHONY: test
test:
	clj -X:test

.PHONY: build
build: clean
	clj -Sforce -T:build all

.PHONY: deploy
deploy: build
	sudo pkill java && \
	nohup java -jar target/utopia-standalone.jar &
