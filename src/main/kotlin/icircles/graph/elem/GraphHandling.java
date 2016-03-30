package icircles.graph.elem;


import java.util.*;

import icircles.graph.EulerDualEdge;
import icircles.graph.EulerDualNode;
import icircles.graph.GraphCycle;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;


public class GraphHandling {

    private UndirectedGraph<EulerDualNode, EulerDualEdge> graph;
    private List<EulerDualNode> vertexList;
    private boolean adjMatrix[][];

    public GraphHandling() {
        this.graph = new SimpleGraph<>(EulerDualEdge.class);
        this.vertexList = new ArrayList<>();
    }

    public void addVertex(EulerDualNode vertex) {
        this.graph.addVertex(vertex);
        this.vertexList.add(vertex);
    }

    public void addEdge(EulerDualNode vertex1, EulerDualNode vertex2, EulerDualEdge edge) {
        this.graph.addEdge(vertex1, vertex2, edge);
    }

    public UndirectedGraph<EulerDualNode, EulerDualEdge> getGraph() {
        return graph;
    }

    public List<GraphCycle> computeCycles() {
        List<GraphCycle> graphCycles = new ArrayList<>();

        List<List<EulerDualNode> > cycles = getAllCycles();

        for (List<EulerDualNode> cycle : cycles) {
            List<EulerDualEdge> edges = new ArrayList<>();

            for (int i = 0; i < cycle.size(); i++) {
                int j = i + 1 < cycle.size() ? i + 1 : 0;

                EulerDualNode v1 = cycle.get(i);
                EulerDualNode v2 = cycle.get(j);

                edges.add(graph.getEdge(v1, v2));
            }

            graphCycles.add(new GraphCycle(cycle, edges));
        }

        return graphCycles;
    }

    public List<List<EulerDualNode>> getAllCycles() {
        this.buildAdjancyMatrix();

        @SuppressWarnings("unchecked")
        EulerDualNode[] vertexArray = this.vertexList.toArray(new EulerDualNode[0]);
        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(this.adjMatrix, vertexArray);

        @SuppressWarnings("unchecked")
        List<List<EulerDualNode>> cycles0 = ecs.getElementaryCycles();

        // remove cycles of size 2
        Iterator<List<EulerDualNode>> listIt = cycles0.iterator();
        while (listIt.hasNext()) {
            List<EulerDualNode> cycle = listIt.next();

            if (cycle.size() == 2) {
                listIt.remove();
            }
        }

        // remove repeated cycles (two cycles are repeated if they have the same vertex (no matter the order)
        List<List<EulerDualNode>> cycles1 = removeRepeatedLists(cycles0);

//        for (List<EulerDualNode> cycle : cycles1) {
//            System.out.println(cycle);
//        }


        return cycles1;
    }

    private void buildAdjancyMatrix() {
        Set<EulerDualEdge> edges = this.graph.edgeSet();
        Integer nVertex = this.vertexList.size();
        this.adjMatrix = new boolean[nVertex][nVertex];

        for (EulerDualEdge edge : edges) {
            EulerDualNode v1 = this.graph.getEdgeSource(edge);
            EulerDualNode v2 = this.graph.getEdgeTarget(edge);

            int i = this.vertexList.indexOf(v1);
            int j = this.vertexList.indexOf(v2);

            this.adjMatrix[i][j] = true;
            this.adjMatrix[j][i] = true;
        }
    }

    /* Here repeated lists are those with the same elements, no matter the order,
     * and it is assumed that there are no repeated elements on any of the lists*/
    private List<List<EulerDualNode>> removeRepeatedLists(List<List<EulerDualNode>> listOfLists) {
        List<List<EulerDualNode>> inputListOfLists = new ArrayList<List<EulerDualNode>>(listOfLists);
        List<List<EulerDualNode>> outputListOfLists = new ArrayList<List<EulerDualNode>>();

        while (!inputListOfLists.isEmpty()) {
            // get the first element
            List<EulerDualNode> thisList = inputListOfLists.get(0);
            // remove it
            inputListOfLists.remove(0);
            outputListOfLists.add(thisList);
            // look for duplicates
            Integer nEl = thisList.size();
            Iterator<List<EulerDualNode>> listIt = inputListOfLists.iterator();
            while (listIt.hasNext()) {
                List<EulerDualNode> remainingList = listIt.next();

                if (remainingList.size() == nEl) {
                    if (remainingList.containsAll(thisList)) {
                        listIt.remove();
                    }
                }
            }

        }

        return outputListOfLists;
    }

}