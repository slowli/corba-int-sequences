package demo.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

/**
 * Directory (context) within CORBA naming space containing services with a specific type.
 * This is where CORBA-related code is concentrated.
 * 
 * @param T
 *    type of services
 */
public class ServiceDirectory<T extends org.omg.CORBA.Object> {

	/**
	 * Filters services by their name.
	 */
	public static interface NameFilter {
		
		/**
		 * Checks if the service name matches certain criteria.
		 * 
		 * @param name 
		 *    CORBA name to check
		 * @return
		 */
		boolean matches(Name name);
	}
	
	/** Maximal number of services in the directory. */
	private static final int MAX_SERVICES = 100;
	
	/** Create directory if it is absent? */
	private final boolean createIfAbsent;
	
	/** ORB to communicate with services. */
	public final ORB orb;
	
	/** Name of the directory within the CORBA name service. */
	public final String directoryName;
	
	/** Method to narrow CORBA objects. */
	private final Method narrowMethod;
	
	private NamingContextExt nameService = null;
	
	private POA rootPOA = null;
	
	private NamingContextExt serviceDir = null;
	
	/**
	 * Creates a service directory at the specified location.
	 * 
	 * @param orb
	 *    object request broker to use for CORBA-related operations
	 * @param directoryName
	 *    name of the directory
	 * @param helper
	 *    helper class to narrow CORBA object references
	 * @param createIfAbsent
	 *    create directory if it is absent?
	 * 
	 * @throws IllegalArgumentException
	 *    if helper class is invalid (does not have public static narrow method)
	 */
	public ServiceDirectory(ORB orb, String directoryName, Class<?> helper, boolean createIfAbsent) {
		this.orb = orb;
		this.createIfAbsent = createIfAbsent;
		this.directoryName = directoryName;
		
		final Class<?> objectCls = org.omg.CORBA.Object.class;
		try {
			this.narrowMethod = helper.getMethod("narrow", objectCls);
			if (!objectCls.isAssignableFrom(narrowMethod.getReturnType())) {
				throw new IllegalArgumentException("Invalid helper class: " + helper);
			}
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Invalid helper class: " + helper);
		}
	}
	
	/**
	 * Narrows a generic CORBA object into a service.  
	 * 
	 * @param obj
	 *    CORBA object to narrow
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private T narrow(org.omg.CORBA.Object obj) {
		try {
			return (T) this.narrowMethod.invoke(null, obj);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// Should not happen
			throw new RuntimeException("Could not narrow object");
		}
	}
	
	/**
	 * Returns the object request broker to work with CORBA.
	 */
	private ORB orb() {
		return this.orb;
	}
	
	/**
	 * Returns name service to resolve names to remote object references.
	 * 
	 * @return
	 *    name service object
	 *
	 * @throws ServiceException
	 *    if a CORBA-related error occurs while getting the naming service
	 */
	private NamingContextExt nameService() throws ServiceException {
		if (this.nameService == null) {
			try {
				org.omg.CORBA.Object obj = orb().resolve_initial_references("NameService");
				this.nameService = NamingContextExtHelper.narrow(obj);
			} catch (UserException | SystemException e) {
				throw new ServiceException("Failed to locate name service", e);
			}
		}
		return this.nameService;
	}
	
	/**
	 * Returns root portable object adapter (POA) service to convert {@linkplain Servant servants}
	 * to CORBA objects.
	 * 
	 * @return
	 *    root POA service
	 * @throws ServiceException
	 *    if a CORBA-related error occurs while getting the POA service
	 */
	private POA rootPOA() throws ServiceException {
		if (this.rootPOA == null) {
			try {
				rootPOA = POAHelper.narrow(this.orb().resolve_initial_references("RootPOA"));
				rootPOA.the_POAManager().activate();
			} catch (UserException | SystemException e) {
				throw new ServiceException("Failed to activate POA manager", e);
			}
		}
		
		return this.rootPOA;
	}
	
	/**
	 * Directory that contains services. If tools are in the server mode,
	 * the directory is created provided it doesn't exist before the call.
	 *
	 * @throws ServiceException
	 *    if a CORBA-related error occurs while getting or creating the directory
	 */
	private NamingContextExt serviceDir() throws ServiceException {
		if (this.serviceDir == null) {
			NamingContextExt ns = this.nameService();
			NameComponent[] name = null;
			
			try {
				name = ns.to_name(this.directoryName);
			} catch (UserException | SystemException e) {
				throw new ServiceException("Invalid service directory name", e);
			}
			
			org.omg.CORBA.Object obj = null;
			
			if (this.createIfAbsent) {
				// Attempt to create a service directory first
				try {
					obj = ns.bind_new_context(name);
				} catch (AlreadyBound e) {
					// Directory already exists, we just have to get the corresponding naming context
					obj = null;
				} catch (UserException | SystemException e) {
					throw new ServiceException("Failed to create services directory", e);
				}
			}
			
			try {
				if (obj == null) obj = ns.resolve(name);
				this.serviceDir = NamingContextExtHelper.narrow(obj);
			} catch (UserException | SystemException e) {
				throw new ServiceException("Failed to get services directory", e);
			}
		}
		
		return this.serviceDir;
	}
	
	/**
	 * Retrieves names of currently bound services. Some of these services may be unavailable.
	 *
	 * @throws ServiceException
	 *    if a CORBA-related error occurs while getting service names
	 */
	public Collection<Name> serviceNames() throws ServiceException {
		NamingContextExt dir = this.serviceDir();
		BindingListHolder list = new BindingListHolder();
		BindingIteratorHolder it = new BindingIteratorHolder();
		dir.list(MAX_SERVICES, list, it);
		
		List<Name> serviceNames = new ArrayList<Name>();
		for (Binding binding : list.value) {
			serviceNames.add(new Name(binding.binding_name[0]));
		}
		return serviceNames;
	}
	
	/**
	 * Resolves a name to a service.
	 * 
	 * @param name
	 *    name to resolve
	 * @return
	 *    resolved service
	 *    
	 * @throws ServiceException
	 *    if a CORBA-related error occurs while resolving the service name
	 */
	public T resolve(Name name) throws ServiceException {
		NamingContextExt dir = this.serviceDir();
		try {
			org.omg.CORBA.Object obj = dir.resolve(name.toComponents());
			T narrowedObj = this.narrow(obj);
			narrowedObj._non_existent();
			return narrowedObj;
		} catch (UserException | SystemException e) {
			throw new ServiceException("Failed to access service name " + name, e);
		}
	}
	
	/**
	 * Binds or rebinds an object as a child of a service directory.
	 * 
	 * @param name
	 *    name to assign to the object
	 * @param servant
	 * 
	 * @throws ServiceException
	 *    if a CORBA-related error occurs during operation
	 */
	public void bind(Name name, Servant servant) throws ServiceException {
		System.out.format("Binding implementation to name %s\n", name);
		
		try {
			org.omg.CORBA.Object obj = this.rootPOA().servant_to_reference(servant);
			obj = this.narrow(obj);
			this.serviceDir().rebind(name.toComponents(), obj);
		} catch (UserException | SystemException e) {
			throw new ServiceException("Failed to bind implementation", e);
		}
	}
	
	/**
	 * Unbinds all services with the name matching the specified predicate.
	 * 
	 * @param filter
	 *    predicate to filter services
	 *
	 * @throws ServiceException
	 *    if a CORBA-related error occurs during operation
	 */
	public void unbindAll(NameFilter filter) throws ServiceException {
		for (Name name : this.serviceNames()) {
			if (filter.matches(name)) {
				System.out.format("Unbinding name %s\n", name);
				try {
					this.serviceDir().unbind(name.toComponents());
				} catch (UserException | SystemException e) {
					throw new ServiceException("Failed to unbind " + name, e);
				}
			}
		}
	}
}
