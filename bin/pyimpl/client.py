
""" Integer sequences with CORBA - client Python script """

import sys, time, omniORB, CosNaming
from omniORB import CORBA
from CosNaming import NamingContextExt, NameComponent

import demo
from pyimpl.common import *

class IntegerSequence_proxy(object, demo.IntegerSequence):

    """ Proxy for integer sequence service. When accessing the service,
    proxy prints information about requests, received data, and timings. """

    def __init__(self, ref, name, shorten=False):
        """ Creates a proxy for a named Fibonacci numbers service. """

        self.name = name # this is the name in the CORBA system
        self.shorten = shorten
        self._reference = ref
        if self._reference is None:
            raise ServiceError("Invalid reference")

    def __str__(self):
        return self._get_name()

    def _get_name(self):
        return self._reference._get_name()

    def _get_description(self):
        return self._reference._get_description()

    def _get_maxIndex(self):
        return self._reference._get_maxIndex()
    
    def number(self, index):
        print "Performing request {0}({1})".format(self.name.id, index)
        t_start = time.time()
        val = self._reference.number(index)
        print "Request completed in {0} ms".format((time.time() - t_start) * 1000)
        self.print_val(index, val)
        return val

    def numbers(self, indices):
        args = ', '.join(str(x) for x in indices)
        print "Performing batch request {0}([{1}])".format(self.name.id, args)
        t_start = time.time()
        vals = self._reference.numbers(indices)
        print "Request completed in {0} ms".format((time.time() - t_start) * 1000)
        for i, val in zip(indices, vals): self.print_val(i, val)
        return vals

    def print_val(self, index, val):
        if (val._d == demo.t_error):
            print "Error getting {0}({1}): {2}".format(self.name.id, index, val.message)
        elif (val._d == demo.t_int):
            print "{0}({1}) = {2}".format(self.name.id, index, val.intVal)
        else:
            s = val.stringVal
            if self.shorten and len(s) > 50:
                s = s[:20] + "...[{0} digits skipped]...".format(len(s) - 40) + s[-20:]
            print "{0}({1}) = {2}".format(self.name.id, index, s)

class Client(object):

    USAGE = """\
Usage: client [option...] (sequence-ID | service-ID) index index...
       client (--list | --help)

Retrieves members of integer sequences using CORBA remoting.

Sequence ID is the identifier of an integer sequence, e.g. 'fib' (Fibonacci numbers).
Service ID is the identifier of a particular implementation of a sequence,
which consists of a sequence ID, dot '.', and a kind, e.g. 'pow3.naive-py'.

Indices are non-negative integers. Indexing starts with zero; e.g., 
fib(0) = 0 and fib(1) = 1. 

Options:
    --seq
         Perform a separate request for each index.
    --batch
         Perform a batch request for all indices (default).
    --short
         Print only 20 first and 20 last digits of received integers.
    --list
         Print the list of registered implementations and exit.
    --help
         Print this help message and exit.

Examples:
    client fib 5 6 7
    client --seq --short primes.py 10000 20000""";

    def __init__(self, args):
        # Service names to try to connect to.
        self.service_name = ''
        # Use one batch call or separate calls for every index?
        self.batch = True
        # Indices of numbers in the sequence to get.
        self.indices = []
        # Shorten long returned numbers?
        self.shorten = False

        self.directory = ServiceDir("integer-seq", False)
        self.process_args(args)

    def process_args(self, args):
        """ Processes command line arguments """

        if len(args) == 1:
            raise ArgumentError("No arguments specified.")

        argi = 1;
        while (argi < len(args)) and (args[argi][0:2] == '--'):
            arg = args[argi]

            if arg == '--seq':
                self.batch = False
            elif arg == '--batch':
                self.batch = True
            elif arg == '--short':
                self.shorten = True
            elif arg == '--help':
                print self.USAGE; sys.exit(0)
            elif arg == '--list':
                self.list_services(); sys.exit(0)
            else:
                raise ArgumentError("Unknown argument: {0}.".format(arg))
            argi += 1

        if (argi == len(sys.argv)):
            raise ArgumentError("No sequence specified.")
        else:
            self.service_name = sys.argv[argi]; argi += 1

        self.indices = [int(x) for x in sys.argv[argi:]]
        if (len(self.indices) > demo.MAX_QUERY_SIZE):
            raise ArgumentError("Too many indices specified. Specify no more than {0}" \
                    .format(demo.MAX_QUERY_SIZE))

    def list_services(self):
        names = self.directory.service_names
        
        print "Registered sequence implementations:"
        for name in names:
            print '-' * 50
            try:
                ref = self.directory.get(name, demo.IntegerSequence)
                print INFO_TEMPLATE.format(id=name.id, kind=name.kind, \
                    name=ref._get_name(), \
                    descr=ref._get_description(), \
                    max=ref._get_maxIndex())
            except ServiceError, ex:
                print ERROR_TEMPLATE.format(id=name.id, kind=name.kind, \
                    error=ex.message)

    def run(self):
        print "Getting service by sequence name '{0}'...".format(self.service_name)
        
        names = self.directory.service_names
        matcher = lambda name: str(name) == self.service_name \
            or name.id == self.service_name
        names = [ n for n in names if matcher(n) ]

        proxy = None
        for name in names:
            try:
                ref = self.directory.get(name, demo.IntegerSequence)
                proxy = IntegerSequence_proxy(ref, name, shorten=self.shorten)
                break
            except ServiceError, ex:
                print ex.message
                proxy = None
        
        if proxy is None:
            raise ServiceError("No available services that match the name '{0}'".format(self.service_name))

        print "Connected to service '{0}' (CORBA name: {1})".format(proxy, proxy.name)

        # Make call(s)
        if self.batch:
            proxy.numbers(self.indices)
        else:
            for i in self.indices: proxy.number(i)


if __name__ == "__main__":
    try:
        client = Client(sys.argv)
        client.run()
    except ArgumentError, e:
        print e.message + "\nInvoke with `--help` option to get help."
        sys.exit(2)
    except Exception, e:
        print "Error: " + e.message
        if isinstance(e, ServiceError) and e.cause is not None:
            print "Cause: " + str(e.cause)
        sys.exit(1)

