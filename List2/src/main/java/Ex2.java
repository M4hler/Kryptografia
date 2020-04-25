import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.KeyStore;

public class Ex2
{
    public static void main(String[] args)
    {
        Main main = new Main();
        KeyStore keyStore = main.keyStoreSetup();
        Key key = main.keySetup(keyStore);

        try
        {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            byte[] firstIV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6};
            byte[] secondIV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7};

            byte[] b = "false".getBytes();

            byte[] plaintext = xor(firstIV, secondIV);
            plaintext = xor(plaintext, b);

            IvParameterSpec iv = new IvParameterSpec(firstIV);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            byte[] cipherEve = cipher.doFinal(plaintext);

            for(int i = 0; i < cipherEve.length; i++)
            {
                System.out.print(cipherEve[i] + " ");
            }

            IvParameterSpec iv2 = new IvParameterSpec(secondIV);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv2);
            byte[] plaintextAlice = xor(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, "false".getBytes());
            byte[] cipherAlice = cipher.doFinal(plaintextAlice);

            System.out.println();
            for(int i = 0; i < cipherAlice.length; i++)
            {
                System.out.print(cipherAlice[i] + " ");
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private static byte[] xor(byte[] b1, byte[] b2)
    {
        byte[] newB2 = b2;
        if(b1.length != b2.length)
        {
            if(b1.length < b2.length)
            {
                byte[] b3 = b2;
                b2 = b1;
                b1 = b3;
            }

            newB2 = new byte[b1.length];

            for(int i = 0; i < b1.length - b2.length; i++)
            {
                newB2[i] = 0;
            }

            for(int i = b1.length - b2.length; i < b1.length; i++)
            {
                newB2[i] = b2[i - (b1.length - b2.length)];
            }
        }

        byte[] result = new byte[b1.length];
        for(int i = 0; i < b1.length; i++)
        {
            result[i] = (byte) (b1[i] ^ newB2[i]);
        }
        return result;
    }
}
