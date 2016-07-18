import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Secret
{
    private static SecureRandom random = null;
    
    public static void encipher (Identifible [] publicKeys, int m, int k, byte [] message) throws Exception
    {
        int n = publicKeys.length;
        byte [][] keys = new byte [n][];
        for (int i = 0; i < n; i++)
        {
            keys [i] = getRandomKey (k);
        }
        
        byte [] key = combineKeys (keys);
        byte [] cipherText = encipher (message, key);
    }
    
    public static byte [] encipher (byte [] plainText, byte [] key) throws Exception
    {
        byte [] IV = "0123456789ABCDEF".getBytes ("UTF-8");
        Cipher cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
        cipher.init (Cipher.ENCRYPT_MODE, new SecretKeySpec (key, "AES"), new IvParameterSpec (IV));
        return cipher.doFinal (plainText);
    }
    
    public static byte [] decipher (byte [] cipherText, byte [] key) throws Exception
    {
        byte [] IV = "0123456789ABCDEF".getBytes ("UTF-8");
        Cipher cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
        cipher.init (Cipher.DECRYPT_MODE, new SecretKeySpec (key, "AES"), new IvParameterSpec (IV));
        return cipher.doFinal (cipherText);
    }

    private static byte [] combineKeys (byte [][] keys)
    {
        // we assume at least 1 key in parameter
        int k8 = keys [0].length;
        byte [] result = new byte [k8];
        
        for (int i = 0; i < k8; i++)
        {
            result [i] = keys [0][i];
        }
        
        int n = keys.length;
        
        for (int i = 0; i < k8; i++)
        {
            for (int j = 1; j < n; j++)
            {
                result [i] = (byte) (result [i] ^ keys [j][i]);
            }
        }
        
        return result;
    }
    
    private static byte [] getRandomKey (int numberOfBits)
    {
        if (random == null)
        {
            random = new SecureRandom ();
        }
        
        int n = (int) Math.ceil (numberOfBits / 8);
        byte [] bytes = new byte [n];
        random.nextBytes (bytes);
        return bytes;
    }
    
    private static byte [] encode (byte [] data, int m)
    {
        // to be provided by Sarami
        return null;
    }
    
    private static byte [] decode (byte [] data, int n)
    {
        // to be provided by Sarami
        return null;
    }
    
    private static int getCodeLength (int n, int m)
    {
        // to be provided by Sarami
        return 0;
    }
    
    private static byte getBit (byte [] data, int i)
    {
        int byteId = i / 8;
        int bitId = i % 8;
        return (byte) (((data [byteId] & (1 << bitId)) == 0) ? 0 : 1);
    }
    
    private static void setBit (byte [] data, int i, byte bit)
    {
        int byteId = i / 8;
        int bitId = i % 8;
        if (bit == 0)
        {
            // need to clear bit i
            data [byteId] &= ~ (1 << bitId);
        }
        else // (bit == 1)
        {
            // need to set bit i
            data [byteId] |= (1 << bitId);
        }
    }
    
    private byte [][] encodeKeys (byte [][] keys, int m)
    {
        int n = keys.length;
        int k = keys [0].length * 8;
        int r = getCodeLength (n, m);
        
        byte [][] results = new byte [r][k/8];
        
        for (int i = 0; i < k; i++)
        {
            byte [] data = new byte [n];
            for (int nn = 0; nn < n; nn++)
            {
                data [nn] = getBit (keys [nn], i);
            }
            data = encode (data, m);
            for (int rr = 0; rr < r; rr++)
            {
                setBit (results [rr], i, data [rr]);
            }
        }
        
        return results;
    }

    public static void main (String [] args) throws Exception
    {
        byte [] plainText = "HelloWorldCipher".getBytes ("UTF-8");
        byte [] key = "0123456789ABCDEF".getBytes ("UTF-8");
        byte [] cipherText = encipher (plainText, key);
        byte [] plainTextAgain = decipher (cipherText, key);
        for (int i = 0; i < plainText.length; i++)
        {
            if (plainText [i] != plainTextAgain [i])
            {
                System.out.println (i + ": " + plainText [i] + " -> " + plainTextAgain [i]);
            }
        }
    }
}

interface Identifible
{
    public String getID ();
    public byte [] getContent ();
}