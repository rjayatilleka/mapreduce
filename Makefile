SHELL = /bin/bash

all: build/mapreduce binaries distribution

clean:
	@rm -rf generated build bin dist logs

install: uninstall
	@cp -rf dist ~/jaya0089-mapreduce

uninstall:
	@rm -rf ~/jaya0089-mapreduce

takelogs:
	@rm -rf logs
	@cp -r ~/jaya0089-mapreduce/log logs
	@rm -rf ~/jaya0089-mapreduce/log
	@mkdir -p ~/jaya0089-mapreduce/log/metrics

test-output:
	@rm -rf /tmp/testlogs
	@scp -r cselabs:~/logs /tmp/testlogs

.PHONY: clean install uninstall remote-takelogs remote-client distribution


#### ------------------------------------------------------
#### ----- Java 8 -----------------------------------------

ip = $(shell dig +short myip.opendns.com @resolver1.opendns.com)
host = $(shell dig +short -x $(ip))
cselabs = $(shell if [[ $(host) == *.cselabs.umn.edu. ]] ; then echo cse ; fi)

ifeq ($(cselabs), cse)
	java = /soft/jdk1.8.0_31/bin/java
	javac = /soft/jdk1.8.0_31/bin/javac
else
	java = java
	javac = javac
endif

print-java-check:
	echo $(ip) $(host) $(cselabs) $(java) $(javac)


#### ------------------------------------------------------
#### ----- Thrift Code Generation -------------------------

thrift_src = thrift/mapreduce.thrift
thrift_gen = generated/mapreduce/thrift

$(thrift_gen): $(thrift_src) | generated
	@thrift --gen java -out generated $(thrift_src)
	@touch $@

generated:
	@mkdir generated

compile-thrift: $(thrift_gen)


#### ------------------------------------------------------
#### ----- Java Compilation -------------------------------

java_src = $(shell find src -name "*.java")

java_cp = "lib/*"
javac_flags = -sourcepath "src:generated" -d "build" -g

build:
	@mkdir build

build/mapreduce: $(java_src) compile-thrift | build
	@$(javac) -cp $(java_cp) $(javac_flags) $(java_src)
	@touch $@


#### ------------------------------------------------------
#### ----- Binaries ---------------------------------------

define binary
@echo $(java) $(java_flags) -cp build:$(java_cp) $(main) '"$$@"' > $@
@chmod +x $@
endef

binaries: bin/orchestrator bin/client bin/master

bin:
	@mkdir bin

bin/master: main = mapreduce.master.Master
bin/master: | bin
	$(binary)

bin/client: main = mapreduce.client.Client
bin/client: java_flags = -Dorg.slf4j.simpleLogger.defaultLogLevel=error
bin/client: | bin
	$(binary)

bin/orchestrator: test/orchestrator.py | bin
	@cp test/orchestrator.py $@
	@chmod +x $@


#### ------------------------------------------------------
#### ----- Distribution -----------------------------------

distribution: dist/log dist/log/metrics dist/lib dist/test dist/bin dist/build dist/work

dist:
	@mkdir dist

dist/log: | dist
	@mkdir dist/log

dist/work: | dist
	@mkdir dist/work
	@mkdir dist/work/input
	@mkdir dist/work/intermediate
	@mkdir dist/work/output

dist/log/metrics: | dist/log
	@mkdir dist/log/metrics

dist/lib: | dist
	@cp -rf lib dist/

dist/test: $(shell find test) | dist
	@cp -rf test dist/

dist/bin: binaries | dist
	@cp -rf bin dist/

dist/build: build/mapreduce | dist
	@cp -rf build dist/

