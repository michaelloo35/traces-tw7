package pl.edu.agh.tw.application;

import org.jgraph.JGraph;
import org.jgrapht.Graph;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.generate.ComplementGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

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

        String trace = "baadbc";

//        3.
//        visualize(generateMinDependencyGraph(trace, g));

//        2.
//        for (String s : generateFoataFNF(trace, g)) {
//            System.out.println(s);
//        }

//        4.
        for (String s : generateFoataFromGraph(generateMinDependencyGraph(trace, g), trace)) {
            System.out.println(s);
        }
    }

    // punkt 1
    private static Graph<String, DefaultEdge> createDgraph(Graph<String, DefaultEdge> iGraph) {
        Graph<String, DefaultEdge> d = new SimpleGraph<>(DefaultEdge.class);
        new ComplementGraphGenerator(iGraph).generateGraph(d);
        return d;
    }

    private static void visualize(Graph graph) throws InterruptedException {
        JFrame frame = new JFrame();
        frame.setSize(400, 400);

        JGraph jgraph = new JGraph(new JGraphModelAdapter<>(graph));
        frame.getContentPane().add(jgraph);
        frame.setVisible(true);
        while (true) {
            Thread.sleep(2000);
        }
    }

    // marker = 0
    // punkt 2
    private static List<String> generateFoataFNF(String trace, Graph<String, DefaultEdge> iGraph) {

        HashMap<String, Stack<String>> letterStacks = new HashMap<>();
        Set<String> alphabet = new HashSet<>();

        for (char c : trace.toCharArray()) {
            // create stack for each letter
            letterStacks.put(String.valueOf(c), new Stack<>());
            // create set alphabet
            alphabet.add(String.valueOf(c));
        }


        // since algorithm starts from the right side we need to reverse word
        String reversedWord = new StringBuilder(trace).reverse().toString();

        // we need to place each letter c on its own stack + markers on letters that do not commute with c
        fillStacks(iGraph, letterStacks, alphabet, reversedWord);

        List<String> result = new ArrayList<>();

        // now we need to empty the stacks following algorithm bellow
        // UNTIL all stacks are empty
        // 1. take all letters from the top layer
        // 2. sort them and that is our Foata FNF block
        // 3. remove MARKs on stacks corresponding to letters that do not commute with each letter taken from top layer
        Collection<Stack<String>> stacks = letterStacks.values();

        // until all stacks are empty
        while (!stacks.stream().allMatch(Vector::isEmpty)) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");

            List<String> unsortedResult = new ArrayList<>();

            //1.
            takeLettersFromTopLayer(stacks, unsortedResult);

            //2.
            Collections.sort(unsortedResult);

            //3. then we need to take down marks just from the letters that are not in relation with our letter
            popMarks(iGraph, letterStacks, alphabet, unsortedResult);


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

    private static void popMarks(Graph<String, DefaultEdge> g, HashMap<String, Stack<String>> letterStacks, Set<String> alphabet, List<String> unsortedResult) {
        unsortedResult.forEach(letter ->
                alphabet.stream()
                        .filter(l -> !l.equals(letter))
                        .filter(l -> !g.containsEdge(l, letter))
                        .forEach(l -> letterStacks.get(l).pop()));
    }

    private static void takeLettersFromTopLayer(Collection<Stack<String>> stacks, List<String> unsortedResult) {
        for (Stack<String> stack : stacks) {

            // check if stack is not empty
            if (!stack.isEmpty()) {
                String letter = stack.peek();

                // if there's a letter on the top pop it
                if (!letter.equals(MARK)) {
                    unsortedResult.add(stack.pop());
                }
            }
        }
    }

    private static void fillStacks(Graph<String, DefaultEdge> g, HashMap<String, Stack<String>> letterStacks, Set<String> alphabet, String reversedWord) {
        for (char c : reversedWord.toCharArray()) {

            // iterate through all letters in set A
            String currentLetter = String.valueOf(c);
            for (String letter : alphabet) {

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
    }


    // 3.
    private static Graph<Integer, DefaultEdge> generateMinDependencyGraph(String trace, Graph<String, DefaultEdge> iGraph) {
        SimpleDirectedGraph<Integer, DefaultEdge> dependencyGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
        DefaultEdge e = new DefaultEdge();
        // we need to add all its dependencies in trace in other words:
        // and add edges from letter c to any letter in trace t that occurs after c and does not commute with c in relation I
        // also we need labels for letters let it be position in trace t

        Set<String> alphabet = new HashSet<>();
        HashMap<Integer, String> labels = new HashMap<>();

        char[] traceAsCharArray = trace.toCharArray();
        int size = traceAsCharArray.length;

        // first compute alphabet of t add vertexes and labels
        for (int i = 0; i < size; i++) {
            alphabet.add(String.valueOf(traceAsCharArray[i]));
            labels.put(i, String.valueOf(traceAsCharArray[i]));
            dependencyGraph.addVertex(i);
        }


        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // letters of our interest occurs after letter at position i
                if (j > i) {
                    // if relation I does not contain edge between two letters create edge
                    if (!iGraph.containsEdge(labels.get(i), labels.get(j)))
                        dependencyGraph.addEdge(i, j);
                }
            }
        }

        // reduce redundant edges
        TransitiveReduction.INSTANCE.reduce(dependencyGraph);
        return dependencyGraph;
    }

    //4. we use labels here as well CORNER CASE ON LAST LETTER
    private static List<String> generateFoataFromGraph(Graph<Integer, DefaultEdge> minDependencyGraph, String trace) {

        // we will get Directed Graph as labels so we need labels map
        HashMap<Integer, String> labels = new HashMap<>();

        char[] traceAsCharArray = trace.toCharArray();
        int size = traceAsCharArray.length;

        // first compute alphabet of t add vertexes and labels
        for (int i = 0; i < size; i++) {
            labels.put(i, String.valueOf(traceAsCharArray[i]));
        }


        TopologicalOrderIterator<Integer, DefaultEdge> topologicalIterator = new TopologicalOrderIterator(minDependencyGraph);
        StringBuilder sb = new StringBuilder();


        Integer vertexBeforeCurrent = topologicalIterator.next();

        // create resultList
        List<Integer> resultVertexes = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        while (topologicalIterator.hasNext()) {

            Integer currentVertex = topologicalIterator.next();
            resultVertexes.add(vertexBeforeCurrent);

            // if there are edges to our next vertex in order we need to finish "block" remove all gathered vertexes
            // sort them and move on onto next vertex then.
            if (minDependencyGraph.incomingEdgesOf(currentVertex).size() > 0) {
                resultVertexes.forEach(minDependencyGraph::removeVertex);

                List<String> unsortedResultList = new ArrayList<>();
                resultVertexes.forEach(v -> unsortedResultList.add(labels.get(v)));
                unsortedResultList.sort(String.CASE_INSENSITIVE_ORDER);

                // just to make nice output
                sb.append("(");
                // already sorted here
                unsortedResultList.forEach(sb::append);
                sb.append(")");
                resultList.add(sb.toString());
                // just to make nice output
                sb = new StringBuilder();
                resultVertexes.clear();
            }
            vertexBeforeCurrent = currentVertex;
        }

        // append last vertex
        sb.append("(");
        sb.append(labels.get(vertexBeforeCurrent));
        sb.append(")");
        resultList.add(sb.toString());
        return resultList;

    }
}
