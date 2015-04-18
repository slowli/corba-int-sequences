
"""Integer sequences with CORBA - server Python script"""

import sys, time, omniORB, CosNaming
from omniORB import CORBA
from CosNaming import NamingContext, NamingContextExt, NameComponent

from pyimpl.sequences import *
from pyimpl.common import *
import demo, demo__POA
from demo import Response


class IntegerSequence_impl(demo__POA.IntegerSequence):
    """ Generic implementation of a number service"""

    def __init__(self, fn, name=None, max_index=None, description=None):
        self._name = name if not name is None else fn.name
        self._description = description if not description is None else fn.description
        self._maxIndex = max_index if not max_index is None else fn.max_index
        self._fn = fn

    def _get_name(self): return self._name

    def _get_description(self): return self._description

    def _get_maxIndex(self): return self._maxIndex

    def __compute(self, index):
        return self._fn(index)

    def __get_number(self, index):
        try:
            if index < 0:
                raise ValueError("Index cannot be negative")
            elif index > self._get_maxIndex():
                raise ValueError("Index is too big")
            else:
                val = self.__compute(index)
                if type(val) is int:
                    return Response(demo.t_int, val)
                else:
                    return Response(demo.t_string, str(val))
        except Exception, e:
            return Response(demo.t_error, e.message)

    def __str__(self):
        return self._get_name()

    def number(self, index):
        print "Requested number #{1} from sequence '{0}'".format(self, index)
        t_start = time.clock()
        response = self.__get_number(index)
        print "Spent {0} ms on computation".format((time.clock() - t_start) * 1000)
        return response

    def numbers(self, indices):
        print "Requested number(s) #{1} from sequence '{0}'".format(self, indices)
        t_start = time.clock()
        response = [ self.__get_number(i) for i in indices ]
        print "Spent {0} ms on computation".format((time.clock() - t_start) * 1000)
        return response

class Server(object):

    USAGE = """\
Usage: server [--list | --help]

Runs implementations of integer sequences.

Options:
    --list
        List implementations hosted by this server and exit.
    --help
        Print this help message and exit."""

    def __init__(self, args):
        self.process_args(args)

    def process_args(self, args):
        argi = 1
        while (argi < len(args)):
            arg = args[argi]
            argi += 1
            
            if (arg == '--help'):
                print self.USAGE
                sys.exit(0)
            elif (arg == '--list'):
                self.list_implementations()
                sys.exit(0)
            else:
                raise ArgumentError("Unknown argument: {0}.".format(arg))

    @property
    @cached
    def implementations(self):

        impl = dict();
        impl[Name(ID='fib', kind='py')] = IntegerSequence_impl(fibonacci)
        impl[Name(ID='pow2', kind='py')] = IntegerSequence_impl(pow2)
        impl[Name(ID='pow3', kind='py-naive')] = IntegerSequence_impl(pow3_naive)
        impl[Name(ID='pow3', kind='py')] = IntegerSequence_impl(pow3)
        
        impl[Name(ID='primes', kind='py')] = IntegerSequence_impl( \
                name="Primes (Python)", \
                max_index=500000, \
                description="Prime numbers implemented using the sieve of Eratosthenes.",
                fn=primes_seq().get)

        return impl

    def list_implementations(self):
        print "List of implementations hosted by this server:"
        for name, impl in self.implementations.iteritems():
            print '-' * 50
            print INFO_TEMPLATE.format(id=name.id, kind=name.kind, \
                name=impl._get_name(), \
                descr=impl._get_description(), \
                max=impl._get_maxIndex())

    def run(self):
        # Get object request brocker and POA manager
        directory = ServiceDir("integer-seq", True)
        directory.unbind_all(lambda name: name.kind.endswith("py"))

        impls = self.implementations
        for name, impl in impls.iteritems():
            directory.bind(name, impl)

        # Activate POA manager to properly address incoming calls
        poa = directory.orb.resolve_initial_references("RootPOA")
        poa_manager = poa._get_the_POAManager()
        poa_manager.activate()
        print "Ready for incoming requests..."
        directory.orb.run()

if __name__ == "__main__":
    try:
        server = Server(sys.argv)
        server.run()
    except ArgumentError, e:
        print e.message + "\nInvoke with `--help` option to get help."
        sys.exit(2)
    except Exception, e:
        print "Error: " + e.message
        if isinstance(e, ServiceError) and e.cause is not None:
            print "Cause: " + str(e.cause)
        sys.exit(1)
