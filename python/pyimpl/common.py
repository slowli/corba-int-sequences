
"""Common tools for client and server."""

import omniORB, CosNaming
from omniORB import CORBA
from CosNaming import NamingContext, NamingContextExt, NameComponent

# Templates for listing sequence implementations

INFO_TEMPLATE = """\
Sequence ID: {id}, kind: {kind}
Name: {name}
Maximal supported index: {max}
Description:
{descr}"""

ERROR_TEMPLATE = """\
Sequence ID: {id}, kind: {kind}
{error}"""

def cached(fn, hash_args=lambda *args: hash(args)):
    """ Caches results of function calls. """
    
    _cache = dict()

    def cached_fn(*args):
        hash = hash_args(*args)
        if not _cache.has_key(hash):
            _cache[hash] = fn(*args)
        return _cache[hash]
    return cached_fn

class ArgumentError(ValueError):
    """ Exception thrown when there is a problem parsing program arguments. """

class ServiceError(RuntimeError):
    """ Exception thrown to indicate a CORBA-related problem. """

    def __init__(self, message, cause=None):
        RuntimeError.__init__(self, message)
        self.cause = cause

class Name(object):
    def __init__(self, component=None, ID='', kind=''):
        if not component is None:
            self.id, self.kind = component.id, component.kind
        else:
            self.id, self.kind = ID, kind

    def __str__(self): 
        return self.id + '.' + self.kind

    @property
    def components(self):
        return [ NameComponent(self.id, self.kind) ]

class ServiceDir(object):

    """ Directory (context) within CORBA naming space containing services with a specific type. """

    def __init__(self, dir_name, create=False):
        self.create = create
        self.dir_name = dir_name

    @property
    @cached
    def orb(self):
        return CORBA.ORB_init([], CORBA.ORB_ID)

    @property
    @cached
    def name_service(self):
        """ Gets the CORBA name service to resolve service names to object references. """

        try:
            obj = self.orb.resolve_initial_references("NameService")
            return obj._narrow(CosNaming.NamingContextExt)
        except CORBA.Exception, ex:
            raise ServiceError("Failed to locate name service", ex)

    @property
    @cached
    def service_dir(self):
        """ Directory that contains services. If the corresponding option is set,
        the directory is created provided it doesn't exist before the call. """
        
        ns = self.name_service
        try:
            name = ns.to_name(self.dir_name)
        except CORBA.Exception, ex:
            raise ServiceError("Invalid service directory name", ex)

        context = None
        if self.create:
            try:
                context = ns.bind_new_context(name)
            except NamingContext.AlreadyBound:
                context = None
            except CORBA.Exception, ex:
                raise ServiceError("Failed to create services directory", ex)

        try:
            if context is None:
                context = self.name_service.resolve(name)
                context = context._narrow(CosNaming.NamingContextExt)
                return context
        except CORBA.Exception, ex:
            raise ServiceError("Failed to get services directory", ex)

    @property
    def service_names(self):
        """Returns names of bound services (relative to the service directory)."""

        try:
            sdir = self.service_dir
            bindings, b_iter = sdir.list(100)
            return [Name(component=b.binding_name[0]) for b in bindings]
        except CORBA.Exception, ex:
            raise ServiceError("Failed to list services", ex)

    def get(self, name, obj_type):
        """ Resolves a name to a service. """

        try:
            obj = self.service_dir.resolve(name.components)
            obj = obj._narrow(obj_type)
            obj._non_existent()
            return obj
        except CORBA.Exception, ex:
            raise ServiceError("Failed to access service name {0}".format(name), ex)

    def bind(self, name, obj):
        """ Binds or rebinds an object as a child of a service directory. """
        
        print "Binding implementation '{1}' to name {0}".format(name, obj)
        self.service_dir.rebind(name.components, obj._this())

    def unbind_all(self, filter=lambda: True):
        """ Unbinds all services with the name matching the specified predicate. """
        
        for n in self.service_names:
            if filter(n):
                print "Unbinding name {0}".format(n)
                self.service_dir.unbind(n.components)
