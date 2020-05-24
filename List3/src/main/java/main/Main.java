package main;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class Main
{
    //(w, q, r) - private key; b - public key
    private BigInteger[] w, b;
    private BigInteger q, r;
    private Random rand = new Random();
    private static final int MAX_CHARS = 150;
    private static final int BINARY_LENGTH = MAX_CHARS * 8;

    public Main()
    {
        genKeys();
    }

    private void genKeys()
    {
        w = new BigInteger[BINARY_LENGTH]; //super-increasing sequence
        w[0] = new BigInteger(50, rand).add(BigInteger.ONE); //initialize

        BigInteger sum = new BigInteger(w[0].toByteArray());
        for(int i = 1; i < w.length; i++) //create super-increasing sequence
        {
            w[i] = sum.add(new BigInteger(50, rand).add(BigInteger.ONE));
            sum = sum.add(w[i]);
        }

        q = sum.add(new BigInteger(50, rand).add(BigInteger.ONE));
        r = q.subtract(BigInteger.ONE); //q - 1 is co-prime to q

        // generate the public key sequence
        b = new BigInteger[BINARY_LENGTH];
        for(int i = 0; i < b.length; i++)
        {
            b[i] = w[i].multiply(r).mod(q);
        }
    }

    public String encryption(String message)
    {
        String msgBinary = new BigInteger(message.getBytes(StandardCharsets.UTF_8)).toString(2); //binary

        if(msgBinary.length() < BINARY_LENGTH) //padding with zero's
        {
            msgBinary = String.format("%0" + (BINARY_LENGTH - msgBinary.length()) + "d", 0) + msgBinary;
        }

        BigInteger result = BigInteger.ZERO;
        for(int i = 0; i < msgBinary.length(); i++)
        {
            result = result.add(b[i].multiply(new BigInteger(msgBinary.substring(i, i + 1))));
        }

        return result.toString();
    }

    public String decryption(String ciphertext)
    {
        BigInteger tmp = new BigInteger(ciphertext).mod(q).multiply(r.modInverse(q)).mod(q);
        byte[] decrypted_binary = new byte[w.length];  //array for decrypted message in binary

        for(int i = w.length - 1; i >= 0; i--)
        {
            if(w[i].compareTo(tmp) <= 0)
            {
                tmp = tmp.subtract(w[i]);
                decrypted_binary[i] = 1;
            }
            else
            {
                decrypted_binary[i] = 0;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < decrypted_binary.length; i++) {
            sb.append(decrypted_binary[i]);
        }

        return new String(new BigInteger(sb.toString(), 2).toByteArray());
    }

    public static void main(String[] args)
    {
        Main crypto = new Main();

        Scanner input = new Scanner(System.in);

        System.out.print("Enter plaintext: ");
        String message = input.nextLine();

        if(message.length() > MAX_CHARS || message.length() <= 0)
        {
            System.out.println("Message should be non-empty and shorter than " + MAX_CHARS + " characters");
            System.exit(0);
        }

        System.out.println("Number of plaintext bytes = " + message.getBytes().length);

        String encrypted = crypto.encryption(message);
        System.out.println(message + " is encrypted as: " + encrypted);

        System.out.println("Result of decryption: " + crypto.decryption(encrypted));
    }
}