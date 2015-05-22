all: compile
	@echo -e '[INFO] Done!' 
 
clean:
	@echo -e '[INFO] Cleaning Up..'
	@-rm -rf cs455/**/*.class cs455/**/**/*.class cs455/**/**/**/*.java cs455/**/**/**/**/*.java /tmp/cs455-tylerjms/
 
compile: 
	@echo -e '[INFO] Compiling the Source..'
	@javac -d . -cp ./lib/jericho-html-3.3.jar:. cs455/**/*.java 



