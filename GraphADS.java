/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/
package ammo;


import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Serializable;

/**
 * The <code>GraphADS</code> encapsulates the abstract data-structure
 * representing the ontology graph.
 *
 */
public class GraphADS implements Serializable {

    /** Name of the ontology representing this graph.
     */
    String name;

    /**Ontology concept ids are continues from startIndex to startIndex + length.
     */
    long startIndex;

    /** Count of the ontology terms.
     */
    long length;

    /** This represents the adjacencyList for the ontology i.e. parent pointing to the child.
     */
    HashMap<Long,HashSet<Long>> adjacencyList;

    /** This represents all the roots in the ontology. Traversing is done by them (also does the 
     * topological sort simultaneously).
     */
    HashSet<Long> roots;


    /**
     * Constructor initializing the graph data-structure from NCBO database
     * for a given ontology identifier.
     */
    public GraphADS(String ontologyID) {
	Resource resource = new Resource("ncbodev-obrdbmaster1.sunet", "resource_index_test", "ammo", "ammo");
	name = resource.getOntologyStatistics("obs_ontology","id","name",ontologyID);
	startIndex = Long.parseLong(resource.getOntologyStatistics("obs_concept", "ontology_id", "id", ontologyID));
	length = Long.parseLong(resource.getOntologyStatistics("obs_concept", "ontology_id", "count(id)", ontologyID));
	adjacencyList = new HashMap<Long,HashSet<Long>> ();
	roots = resource.getOntologyGraphSpecObs(startIndex, length, adjacencyList);
	resource.close();
    }

    /**
     * One-step breadth first search for getting nodes at the next level.
     * @param nodes Nodes at the current level.
     * @param visited Nodes already visited.
     * @return Nodes at the next level.
     */
    private HashSet<Long> getNextLevel(HashSet<Long> nodes, HashSet<Long> visited) {
	HashSet<Long> nextLevel = new HashSet<Long> ();
	Iterator niterator = nodes.iterator();
	while(niterator.hasNext()) {
	    Long node = (Long) niterator.next();
	    HashSet<Long> cnodes = adjacencyList.get(node);
	    if (cnodes == null)
		continue;
	    else {
		Iterator citerator = cnodes.iterator();
		while (citerator.hasNext()) {
		    Long cnode = (Long) citerator.next();
		    if (!visited.contains(cnode))
			nextLevel.add(cnode);
		}
	    }
	}
	return nextLevel;

    }
    
    /**
     * Helper function to store already visited nodes.
     * @param nodes List of current nodes.
     * @param visited List of already visited nodes.
     */
    private void insertIntoVisited(HashSet<Long> nodes, HashSet<Long> visited) {
	Iterator iterator = nodes.iterator();
	while(iterator.hasNext())
	    visited.add((Long) iterator.next());
    }
    
    /**
     * Function to compute a reverse topological sort of the ontology 
     * graph.
     * @return Arraylist of nodes in reverse topological sort.
     */
    public ArrayList<HashSet<Long>> ReverseLevelBasedTopologicalSort() {
	ArrayList<HashSet<Long>> topologicalSort = new ArrayList<HashSet<Long>> ();
	HashSet<Long> visited = new HashSet<Long> ();
	topologicalSort.add(roots);
	insertIntoVisited(roots,visited);
	HashSet<Long> storeNext;
	HashSet<Long> temp = roots;
	while ((storeNext = getNextLevel(temp,visited)).size() != 0) {
	    topologicalSort.add(storeNext);
	    insertIntoVisited(storeNext,visited);
	    temp = storeNext;
	}
	return topologicalSort;
    }

  

	public static void main(String [] args) {
	    long time = System.currentTimeMillis();
	    GraphADS gads = new GraphADS("247");
	    ArrayList<HashSet<Long>> ts = gads.ReverseLevelBasedTopologicalSort();
	    System.out.println(System.currentTimeMillis() - time);
	    for(int i=0; i < ts.size(); i++)
	    	System.out.println(ts.get(i).size());
	}
    }