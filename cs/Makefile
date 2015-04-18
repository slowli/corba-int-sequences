ROOT_DIR=..
include $(ROOT_DIR)/Makevars

IDL_OUT=$(BASE).dll
IIOP_LIB=IIOPChannel.dll

# Source directory
SRC=src
CLIENT=client-cs.exe
CLIENT_SRC=$(SRC)/Client.cs $(SRC)/Common.cs $(SRC)/IntegerSequenceProxy.cs
SERVER=server-cs.exe
SERVER_SRC=$(SRC)/Server.cs $(SRC)/Common.cs $(SRC)/IntegerSequenceImpl.cs $(SRC)/Seq.*.cs

build: idl $(CLIENT) $(SERVER)

idl: $(IDL_OUT)

$(IDL_OUT): $(IDL)
	$(IDLC_CS) $(IDLC_CSFLAGS) $(BASE) $(IDL)

$(CLIENT): $(CLIENT_SRC) $(IDL_OUT)
	$(CSC) $(CSCFLAGS) -out:$@ -r:$(IDL_OUT),$(IIOP_LIB) $(CLIENT_SRC)
	
$(SERVER): $(SERVER_SRC) $(IDL_OUT)
	$(CSC) $(CSCFLAGS) -out:$@ -r:$(IDL_OUT),$(IIOP_LIB),System.Numerics.dll $(SERVER_SRC)

clean:
	rm -f $(IDL_OUT) $(CLIENT) $(SERVER)

install: build
	mkdir -p $(BIN)
	install -m 644 $(IDL_OUT) $(BIN)
	install -m 644 $(IIOP_LIB) $(BIN)
	install -m 755 $(CLIENT) $(BIN)
	install -m 755 $(SERVER) $(BIN)