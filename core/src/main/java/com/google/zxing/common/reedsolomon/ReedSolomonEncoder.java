/*
 * Copyright 2008 ZXing authors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Implements Reed-Solomon encoding, as the name implies.</p>
 * @author Sean Owen
 * @author William Rucklidge
 */
public final class ReedSolomonEncoder {

	private final GenericGF field;
	private final List<GenericGFPoly> cachedGenerators;

	public ReedSolomonEncoder(GenericGF field) {
		this.field = field;
		this.cachedGenerators = new ArrayList<>();
		cachedGenerators.add(new GenericGFPoly(field, new int[]{1}));
	}

	private GenericGFPoly buildGenerator(int degree) {
		if (degree >= cachedGenerators.size()) {
			GenericGFPoly lastGenerator = cachedGenerators.get(cachedGenerators.size() - 1);
			for (int d = cachedGenerators.size(); d <= degree; d++) {
				GenericGFPoly nextGenerator = lastGenerator.multiply(
						new GenericGFPoly(field, new int[] { 1, field.exp(d - 1 + field.getGeneratorBase()) }));
				cachedGenerators.add(nextGenerator);
				lastGenerator = nextGenerator;
			}
		}
		return cachedGenerators.get(degree);
	}
	// ecBytes, is the number of parity check bits or n-k. In RS[7,3,5] over GF(8) this is n-k = 4 symbols.
	public void encode(int[] toEncode, int ecBytes) {

		if (ecBytes == 0) 
			throw new IllegalArgumentException("No error correction bytes");


		int dataBytes = toEncode.length - ecBytes;

		if (dataBytes <= 0) 
			throw new IllegalArgumentException("No data bytes provided");

		GenericGFPoly generator = buildGenerator(ecBytes);
		int[] infoCoefficients = new int[dataBytes];
		System.arraycopy(toEncode, 0, infoCoefficients, 0, dataBytes);
		// build the message polynomial
		GenericGFPoly info = new GenericGFPoly(field, infoCoefficients);
		info = info.multiplyByMonomial(ecBytes, 1);
		GenericGFPoly remainder = info.divide(generator)[1];
		int[] coefficients = remainder.getCoefficients();
		int numZeroCoefficients = ecBytes - coefficients.length;
		for (int i = 0; i < numZeroCoefficients; i++) {
			toEncode[dataBytes + i] = 0;
		}
		System.arraycopy(coefficients, 0, toEncode, dataBytes + numZeroCoefficients, coefficients.length);
	}
	//encode from binary
	public void encodeChekad(int[] toEncode, int ecBytes) {

		if (ecBytes == 0) {
			throw new IllegalArgumentException("No error correction bytes");
		}

		int dataBytes = toEncode.length - ecBytes;


		if (dataBytes <= 0) {
			throw new IllegalArgumentException("No data bytes provided");
		}
		GenericGFPoly generator = buildGenerator(ecBytes);
		int[] infoCoefficients = new int[toEncode.length];
		System.arraycopy(toEncode, 0, infoCoefficients, 0,toEncode.length);
		// build the message polynomial
		GenericGFPoly info = new GenericGFPoly(field, infoCoefficients);
		info = info.multiplyByMonomial(ecBytes, 1);
		GenericGFPoly remainder = info.divide(generator)[1];
		int[] coefficients = remainder.getCoefficients();
		int numZeroCoefficients = ecBytes - coefficients.length;
		for (int i = 0; i < numZeroCoefficients; i++) {
			toEncode[dataBytes + i] = 0;
		}
		System.arraycopy(coefficients, 0, toEncode, dataBytes + numZeroCoefficients, coefficients.length);
	}

	public int[] encodeFromBinary(int[] message) {
		
		//System.out.println("Transmitted message in binary!");
		//System.out.println(Arrays.toString(message));
		int dim = (int) (Math.log(field.getSize())/Math.log(2));
		if (message.length % dim != 0) {
			throw new IllegalArgumentException("No error correction bytes");
		} // using a MDS formula for RS codes we have the following:
		int dmin =(int) Math.pow(2, dim) - message.length/dim;
		System.out.printf("This code can correct up to %d errors%n", (dmin-1)/2);
		int[] m = field.bitarrayToGF(message);
		int[] toE= new int[field.getSize() - 1];
		System.arraycopy(m, 0, toE, 0, m.length);
		//System.out.println("Prepapred message in GF ( with padded zeros!)");
		//System.out.println(Arrays.toString(toE));	
		this.encode(toE,field.getSize()-1-dim);

		//System.out.println("Encoded message in GF last (n-k) symbols are parity");
		//System.out.println(Arrays.toString(toE));

		System.out.printf("Encoded ( transmitted) binary message. %nThe last (n-k)*m =%d bits are parity%n", (dmin ));
		System.out.printf("This code can correct up to %d errors%n", (dmin-1)/2);
		return GenericGF.toBinary(toE,2,dim);
		//System.out.println(Arrays.toString(toE));
	}
	public static void main(String[] args){
		ReedSolomonEncoder rse = new ReedSolomonEncoder(GenericGF.GF8);
		int[] message = new int[]{1,0,0,0,1,0,0,0,1};
		System.out.println(Arrays.toString(message));
		//rse.encodeFromBinary(message);
		System.out.println(Arrays.toString(rse.encodeFromBinary(message)));


	}


}
