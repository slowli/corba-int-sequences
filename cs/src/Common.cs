using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.Remoting.Channels;
using Ch.Elca.Iiop;
using Ch.Elca.Iiop.Services;
using omg.org.CORBA;
using omg.org.CosNaming;
using omg.org.CosNaming.NamingContext_package;

namespace demo.impl {

	#region Utilities

	internal static class StringUtils {
		/// <summary>
		/// Delimiter between listed integer sequences.
		/// </summary>
		public const string Delimiter = "--------------------------------------------------";

		/// <summary>
		/// Prints information about a single integer sequences.
		/// </summary>
		/// <param name="sequence">sequence to list</param>
		/// <param name="corbaName">name of the sequence in the CORBA system</param>
		public static void PrintInfo(IntegerSequence sequence, Name corbaName) {
			Console.WriteLine("Sequence ID: {0}, kind: {1}", corbaName.ID, corbaName.Kind);
			Console.WriteLine("Name: {0}", sequence.name);
			Console.WriteLine("Maximal supported index: {0}", sequence.maxIndex);
			Console.WriteLine("Description:\n{0}", sequence.description);
		}

		public static void PrintErroneousInfo(ServiceException ex, Name corbaName) {
			Console.WriteLine("Sequence ID: {0}, kind: {1}", corbaName.ID, corbaName.Kind);
			Console.WriteLine(ex.Message);
		}
	}

	#endregion

	#region Exceptions

	/// <summary>
	/// Exception thrown to indicate a CORBA-related problem.
	/// </summary>
	public sealed class ServiceException : InvalidOperationException {
		public ServiceException(string message) : base(message) { }

		public ServiceException(string message, Exception e)
			: base(message, e) { }
	}

	/// <summary>
	/// Exception thrown when there is a problem parsing program arguments.
	/// </summary>
	public class CLIArgumentException : ArgumentException {
		public CLIArgumentException(string message) : base(message) { }
	}

	#endregion

	#region CORBA names

	/// <summary>
	/// CORBA name that identifies a service.
	/// </summary>
	/// <seealso cref="omg.org.CosNaming.NameComponent"/>
	public struct Name {
		
		/// <summary>
		/// Identifier of the service, e.g. "fib".
		/// </summary>
		public string ID;

		/// <summary>
		/// Kind of the service, e.g. "naive-cs".
		/// </summary>
		public string Kind;

		public Name(NameComponent component) {
			this.ID = component.id;
			this.Kind = component.kind;
		}

		/// <summary>
		/// Converts name to a list of components.
		/// </summary>
		public NameComponent[] Components {
			get {
				return new NameComponent[1] { new NameComponent(this.ID, this.Kind) };
			}
		}

		public override string ToString() {
			return this.ID + "." + this.Kind;
		}
	}

	#endregion

	#region Service directories

	/// <summary>
	/// Directory (context) within CORBA name service containing services with the specified type.
	/// </summary>
	/// <typeparam name="T">Type of services in the directory; it should be an interface 
	/// created by translating IDL file</typeparam>
	public class ServiceDir<T> where T : class {

		/// <summary>
		/// Host where the name service is running.
		/// </summary>
		public string NameServiceHost = "localhost";

		/// <summary>
		/// Port where the name service is running.
		/// </summary>
		public int NameServicePort = 2809;

		/// <summary>
		/// Create directory if it is absent?
		/// </summary>
		private readonly bool CreateIfAbsent = false;

		/// <summary>
		/// Name of the directory within the CORBA name service.
		/// </summary>
		private readonly string DirectoryName = "";

		/// <summary>
		/// Creates a service directory.
		/// </summary>
		/// <param name="name">name of the directory meeting CORBA name service conventions</param>
		/// <param name="createIfAbsent">create directory if it is absent?</param>
		public ServiceDir(string name, bool createIfAbsent) {
			this.DirectoryName = name;
			this.CreateIfAbsent = createIfAbsent;
		}

		private NamingContextExt _NameService;

		/// <summary>
		/// Name service of the object request broker. The service resolves names to remote object references.
		/// </summary>
		/// <exception cref="ServiceException">
		/// if a CORBA-related error occurs while getting the naming service
		/// </exception>
		private NamingContextExt NameService {
			get {
				if (this._NameService == null) {
					IChannel channel = ChannelServices.GetChannel("IIOPChannel");

					if (channel == null) {
						if (CreateIfAbsent) channel = new IiopChannel(8087); else channel = new IiopClientChannel();
					}
					ChannelServices.RegisterChannel(channel, false);

					try {
						CorbaInit init = CorbaInit.GetInit();
						this._NameService = (NamingContextExt) init.GetNameService(NameServiceHost, NameServicePort);
						// Invoke a method to see if the name service is actually working
						this._NameService.to_name("testing");
					} catch (AbstractCORBASystemException e) {
						throw new ServiceException("Failed to locate name service", e);
					} catch (TargetInvocationException e) {
						throw new ServiceException("Failed to locate name service", e.InnerException);
					}
				}

				return _NameService;
			}
		}

		private NamingContextExt _DirContext = null;

		/// <summary>
		/// Directory that contains services. If the corresponding option is set,
		/// the directory is created provided it doesn't exist before the call.
		/// </summary>
		/// <exception cref="ServiceException">
		/// if a CORBA-related error occurs while getting or creating the directory
		/// </exception>
		private NamingContextExt DirContext {
			get {
				if (_DirContext == null) {
					NameComponent[] name = null;
			
					try {
						name = this.NameService.to_name(this.DirectoryName);
					} catch (AbstractCORBASystemException e) {
						throw new ServiceException("Invalid service directory name", e);
					}
			
					NamingContextExt dir = null;
			
					if (this.CreateIfAbsent) {
						// Attempt to create a service directory first
						try {
							dir = (NamingContextExt) NameService.bind_new_context(name);
						} catch (TargetInvocationException e) {
							if (e.InnerException.GetType() == typeof(AlreadyBound)) {
								// Directory already exists
								dir = null;
							} else {
								throw new ServiceException("Failed to create services directory", e.InnerException);
							}
						} catch (AbstractCORBASystemException e) {
							throw new ServiceException("Failed to create services directory", e);
						}
					}
			
					try{
						if (dir == null) dir = (NamingContextExt) NameService.resolve(name);
					} catch (AbstractCORBASystemException e) {
						throw new ServiceException("Failed to get services directory", e);
					} catch (TargetInvocationException e) {
						throw new ServiceException("Failed to get services directory", e.InnerException);
					}

					this._DirContext = dir;
				}

				return _DirContext;
			}
		}

		/// <summary>
		/// Names of currently bound services. Some of these services may be unavailable.
		/// </summary>
		/// <exception cref="ServiceException">
		/// if a CORBA-related error occurs while getting service names
		/// </exception>
		public List<Name> ServiceNames {
			get {
				List<Name> names = new List<Name>();

				Binding[] bindings;
				BindingIterator iterator;
				DirContext.list(100, out bindings, out iterator);
				foreach (Binding binding in bindings) {
					names.Add(new Name(binding.binding_name[0]));
				}
				return names;
			}
		}

		/// <summary>
		/// Resolves a name to a service.
		/// </summary>
		/// <param name="name">name to resolve</param>
		/// <returns>resolved service</returns>
		/// <exception cref="ServiceException">
		/// if a CORBA-related error occurs while resolving the service name
		/// </exception>
		public T Resolve(Name name) {
			try {
				MarshalByRefObject reference = this.DirContext.resolve(name.Components);
				// Touch the object to immediately fail if it is unavailable
				OrbServices.GetSingleton().non_existent(reference); 
				// Jeez, C#...
				return (T) ((object) reference);
			} catch (AbstractCORBASystemException e) {
				throw new ServiceException("Failed to resolve service name " + name, e);
			} catch (TargetInvocationException e) {
				throw new ServiceException("Failed to resolve service name " + name, e.InnerException);
			}
		}

		/// <summary>
		/// Binds or rebinds an object as a child of a service directory. 
		/// </summary>
		/// <param name="name">name to assign to the object</param>
		/// <param name="obj"></param>
		/// <exception cref="ServiceException">if a CORBA-related error occurs during operation</exception>
		public void Bind<U>(Name name, U obj) where U : MarshalByRefObject, T {
			Console.WriteLine("Binding implementation {1} to name {0}", name, obj);
			try {
				this.DirContext.rebind(name.Components, obj);
			} catch (AbstractCORBASystemException e) {
				throw new ServiceException("Failed to bind implementation " + name, e);
			} catch (TargetInvocationException e) {
				throw new ServiceException("Failed to bind implementation " + name, e.InnerException);
			}
		}

		/// <summary>
		/// Unbinds all services with the name matching the specified predicate.
		/// </summary>
		/// <param name="matcher">predicate to check the service names against</param>
		/// <exception cref="ServiceException">if a CORBA-related error occurs during operation</exception>
		public void UnbindAll(Predicate<Name> matcher) {
			foreach (Name name in this.ServiceNames) {
				if (matcher(name)) {
					Console.WriteLine("Unbinding name {0}", name);
					try {
						this.DirContext.unbind(name.Components);
					} catch (AbstractCORBASystemException e) {
						throw new ServiceException("Failed to unbind " + name, e);
					} catch (TargetInvocationException e) {
						throw new ServiceException("Failed to unbind " + name, e.InnerException);
					}
				}
			}
		}
	}

	/// <summary>
	/// Concrete implementation of abstract service directory for dealing with integer sequences.
	/// </summary>
	internal sealed class IntegerSequenceDir : ServiceDir<demo.IntegerSequence> {

		private const string DirName = "integer-seq";

		public IntegerSequenceDir(bool createIfAbsent)
			: base(DirName, createIfAbsent) {
		}
	}

	#endregion
}
