import java.math.BigInteger;
import java.util.*;

public class Main
{
    private long seed;
    private long increment;
    private long multiplier;
    private long modulus;

    private long[] glibctable;
    private long index;

    public Main(long seed, long increment, long multiplier, long modulus)
    {
        this.seed = seed;
        this.increment = increment;
        this.multiplier = multiplier;
        this.modulus = modulus;
        generateGlibcTable(1L);
    }

    public void generateGlibcTable(long seed)
    {
        glibctable = new long[344];
        glibctable[0] = seed;

        for(int i = 1; i < 31; i++)
        {
            glibctable[i] = Math.floorMod(16807 * glibctable[i - 1], 2147483647);
        }

        for(int i = 31; i < 34; i++)
        {
            glibctable[i] = glibctable[i - 31];
        }

        for(int i = 34; i < 344; i++)
        {
            glibctable[i] = glibctable[i - 31] + glibctable[i - 3];
        }

        for(int i = 0; i < 10; i++)
        {
            System.out.println("glibc: " + glibctable[i]);
        }

        index = 0;
    }

    public long next()
    {
        long x = glibctable[(int)index % 344];
        index = (index + 1) % 344;
        return (x >> 1);
    }

    public long LCG()
    {
        seed =  Math.floorMod(seed * multiplier + increment, modulus);
        return seed;
    }

    public long gcd(long a, long b)
    {
        if(b == 0)
        {
            return a;
        }
        else
        {
            return gcd(b, a % b);
        }
    }

    public long incrementNotKnown(List<Long> states, long modulus, long multiplier)
    {
        long increment = states.get(1) - states.get(0) * multiplier % modulus;
        return increment;
    }

    public long incrementAndMultiplierNotKnown(List<Long> states, long modulus)
    {
        BigInteger bi1 = new BigInteger(String.valueOf(states.get(1) - states.get(0)));

        if(states.get(1) - states.get(0) < 0)
        {
            bi1 = new BigInteger(String.valueOf(states.get(1) - states.get(0) + modulus));
        }

        BigInteger bi2 = new BigInteger(String.valueOf(modulus));

        long multiplier = (states.get(2) - states.get(1)) * bi1.modInverse(bi2).longValue() % modulus;

        if(multiplier < 0)
            multiplier += modulus;

        return multiplier;
    }

    public long nothingKnown(List<Long> states)
    {
        long[] tSeriesDifferences = new long[states.size() - 1];
        for(int i = 0; i < tSeriesDifferences.length; i++)
        {
            tSeriesDifferences[i] = states.get(i + 1) - states.get(i);
        }

        long[] t2t0_t1t1 = new long[tSeriesDifferences.length - 2];
        long[] modulusGuesses = new long[tSeriesDifferences.length - 3];
        for(int i = 0; i < t2t0_t1t1.length; i++)
        {
            t2t0_t1t1[i] = tSeriesDifferences[i + 2] * tSeriesDifferences[i] - tSeriesDifferences[i + 1] * tSeriesDifferences[i + 1];

        }

        Map<Long, Long> map = new HashMap<>();
        long max = 0;
        long probableModulus = 0;
        for(int i = 0; i < modulusGuesses.length; i++)
        {
            modulusGuesses[i] = Math.abs(gcd(t2t0_t1t1[i], t2t0_t1t1[i + 1]));

            if(!map.containsKey(modulusGuesses[i]))
            {
                map.put(modulusGuesses[i], 1L);
            }
            else
            {
                long newVal = map.get(modulusGuesses[i]) + 1;
                map.put(modulusGuesses[i], newVal);

                if(newVal > max)
                {
                    probableModulus = modulusGuesses[i];
                }
            }
            System.out.println("modulus: " + i + " " + modulusGuesses[i]);
        }


        return probableModulus;
    }

    public void ex2()
    {
        generateGlibcTable(1);
        long state1 = next();
        long state2 = next();

        List<Long> states = new ArrayList<>();
        states.add(state1);
        states.add(state2);

        for(int i = 0; i < states.size(); i++)
        {
            System.out.println(states.get(i));
        }

        long res1 = incrementNotKnown(states, 2147483647, 16807);
        System.out.println("Increment guess 1: " + res1);

        generateGlibcTable(1);
        states = new ArrayList<>();
        state1 = next();
        state2 = next();

        states.add(state1 * 2 + 1);
        states.add(state2 * 2 + 1);

        long state3 = next();
        states.add(state3 * 2 + 1);

        long res2 = incrementAndMultiplierNotKnown(states, 2147483647);
        System.out.println("Multiplier guess: "+ res2);
    }

    public static void main(String[] args)
    {
        long seed = 1;
        long increment = 12345;
        long multiplier = 1103515245;
        long modulus = 2147483648L;
        Main main = new Main(seed, increment, multiplier, modulus);

        long state1 = main.LCG();
        long state2 = main.LCG();

        List<Long> states = new ArrayList<>();
        states.add(state1);
        states.add(state2);

        long res1 = main.incrementNotKnown(states, modulus, multiplier);
        System.out.println("Increment: " + res1);

        long state3 = main.LCG();
        states.add(state3);

        long res2 = main.incrementAndMultiplierNotKnown(states, modulus);
        System.out.println("Multiplier: "+ res2);

        for(int i = 0; i < 10; i++)
        {
            states.add(main.LCG());
        }

        long res3 = main.nothingKnown(states);
        System.out.println("Res3: " + res3);

        System.out.println("Glibc random()");
        main.ex2();
    }
}
