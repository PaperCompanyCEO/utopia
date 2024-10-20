.PHONY: clean
clean:
	rm -rf target

.PHONY: build
build: clean
	clj -Sforce -T:build all

.PHONY: deploy
deploy: build
	sudo pkill java && \ <<<< 더 잘 타겟팅 하는 방법?
	nohup java -jar target/utopia-standalone.jar &
