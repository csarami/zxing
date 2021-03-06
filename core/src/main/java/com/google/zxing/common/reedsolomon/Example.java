import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

public class Example {

	public static void print2ASCII(String s){
		byte[] bytes = s.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes)
		{
			int val = b;
			for (int i = 0; i < 8; i++)
			{
				binary.append((val & 128) == 0 ? 0 : 1);
				val <<= 1;
			}
			binary.append(' ');
		}
		System.out.println("'" + s + "' to binary: " + binary);
	}

	public static int[] toASCII(String s,int len){
		byte[] bytes = s.getBytes();
		int[] res=new int[len];
		int count = 0;
		for (byte b : bytes)
		{  

			int val = b;  
			for (int i = 0; i < 8; i++)
			{  
				while(count<len){
					res[count]= (val & 128) == 1 ? 0 : 1;
					count++;
					val <<= 1;
				}
			}
		}
		return res;
	}

	public static int[] toASCII(String s){
		byte[] bytes = s.getBytes();
		int[] res=new int[s.length()*8];
		int count = 0;
		for (byte b : bytes)
		{ 
			int val = b;  
			for (int i = 0; i < 8; i++)
			{  
				res[count]=(val & 128) == 0 ? 0 : 1;
				count++;
				val <<= 1;
			}
		}
		return res;
	}


	public static int[] randWeigtedSeq(int weight, int len){

		return new Random().ints(0, len).distinct().limit(weight).toArray();
	}

	public static void addNoiseAtLoci(int[] nLoc, int[] target){
		for(int loc :nLoc)
			target[loc] = (target[loc]==1 ? 0 : 1);
	}


	/* encode a bit array
    int [] data – an array of integer (with size n), each element is either zero or one (that is, they mimic a bit array)
    int m – an integer
    returns int [] – the encoded version of data, each element is either zero or one, and the original data is recoverable
     if no more than n-m elements are incorrect
     for irreducible poly over GF(2) dgree between 10-20:
      ref: http://www.ams.org/journals/mcom/1972-26-120/S0025-5718-1972-0313227-5/S0025-5718-1972-0313227-5.pdf
     
	 //incomplete
	public static int [] encode (int [] infoSymbols, int m){
		int[] infoBits = GenericGF.bitarrayToGF(number, base, sz);
		GenericGF F;
		if(infoSymbols.length < 8)
			F = GenericGF.GF8;
		else if(infoSymbols.length < 16 )
			F = GenericGF.AZTEC_PARAM;
		else if(infoSymbols.length < 64 )
			F = GenericGF.AZTEC_DATA_6;
		else if(infoSymbols.length < 256 )
			F = GenericGF.DATA_MATRIX_FIELD_256;
		else if(infoSymbols.length < 1024 )
			F = GenericGF.AZTEC_DATA_10;
		else if(infoSymbols.length < 2048 )
			F = GenericGF.AZTEC_DATA_12;
		else{
			System.out.println("Galios Feild is not provided. Find one yourself and substitute it for letter F.");
			return new int[]{0};
		}
		int k;
		if(F.getSize()-1-infoSymbols.length %2 == 0){
			k = infoSymbols.length;
		}
		else if (F.getSize()-1-infoSymbols.length >=3){
			int [] infoSymbolsEx = new int[infoSymbols.length+1];
			System.arraycopy(infoSymbols, 0, infoSymbolsEx, 1, infoSymbols.length);
			infoSymbols = infoSymbolsEx;
			infoSymbolsEx = null;
		}
		
		
	}
	
	*/

	/* decode a coded array
    int [] data – an array of integer (with size r), each element is either zero or one (that is, they mimic a bit array)
    int n – an integer
    returns int [] – the decoded bit array, of size n
	 */
	//public static int [] decode (int [] data, int n);






	public static void main(String[] args) {

	GenericGF F = GenericGF.GF8;
		ReedSolomonEncoder rse = new ReedSolomonEncoder(F);
		int[] message = new int[]{1,0,0,0,1,0,0,0,1};
		System.out.println("Original message");
		System.out.println(Arrays.toString(message));
		System.out.println(Arrays.toString(rse.encodeFromBinary(message)));
		
		System.out.println("Message + noise"); //This can be automaized using method above.
		int[] mpn =new int[]{1,0,0, 0,1,0 , 0,0,1,  1,0,1,  1,1,1,  0,0,1, 0,0,0};// m + noise
		//System.out.println(Arrays.toString(mpn));
	  	
		ReedSolomonDecoder rsd = new ReedSolomonDecoder(F);
		try {
			System.out.println(Arrays.toString(rsd.decodeFromBinary(mpn, 5)));
		} catch (ReedSolomonException e) {
			e.printStackTrace();
		}


	}

	}

}
