##################################################
# Makefile for Python client and server.
##################################################

ROOT_DIR=..
include $(ROOT_DIR)/Makevars

IMPL_MODULE=pyimpl
IDL_OUT=$(IMPL_MODULE)/omni$(OMNIORBPY_VER)/$(BASE)_idl.py

MODULE_DIRS=$(MODULE) $(MODULE)__POA

CLIENT=client-py
SERVER=server-py

build: idl

idl: $(IDL_OUT) 

# Directories created by omniidl are not needed due to a hack
$(IDL_OUT): $(IDL)
	$(IDLC_PY) -Wbstubs=$(IMPL_MODULE).omni$(OMNIORBPY_VER) $<
	rm -rf $(MODULE_DIRS)

clean:
	rm -f $(IMPL_MODULE)/*.pyc
	rm -rf $(IMPL_MODULE)/omni*

install: build $(IMPL_MODULE)/*.py
	mkdir -p $(BIN)/$(IMPL_MODULE)
	install -m 644 $(IMPL_MODULE)/*.py $(BIN)/$(IMPL_MODULE)
	install -m 644 $(IDL_OUT) -D $(BIN)/$(IDL_OUT)
	install -m 644 /dev/null $(dir $(BIN)/$(IDL_OUT))__init__.py
	echo "#!/bin/sh\npython -m $(IMPL_MODULE).client \"\$$@\"" > tmp
	install -m 755 tmp $(BIN)/$(CLIENT)
	echo "#!/bin/sh\npython -m $(IMPL_MODULE).server \"\$$@\"" > tmp
	install -m 755 tmp $(BIN)/$(SERVER)
	rm -r tmp
