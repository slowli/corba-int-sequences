
"""Integer sequences with CORBA - sequence implementations"""

import math

class mat2(object):

    """2x2 matrix consisting of arbitrary precision integers."""

    def __init__(self, a00 = 1L, a01 = 0L, a10 = 0L, a11 = 1L):
        self.a00 = long(a00)
        self.a01 = long(a01)
        self.a10 = long(a10)
        self.a11 = long(a11)
        self.__powers2 = { 1 : self }

    def __mul__(self, other):
        if type(other) != mat2:
            raise TypeError("Both arguments of multiplication must be matrices")

        return mat2(self.a00 * other.a00 + self.a01 * other.a10, \
            self.a00 * other.a01 + self.a01 * other.a11, \
            self.a10 * other.a00 + self.a11 * other.a10, \
            self.a10 * other.a01 + self.a11 * other.a11)

    def __str__(self):
        return '[{0}, {1}, {2}, {3}]'.format(self.a00, self.a01, self.a10, self.a11)


def effective_pow(a, identity=1):
    cache = { 1 : a }

    def pow2pow(pow2):
        i = pow2
        while not cache.has_key(i): i >>= 1
        while i < pow2:
            i <<= 1
            cache[i] = cache[i/2] * cache[i/2]
        return cache[pow2]

    def eff_pow(exponent):
        pow2 = 1; res = identity
        while (exponent > 0):
            if exponent % 2 == 1:
                res *= pow2pow(pow2)
            exponent >>= 1
            pow2 <<= 1
        return res

    return eff_pow

__fib_pow = effective_pow(mat2(1, 1, 1, 0), identity=mat2(1, 0, 0, 1))

def fibonacci(i):
    if (i == 0): return 0
    mat2 = __fib_pow(i - 1)
    return mat2.a00

fibonacci.name = "Fibonacci numbers (Python)"
fibonacci.max_index = 2000000
fibonacci.description = """\
Fibonacci numbers, defined by equalities
    fib(i) = fib(i-1) + fib(i-2), fib(0) = 0, fib(1) = 1.
This implementation in Python uses 2x2 matrices and fast exponentation 
for calculations.

See http://en.wikipedia.org/wiki/Fibonacci_number, http://oeis.org/A000045"""

def pow2(i): return 1L << i

pow2.name = "Powers of 2 (Python)"
pow2.max_index = 1000000
pow2.description = """\
Powers of two, implemented with the bit shift operation:
    pow2(i) = 1L << i."""

def pow3_naive(i):
    res = 1L
    for i in range(i): res *= 3L
    return res

pow3_naive.name = "Powers of 3, naive (Python)"
pow3_naive.max_index = 1000000
pow3_naive.description = """\
Powers of three, implemented with the repeated multiplications."""

pow3 = effective_pow(3L, identity=1L)
pow3.name = "Powers of 3 (Python)"
pow3.max_index = 1000000
pow3.description = """\
Powers of three, implemented with the fast exponentation."""

class primes_seq(object):
    
    def __init__(self):
        self._primes = [ 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59 ]
    
    def add_primes(self, Min=1, Max=2):
        
        """ Adds primes in the given range to the list. """
        
        if (self._primes[-1] < math.sqrt(Max)):
            self.add_primes(Min=self._primes[-1] + 1, 
                    Max=int(math.sqrt(Max)))
            Min = int(math.sqrt(Max)) + 1
            
        numbers = [ True for _ in range(Min, Max + 1) ]
        
        for prime in self._primes:
            if prime > math.sqrt(Max): break
            for i in range(max(2, Min // prime), Max // prime + 1):
                if i * prime >= Min: numbers[i * prime - Min] = False
            
        lst = [ i for i in range(Min, Max + 1) if numbers[i - Min] ]
        self._primes += lst
    
    def est_max(self, index):
        
        """ Estimates the upper bound for a prime number with the given index. """
        
        fn = lambda x: x / math.log(x)

        lo_bound, hi_bound = 1, index ** 2
        while lo_bound + 1 < hi_bound:
            m = (lo_bound + hi_bound) / 2
            if fn(m) < index: lo_bound = m
            else: hi_bound = m

        return hi_bound
    
    def get(self, index):
        
        """ Retrieves i-th prime number. """
        
        if len(self._primes) <= index:
            est_max = self.est_max(index)
            self.add_primes(Min=self._primes[-1] + 1, Max=est_max)
        return self._primes[index]
