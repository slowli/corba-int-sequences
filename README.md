corba-int-sequences
===============

Interaction between programs using CORBA on the example of integer sequences.

CORBA (Common Object Request Broker Architecture) is one of more popular solutions 
to the problem of interaction between heterogeneous program components. 
Using CORBA, clients can interact with remote services as if they were local objects.

A little demonstration of CORBA capabilities is using this technology 
to implement integer sequences such as Fibonacci numbers or prime numbers 
in various environments. This allows to estimate the efficiency behind 
each implementation (and programming language), too.

Contents
---------------

 * **/bin** contains compiled client and server applications 
   with command-line interface
 * **/bin/server-java.jar**, **/bin/client-java.jar** – 
   executable JAR-archives of Java server and client
 * **/bin/server-py[.bat]**, **/bin/client-py[.bat]** – scripts for launching 
   Python server / client
 * **/bin/server-cs.exe**, **/bin/client-cs.exe** – C# executables
 * **/cs** – C# source files
 * **/java** – Java source files
 * **/python** – Python source files
 * **IntegerSequence.idl** – integer sequence interface description
 * **Makefile**, **Makevars** – script and variables used fo automatic builds
 * **NMakefile**, **NMakevars** – same files for Microsoft nmake tool

You may use `--help` option on client and server programs to get help.

Requirements
---------------

 * omniORBpy for Python client and server
 * JRE 1.7+ to launch Java programs
 * Microsoft .NET Framework or compatible Mono framework to run C# programs; 
   IIOP.NET library (included into the project)
 * CORBA name service, e.g. omniNames included in omniORB distribution, 
   or orbd from Java Development Kit. The service should run on port 2809. 
   It is the default port for omniNames; to bind orbd to this port, 
   one should start it with `-ORBInitialPort 2809` option.

Building From Sources
---------------

**Linux.** To build from sources, you should run `make && make install` 
in the root directory of the project. Compiled files are installed 
into **/bin** directory (can be changed in Makevars file).

**Windows.** To build from sources, you could use **nmake.exe** tool 
from Microsoft Visual Studio development kit. Run

`  nmake /f NMakefile && nmake /f NMakefile install`

in the root directory of the project.

Make targets:
 * **build** (default) – compiles client and server programs for each of the three 
   programming languages used in the project
 * **cs**, **java**, **python** – compiles client and server programs 
   for the corresponding programming language
 * **clean** – deletes compiled files
 * **install** – installs compiled files into **/bin** directory.

Usage
---------------

Linux:
 1. Enter **/bin** directory of the project.
 2. Launch CORBA naming service: `orbd -ORBInitialPort 2809 &`.
 3. List implemented integer sequences in the Python server: `./server-py --list`.
 4. Launch Python server: `./server-py > server-py.log &`.
 5. List registered sequences with the Java client: `./client-java.jar --list`.
 6. Retrieve the 100th, the 200th and the 300th Fibonacci numbers with C# client:  
    `./client-cs.exe fib 100 200 300`.

Windows (same actions):
 1. cd bin
 2. start orbd -ORBInitialPort 2809
 3. server-py --list
 4. start server-py
 5. client-java.jar --list
 6. client-cs fib 100 200 300

