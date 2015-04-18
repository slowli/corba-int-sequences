##################################################
# Top level Makefile for CORBA demo.
##################################################

ROOT_DIR=.
include $(ROOT_DIR)/Makevars

.PHONY: java python cs

build: java python cs

java:
	@echo Building Java...
	cd java && $(MAKE) build && cd ..
	
python:
	@echo Building Python...
	cd python && $(MAKE) build && cd ..

cs:
	@echo Building C#...
	cd cs && $(MAKE) build && cd ..
	
install: build
	@echo Installing Java...
	cd java && $(MAKE) install && cd ..
	@echo Installing Python...
	cd python && $(MAKE) install && cd ..
	@echo Installing C#...
	cd cs && $(MAKE) install && cd ..
	
clean:
	cd java && $(MAKE) clean && cd ..
	cd python && $(MAKE) clean && cd ..
	cd cs && $(MAKE) clean && cd ..
