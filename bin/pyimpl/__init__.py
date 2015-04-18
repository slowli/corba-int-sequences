
import omniORB, _omnipy, sys

# A little hack to get rid of obsolete module directories created by omniidl
omniORB.updateModule("demo")
omniORB.updateModule("demo__POA")

if _omnipy.__version__[0] == '3':
    import pyimpl.omni3.IntegerSequence_idl as idl
elif _omnipy.__version__[0] == '4':
    import pyimpl.omni4.IntegerSequence_idl as idl
else:
    raise EnvironmentError("Incompatible omniORBpy version: " + _omnipy.__version__)

sys.modules['demo'] = idl._0_demo
sys.modules['demo__POA'] = idl._0_demo__POA