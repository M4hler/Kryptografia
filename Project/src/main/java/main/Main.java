package main;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.Random;

public class Main
{
    private Random generator = new Random();

    public static void main(String[] args)
    {
        Main main = new Main();
        main.protocol();
    }

    private void protocol()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        //graph looks like this: https://pl.wikipedia.org/wiki/Graf_Petersena
        //vertices are numbered in following order
        //      0
        //  4   5   1
        //    9   6
        //     8 7
        //  3        2
        //vertex 0,1,2,3,4 are in outer ring, vertex 5,6,7,8,9 form inner pentagram
        for(int i = 0; i < 10; i++)
        {
            graph.addVertex(i);
        }

        for(int i = 0; i < 5; i++) //outer ring
        {
            graph.addEdge(i, (i + 1) % 5); //outer ring
            graph.addEdge(i + 5, (i + 7) % 5 + 5); //pentagram
            graph.addEdge(i, i + 5); //linking outer ring with the pentagram
        }

        ArrayList<Pair<Integer, Integer>> threeColoring = new ArrayList<>(); //witness of 3-coloring, serves as commitment
        //coloring is as follows
        //      1
        //  2   0   2
        //    0   1
        //     2 1
        //  1        0
        threeColoring.add(new Pair<>(0, 1));
        threeColoring.add(new Pair<>(1, 2));
        threeColoring.add(new Pair<>(2, 0));
        threeColoring.add(new Pair<>(3, 1));
        threeColoring.add(new Pair<>(4, 2));
        threeColoring.add(new Pair<>(5, 0));
        threeColoring.add(new Pair<>(6, 1));
        threeColoring.add(new Pair<>(7, 1));
        threeColoring.add(new Pair<>(8, 2));
        threeColoring.add(new Pair<>(9, 0));
        //threeColoring.add(new Pair<>(9, 1)); //one can uncomment this line and comment line above to check how fast a cheating prover will be discovered

        double confidence = 0;
        double trustFactor = 0.95;
        int k = 0;

        while(confidence < trustFactor)
        {
            ArrayList<Integer> permutation = generatePermutation();

            for(Pair<Integer, Integer> p : threeColoring)
            {
                p.setSecond(permutation.get(p.getSecond())); //change vertex color according to generated permutation
            }

            int e = Math.abs(generator.nextInt() % graph.edgeSet().size()); //randomly select edge
            DefaultEdge edge = (DefaultEdge) graph.edgeSet().toArray()[e]; //pick edge

            int sourceVertex = graph.getEdgeSource(edge); //get vertex index
            int targetVertex = graph.getEdgeTarget(edge);

            int color1 = threeColoring.get(sourceVertex).getSecond(); //get vertex color
            int color2 = threeColoring.get(targetVertex).getSecond();

            if(color1 != color2)
            {
                confidence = 1 - Math.pow(1.0 - (1.0 / graph.edgeSet().size()), k);
            }
            else
            {
                System.out.println("Fake discovered after " + k + " challenges");
                return;
            }

            k++;
        }

        System.out.println("After " + k + " times confidence level reached " + confidence + " which is above trust factor " + trustFactor);
    }

    private ArrayList<Integer> generatePermutation()
    {
        ArrayList<Integer> permutation = new ArrayList<>();
        while(permutation.size() < 3)
        {
            int random = Math.abs(generator.nextInt()) % 3;
            if(!permutation.contains(random))
            {
                permutation.add(random);
            }
        }

        return permutation;
    }
}