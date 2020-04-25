import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class Main
{
    private BufferedReader reader;
    private String keystoreName;
    private String password;

    public Main()
    {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public static void main(String[] args)
    {
        Main main = new Main();
        main.ExA1();
    }

    private void ExA1()
    {
        String encryptionMode = encryptionMode();
        KeyStore keyStore = keyStoreSetup();
        Key key = keySetup(keyStore);
        String programMode = programMode();

        if(programMode.equals("1"))
        {
            encryptionOracle(encryptionMode, key);
        }
        else if(programMode.equals("2"))
        {
            challenge(encryptionMode, key);
        }
        else
        {
            decryption(encryptionMode, key);
        }
    }

    private String encryptionMode()
    {
        System.out.println("Modes of encryption: OFB, CTR, CBC");
        String mode = "";

        try
        {
            while(!mode.equals("OFB") && !mode.equals("CTR") && !mode.equals("CBC"))
            {
                System.out.print("Choose mode: ");
                mode = reader.readLine();
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }

        return mode;
    }

    public KeyStore keyStoreSetup()
    {
        System.out.println("Keystore options: \n[1] - create \n[2] - load");
        String option = "";

        try
        {
            while(!option.equals("1") && !option.equals("2"))
            {
                System.out.print("Option: ");
                option = reader.readLine();
            }

            KeyStore ks;
            if(option.equals("1")) //create
            {
                ks = KeyStore.getInstance("JCEKS");
                System.out.print("Keystore name: ");
                keystoreName = reader.readLine();

                System.out.print("Keystore password: ");
                password = reader.readLine();
                ks.load(null, password.toCharArray());

                ks.store(new FileOutputStream(keystoreName), password.toCharArray());
            }
            else //load
            {
                ks = KeyStore.getInstance("JCEKS");
                System.out.print("KeyStore file name: ");
                keystoreName = reader.readLine();
                System.out.print("Password: ");
                password = reader.readLine();
                ks.load(new FileInputStream(keystoreName), password.toCharArray());
            }
            return ks;
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            return null;
        }
    }

    public Key keySetup(KeyStore keyStore)
    {
        try
        {
            System.out.println("Key management options: \n[1] - create key and use it \n[2] - get key");
            String option = "";
            while(!option.equals("1") && !option.equals("2"))
            {
                System.out.print("Option: ");
                option = reader.readLine();
            }

            if(option.equals("1"))
            {
                System.out.print("Key alias: ");
                String alias = reader.readLine();

                System.out.print("Key: ");
                String key = reader.readLine();

                System.out.print("Key password: ");
                String keyPassword = reader.readLine();

                SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
                KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(secretKey);
                KeyStore.ProtectionParameter passwordPP = new KeyStore.PasswordProtection(keyPassword.toCharArray());

                keyStore.setEntry(alias, secret, passwordPP);

                keyStore.store(new FileOutputStream(keystoreName), password.toCharArray());
                return keyStore.getKey(alias, keyPassword.toCharArray());
            }
            else
            {
                System.out.print("Key alias: ");
                String alias = reader.readLine();

                System.out.print("Key password: ");
                String keyPassword = reader.readLine();

                return keyStore.getKey(alias, keyPassword.toCharArray());
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            return null;
        }
    }

    private String programMode()
    {
        System.out.println("Program modes: \n[1] - encryption oracle \n[2] - challenge \n[3] - decryption");
        String mode = "";

        try
        {
            while(!mode.equals("1") && !mode.equals("2") && !mode.equals("3"))
            {
                System.out.print("Choose mode: ");
                mode = reader.readLine();
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }

        return mode;
    }

    private void encryptionOracle(String encryptionMode, Key key)
    {
        try
        {
            boolean flag = false;
            int number = 0;
            while(!flag)
            {
                System.out.print("Number of messages to encrypt: ");
                try
                {
                    number = Integer.parseInt(reader.readLine());
                    flag = true;
                }
                catch(Exception e)
                {
                    System.out.println(e.toString());
                }
            }

            String[] plainTexts = new String[number];
            for(int i = 0; i < number; i++)
            {
                System.out.print((i + 1) + ": ");
                plainTexts[i] = reader.readLine();
            }

            Cipher cipher = Cipher.getInstance("AES/" + encryptionMode + "/PKCS5PADDING");

            System.out.print("IV: ");
            String ivInput = reader.readLine();

            IvParameterSpec iv = new IvParameterSpec(ivInput.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            String[] cipherTexts = new String[number];
            for(int i = 0; i < number; i++)
            {
                cipherTexts[i] = Base64.getEncoder().encodeToString(cipher.doFinal(plainTexts[i].getBytes()));
            }

            FileWriter myWriter = new FileWriter("encryption.txt");
            for(int i = 0; i < number; i++)
            {
                myWriter.write(cipherTexts[i] + "\n");
            }
            myWriter.close();
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private void decryption(String encryptionMode, Key key)
    {
        try
        {
            File file = new File("encryption.txt");
            Scanner sc = new Scanner(file);

            System.out.print("IV: ");
            String ivInput = reader.readLine();
            IvParameterSpec iv = new IvParameterSpec(ivInput.getBytes());

            Cipher cipher = Cipher.getInstance("AES/" + encryptionMode + "/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            FileWriter myWriter = new FileWriter("decryption.txt");
            while (sc.hasNextLine())
            {
                String cipherText = sc.nextLine();
                String plainText = new String(cipher.doFinal(Base64.getDecoder().decode(cipherText.getBytes())));
                myWriter.write(plainText + "\n");
            }
            myWriter.close();
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private void challenge(String encryptionMode, Key key)
    {
        try
        {
            System.out.print("IV: ");
            String ivInput = reader.readLine();
            IvParameterSpec iv = new IvParameterSpec(ivInput.getBytes());

            System.out.print("m0: ");
            String m0 = reader.readLine();

            System.out.print("m1: ");
            String m1 = reader.readLine();

            Cipher cipher = Cipher.getInstance("AES/" + encryptionMode + "/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            Random random = new Random();
            boolean message = random.nextBoolean();

            if(message)
            {
                System.out.println(Base64.getEncoder().encodeToString(cipher.doFinal(m0.getBytes())));
            }
            else
            {
                System.out.println(Base64.getEncoder().encodeToString(cipher.doFinal(m1.getBytes())));
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }
}
