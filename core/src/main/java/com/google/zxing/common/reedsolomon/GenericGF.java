/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/**
 * <p>This class contains utility methods for performing mathematical operations over
 * the Galois Fields. Operations use a given primitive polynomial in calculations.</p>
 *
 * <p>Throughout this package, elements of the GF are represented as an {@code int}
 * for convenience and speed (but at the cost of memory).
 * </p>
 *
 * @author Sean Owen
 * @author David Olivier
 */
public final class GenericGF {
	// class members
	public static final GenericGF AZTEC_DATA_12 = new GenericGF(0x1069, 4096, 1); // x^12 + x^6 + x^5 + x^3 + 1
	public static final GenericGF GF8 = new GenericGF(0xB, 8, 1); // x^3 + x + 1
	public static final GenericGF AZTEC_DATA_10 = new GenericGF(0x409, 1024, 1); // x^10 + x^3 + 1
	public static final GenericGF AZTEC_DATA_6 = new GenericGF(0x43, 64, 1); // x^6 + x + 1
	public static final GenericGF AZTEC_PARAM = new GenericGF(0x13, 16, 1); // x^4 + x + 1
	public static final GenericGF QR_CODE_FIELD_256 = new GenericGF(0x011D, 256, 0); // x^8 + x^4 + x^3 + x^2 + 1
	public static final GenericGF DATA_MATRIX_FIELD_256 = new GenericGF(0x012D, 256, 1); // x^8 + x^5 + x^3 + x^2 + 1
	public static final GenericGF AZTEC_DATA_8 = DATA_MATRIX_FIELD_256;
	public static final GenericGF MAXICODE_FIELD_64 = AZTEC_DATA_6;

	private final int[] expTable;
	private final int[] logTable;
	private final GenericGFPoly zero;
	private final GenericGFPoly one;
	private final int size;
	private final int primitive;
	private final int generatorBase;

	/**
	 * Create a representation of GF(size) using the given primitive polynomial.
	 *
	 * @param primitive irreducible polynomial whose coefficients are represented by
	 *  the bits of an int, where the least-significant bit represents the constant
	 *  coefficient
	 * @param size the size of the field
	 * @param b the factor b in the generator polynomial can be 0- or 1-based
	 *  (g(x) = (x+a^b)(x+a^(b+1))...(x+a^(b+2t-1))).
	 *  In most cases it should be 1, but for QR code it is 0.
	 */
	public GenericGF(int primitive, int size, int b) {
		this.primitive = primitive;
		this.size = size;
		this.generatorBase = b;

		expTable = new int[size];
		logTable = new int[size];
		int x = 1;
		for (int i = 0; i < size; i++) {
			expTable[i] = x;
			x *= 2; // we're assuming the generator alpha is 2
			if (x >= size) {
				x ^= primitive;
				x &= size - 1;
			}
		}
		for (int i = 0; i < size - 1; i++) {
			logTable[expTable[i]] = i;
		}
		// logTable[0] == 0 but this should never be used
		zero = new GenericGFPoly(this, new int[]{0});
		one = new GenericGFPoly(this, new int[]{1});
	}

	GenericGFPoly getZero() {
		return zero;
	}

	GenericGFPoly getOne() {
		return one;
	}

	/**
	 * @return the monomial representing coefficient * x^degree
	 */
	GenericGFPoly buildMonomial(int degree, int coefficient) {
		if (degree < 0) {
			throw new IllegalArgumentException();
		}
		if (coefficient == 0) {
			return zero;
		}
		int[] coefficients = new int[degree + 1];
		coefficients[0] = coefficient;
		return new GenericGFPoly(this, coefficients);
	}

	/**
	 * Implements both addition and subtraction -- they are the same in GF(size).
	 *
	 * @return sum/difference of a and b
	 */
	static int addOrSubtract(int a, int b) {
		return a ^ b;
	}

	/**
	 * @return 2 to the power of a in GF(size)
	 */
	int exp(int a) {
		return expTable[a];
	}

	/**
	 * @return base 2 log of a in GF(size)
	 */
	int log(int a) {
		if (a == 0) {
			throw new IllegalArgumentException();
		}
		return logTable[a];
	}

	/**
	 * @return multiplicative inverse of a
	 */
	int inverse(int a) {
		if (a == 0) {
			throw new ArithmeticException();
		}
		return expTable[size - logTable[a] - 1];
	}

	/**
	 * @return product of a and b in GF(size)
	 */
	int multiply(int a, int b) {
		if (a == 0 || b == 0) {
			return 0;
		}
		return expTable[(logTable[a] + logTable[b]) % (size - 1)];
	}

	public int getSize() {
		return size;
	}

	public int getGeneratorBase() {
		return generatorBase;
	}

	@Override
	public String toString() {
		return "GF(0x" + Integer.toHexString(primitive) + ',' + size + ')';
	} 
	public int[] bitarrayToGF(int[] kmBits){
		int m = (int) (Math.log(size)/Math.log(2));
		if (kmBits.length % m != 0) 
			throw new IllegalArgumentException("input must be multiple of m, dimension of GF");
		int[] res = new int[kmBits.length/m];
		for(int i=0;i<res.length;i++){
			for(int count = m-1; count >-1;count--){
				res[i] += (Math.pow(2, count)*kmBits[3*i+m-1-count]);
			}
		} 
		return res;
	}

	public static  int[] toBinary(int number, int base) {
		int m =(int) ( Math.log(number)/Math.log(base))+1;
		int[] ret = new int[m];
		boolean temp;
		for (int i = 0; i < m; i++) {
			temp = (1 << i & number) != 0;
			ret[m - 1 - i] = temp == true? 1: 0;
		}
		return ret;
	}
	// returns binary of fixed digit size
	public static int[] toBinary(int number, int base, final int sz) {
		int m = sz;//(int) ( Math.log(sz)/Math.log(base));
		int l = (number == 0 ? 1: (int) ( Math.log(number)/Math.log(base))+1);
		int[] ret = new int[sz], aux = new int[l];
		boolean temp;
		for (int i = 0; i < l; i++) {
			temp = (1 << i & number) != 0;
			aux[l-1-i] = temp == true? 1: 0;
		}
		System.arraycopy(aux, 0, ret, m-l, l);
		return ret;
	}
	
	// this is slow, to speed up use ArrayList

	public static int[] toBinary(int[] nums, int base) {
		int resLen = 0, mi = 0;
		for(int i= 0;i<nums.length;i++ ){
			mi = (nums[i]==0? 1: (int) ( Math.log(nums[i])/Math.log(base)+1));
			resLen +=(nums[i]==0? 1: (int) ( Math.log(nums[i])/Math.log(base)+1));
		}
		int[] ret = new int[resLen], temp;
		resLen = 0;  
		for (int i = 0; i < nums.length; i++) {	
			mi = (nums[i]==0? 1: (int) ( Math.log(nums[i])/Math.log(base)+1));
			temp = toBinary(nums[i],base);
			System.arraycopy(temp, 0, ret, resLen, mi);
			resLen += mi;
		}
		return ret;
	}
	
	// this retuns the binary sequence each symol of length m
	public static  int[] toBinary(int[] nums, int base, int sz) {
		int resLen = 0, m = sz;
		int[] ret = new int[m*nums.length], temp;
		//System.out.println("\n"+ret.length );
		resLen = 0;  
		for (int i = 0; i < nums.length; i++) {
			temp = toBinary(nums[i],base, sz);
			System.arraycopy(temp, 0, ret, resLen, m);
			//System.out.println("i,numsi, mi,resLen "+ i + " " +nums[i]+ " "+ mi + " "+resLen );
			resLen += m;
		}
		return ret;
	}
	
	


	public static void main(String[] args){
		// GF(8)..... 0 = 000, 1 = 001, ... 7 = 111; Thus 7*2 = 101 in base 2 or 5
		//System.out.println( GF8);
		//System.out.println( GF8.multiply(7, 2));

		int[] r =GF8.bitarrayToGF(new int[]{0,0,1,1,1,0,1,1,1});
		for(int i = 0; i<r.length;i++)
			System.out.println(r[i]);

		int[] rr =toBinary(3,2,3);
		System.out.println("rr is:"); 
		for(int i = 0; i<rr.length;i++)
			System.out.print(rr[i]);
		System.out.println();

		int[] toB = toBinary(r,2,3);
		for(int i = 0; i<toB.length;i++)
			System.out.print(toB[i]);
		
		// Here we can add random noise using method addNoiseAtLoci


	}


}