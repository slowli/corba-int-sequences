##################################################
# Makefile for Java client and server.
##################################################

ROOT_DIR=..
include $(ROOT_DIR)/Makevars

IDL_OUT=$(MODULE)/$(BASE).java

TMP_DIR=tmp
CLIENT=client-java.jar
SERVER=server-java.jar

# Package containing server and client implementations
IMPL_PK=$(MODULE).impl
# The same package as a directory
IMPL_DIR=$(MODULE)/impl
CLIENT_DEPS=$(IMPL_DIR)/*.java
SERVER_DEPS=$(IMPL_DIR)/*.java $(IMPL_DIR)/seq/*.java


build: idl $(CLIENT) $(SERVER)

idl: $(IDL_OUT)

$(IDL_OUT): $(IDL)
	$(IDLC_JAVA) $(IDLC_JAVAFLAGS) -fall $(IDL)

$(CLIENT): $(IMPL_DIR)/Client.java $(CLIENT_DEPS) $(IDL_OUT)
	mkdir -p $(TMP_DIR)
	$(JAVAC) $(JAVACFLAGS) -d $(TMP_DIR) $< $(MODULE)/_$(BASE)Stub.java
	$(JAR) $(JARFLAGS) cfe $@ $(IMPL_PK).Client -C $(TMP_DIR)/ .
	rm -rf $(TMP_DIR)

$(SERVER): $(IMPL_DIR)/Server.java $(SERVER_DEPS) $(IDL_OUT)
	mkdir -p $(TMP_DIR)
	$(JAVAC) $(JAVACFLAGS) -d $(TMP_DIR) $<
	$(JAR) $(JARFLAGS) cfe $@ $(IMPL_PK).Server -C $(TMP_DIR)/ .
	rm -rf $(TMP_DIR)

clean:
	rm -rf $(TMP_DIR) bin
	rm -f $(MODULE)/*.java
	rm -f $(CLIENT) $(SERVER)

install: build
	mkdir -p $(BIN)
	install -m 755 $(CLIENT) $(BIN)
	install -m 755 $(SERVER) $(BIN)