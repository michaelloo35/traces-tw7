package pl.edu.agh.tw.application;

import org.jgraph.JGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.generate.ComplementGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.util.*;

public class Main {

    public static final String MARK = "0";

    public static void main(String[] args) throws InterruptedException {

        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addEdge("a", "d");
        g.addEdge("b", "c");

        String word = "baadbc";
        for (String s : generateFoataFNF(word, g)) {
            System.out.println(s);
        }
    }

    // punkt 1
    private static Graph<String, DefaultEdge> createDgraph(Graph<String, DefaultEdge> g) {
        Graph<String, DefaultEdge> d = new SimpleGraph<>(DefaultEdge.class);
        new ComplementGraphGenerator(g).generateGraph(d);
        return d;
    }

    private static void visualize(Graph<String, DefaultEdge> g) throws InterruptedException {
        JFrame frame = new JFrame();
        frame.setSize(400, 400);

        JGraph jgraph = new JGraph(new JGraphModelAdapter<String, DefaultEdge>(g));
        frame.getContentPane().add(jgraph);
        frame.setVisible(true);
        while (true) {
            Thread.sleep(2000);
        }
    }

    // marker = 0
    private static List<String> generateFoataFNF(String word, Graph<String, DefaultEdge> g) {

        HashMap<String, Stack<String>> letterStacks = new HashMap<>();
        Set<String> letters = new HashSet<>();

        for (char c : word.toCharArray()) {
            // create stacks
            letterStacks.put(String.valueOf(c), new Stack<>());
            // create set A containing letters in word
            letters.add(String.valueOf(c));
        }


        String reversedWord = new StringBuilder(word).reverse().toString();

        // we need to place each letter c on its own stack + markers on letters that do not commute with c
        // iterate through word
        for (char c : reversedWord.toCharArray()) {

            // iterate through all letters in set A
            String currentLetter = String.valueOf(c);
            for (String letter : letters) {

                // if letter equals currentLetter we push it onto its stack
                if (letter.equals(currentLetter)) {
                    letterStacks.get(letter).push(currentLetter);
                }
                // if there's relation between currentLetter and letter from set do nothing
                else if (g.containsEdge(letter, currentLetter)) {
                }
                // otherwise push mark onto letter stack
                else
                    letterStacks.get(letter).push(MARK);
            }
        }

        List<String> result = new ArrayList<>();
        // after that we have our stacks prepare now we take them layer by layer until all are empty

        Collection<Stack<String>> stacks = letterStacks.values();

        // while not all of stacks are empty
        while (!stacks.stream().allMatch(Vector::isEmpty)) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");

            List<String> unsortedResult = new ArrayList<>();

            for (Stack<String> stack : stacks) {
                // check if stack ain't empty
                if (!stack.isEmpty()) {
                    String letter = stack.peek();

                    // if there's a letter on the top get it
                    if (!letter.equals(MARK)) {
                        unsortedResult.add(stack.pop());
                    }
                }
            }
            // then we need to take down marks just from the letters that are not in relation with our letter
            unsortedResult.forEach(letter ->
                    letters.stream()
                            .filter(l -> !l.equals(letter))
                            .filter(l -> !g.containsEdge(l, letter))
                            .forEach(l -> letterStacks.get(l).pop()));

            Collections.sort(unsortedResult);

            // sorted here
            for (String s : unsortedResult) {
                sb.append(s);
            }

            sb.append(")");
            if (sb.length() > 2)
                result.add(sb.toString());
        }
        return result;
    }

}
