/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/
package ammo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;


/**
   This class represents a class of mapping algorithms for context-sensitive maps.

 **/
public class MappingAlgorithm {

    /**
       Graph based representation of ontologies.
     **/
    GraphADS sourceOntology;

    GraphADS destinationOntology;

    GraphADS contextOntology;

    /**
       Handler to the B-tree index containing cached statistics.
     **/
    CachingStatisticsSearching searcher;

    /**
       Model Scoring mechanism AIC or BIC or Mutual Information.
     **/
    ModelScoring scoring;

    InformationContent infocontent;

    /**
       Mappings from numerical identifiers to concept names.
     **/
    IdMaps sourceIdMap;

    IdMaps destinationIdMap;

    IdMaps contextIdMap;

    Long numOfComps;

    /**
       Constructor for initializing the mapping algorithm.
     **/
    public MappingAlgorithm(String sID, String dID, String cID, String indexName, String sourceidMap,String destinationidMap,String contextidMap) {
	this.sourceOntology = new GraphADS(sID);
	this.destinationOntology = new GraphADS(dID);
	if (this.sourceOntology.length > this.destinationOntology.length) {
	    GraphADS temp = this.sourceOntology;
	    this.sourceOntology = this.destinationOntology;
	    this.destinationOntology = temp;
	    this.sourceIdMap = new IdMaps(destinationidMap);
	    this.destinationIdMap = new IdMaps(sourceidMap);
	} else {
	    	this.sourceIdMap = new IdMaps(sourceidMap);
		this.destinationIdMap = new IdMaps(destinationidMap);
	}
	this.contextIdMap = new IdMaps(contextidMap);
	this.contextOntology = new GraphADS(cID);
	this.searcher = new CachingStatisticsSearching(indexName);
	this.scoring = new ModelScoring(ScoreType.CI);
	this.infocontent = new InformationContent();


	}

     /**
       Constructor for initializing the mapping algorithm.
     **/   
    public MappingAlgorithm(String sObjectName, String dObjectName, String cObjectName, String indexName, String sourceidMap,String destinationidMap,String contextidMap, boolean serialized) {
	this.sourceOntology = (new SerializableGraphADS()).restoreSerializableObject(sObjectName);
	this.destinationOntology =  (new SerializableGraphADS()).restoreSerializableObject(dObjectName);
	if (this.sourceOntology.length > this.destinationOntology.length) {
	    GraphADS temp = this.sourceOntology;
	    this.sourceOntology = this.destinationOntology;
	    this.destinationOntology = temp;
	    this.sourceIdMap = new IdMaps(destinationidMap);
	    this.destinationIdMap = new IdMaps(sourceidMap);
	   
	} else {
	    	this.sourceIdMap = new IdMaps(sourceidMap);
		this.destinationIdMap = new IdMaps(destinationidMap);
	}

	this.contextIdMap = new IdMaps(contextidMap);
	this.contextOntology =  (new SerializableGraphADS()).restoreSerializableObject(cObjectName);
	this.searcher = new CachingStatisticsSearching(indexName);
	this.scoring = new ModelScoring(ScoreType.CI);
	this.infocontent = new InformationContent();

    }

    /**
       Populating the mapping data-structure with information content for each node.
     **/
    private void map(long sNode, long tsCount, long dNode, long tdCount, long cNode, long tcCount, double bf, ArrayList<Mapping> mappings) {
	double icS = infocontent.getInformationContent(searcher.getConceptCount(sNode),tsCount);
	double icD = infocontent.getInformationContent(searcher.getConceptCount(dNode),tdCount);	 
	double icC = infocontent.getInformationContent(searcher.getConceptCount(cNode),tcCount);
 
	Mapping map = new Mapping(sNode,icS,dNode,icD,cNode,icC,bf,sourceIdMap,destinationIdMap,contextIdMap);
	map.printMap();
	mappings.add(map);
	
	//	System.out.println("Mapping : " + sNode + ":" + icS + "," + dNode + ":" + icD + "," + cNode + ":" + icC + "," + bf);
    }

    /**
       Computing counts and bayes factor for given nodes under a context.
     **/
    private double computeNodeMap(Long sNode, Long cNode, Long dNode, double alpha) {
	long [] parentNode = {sNode};
	long [] Counts = searcher.getCountsUnderContext(parentNode,dNode,cNode);
	//	System.out.println(cNode + ":" + dNode + ":" + sNode);
	//	System.out.println(Counts[0] + ":" + Counts[1] + ":" + Counts[2] + ":" + Counts[3]);
	double bayesFactor = scoring.getBayesFactorCI(Counts,alpha);
	//if (bayesFactor > 20)
	    //	    System.out.println(Counts[0] + ":" + Counts[1] + ":" + Counts[2] + ":" + Counts[3]);
	return bayesFactor;
    }

    /**
       Depth-first expansion of ontology nodes.
     **/
    private void expand(long dcompNode, HashSet<Long> nextlevelNodes,  HashMap<Long,HashSet<Long>> adjacencyList, HashSet<Long> visited ) {
	 HashSet<Long> children = adjacencyList.get(dcompNode);
	 if (children != null) {
	     //	System.out.println(children);
	     union(nextlevelNodes,children,visited);
	 }

    }

    /**
       Helper function to concatenate a set number of nodes.
    **/
    private void union(HashSet<Long> nextlevelNodes, HashSet<Long> children, HashSet<Long> visited) {	
	Iterator childIterator = children.iterator();
	while(childIterator.hasNext()) {
	    Long child = (Long) childIterator.next();
	    if (!visited.contains(child)) {
		nextlevelNodes.add(child);
		visited.add(child);
	    }
	}
    }

    /**
       Pushing nodes over stack for a depth first branch and bound algorithm.
     **/
    private void push(ArrayList<Long> stackNodes, HashSet<Long> nodes, HashSet<Long> expanded) {
	Iterator nodeIterator = nodes.iterator();
	while(nodeIterator.hasNext()) {
	    Long node = (Long) nodeIterator.next();
	    if (!expanded.contains(node) && !stackNodes.contains(node))
		stackNodes.add(node);
	}

    }

    /**
       Testing if a node is a part of marked nodes thathave been pruned.
     **/
    private boolean markedContains(Long node, ArrayList<HashSet<Long>> markedNodes) {
	if (markedNodes == null)
	    return false;
	for(int i=0; i < markedNodes.size(); i++) {
	    if (markedNodes.get(i).contains(node)) 
		return true;
	}

	return false;
    }

    /**
       Concatenating marked nodes with unmarked nodes.
     **/
    private void unionList(ArrayList<HashSet<Long>> uMarkedNodes, ArrayList<HashSet<Long>> markedNodes) {
	if (markedNodes != null) {
	    
	    for(int i=0; i < markedNodes.size(); i++)
		uMarkedNodes.add(markedNodes.get(i));
	}
    }

    /**
       Extending the marked list of nodes based on the node under current consideration.
     **/
    private ArrayList<HashSet<Long>> markedMapListContains(Long sNode, ArrayList<HashMap<Long,ArrayList<HashSet<Long>>>> markedNodesMapList) {
	ArrayList<HashSet<Long>> unionMarkedNodes = new ArrayList<HashSet<Long>> ();
	for(int i=0; i < markedNodesMapList.size(); i++) {
	    HashMap<Long,ArrayList<HashSet<Long>>> markedNodesMap = markedNodesMapList.get(i);
	    ArrayList<HashSet<Long>> markedNodes = markedNodesMap.get(sNode);
	    unionList(unionMarkedNodes,markedNodes);
	}
	return unionMarkedNodes;
    }

    /**
       Tests if the marked nodes contain the given set of root nodes.
     **/
   private boolean markedRootContains(ArrayList<HashSet<Long>> markedNodes,HashSet<Long> roots) {
       boolean result = true;
       Iterator rIterator = roots.iterator();
       while(rIterator.hasNext()) {
	   result &= markedContains((Long)rIterator.next(),markedNodes);

       }

       return result;
    }

    /**
       Tests if the marked nodes contain the given list of root nodes.
     **/
    private boolean markedMapListRootContains(Long sNode, ArrayList<HashMap<Long,ArrayList<HashSet<Long>>>> markedNodesMapList, HashSet<Long> roots) {
	ArrayList<HashSet<Long>> markedNodes = markedMapListContains(sNode,markedNodesMapList);
	return markedRootContains(markedNodes,roots);

	}

    /**
       Helper function for computing a three-dimensional depth first branch and bound traversal of the three ontology graphs.
     **/
    private HashMap<Long,ArrayList<HashSet<Long>>> depthFirstBranch(HashSet<Long> sNodes,long tsCount,HashMap<Long,HashSet<Long>> sadjacencyList, long cNode, long tcCount, HashMap<Long,HashSet<Long>> cadjacencyList ,HashSet<Long> dNodes, long dcCount, HashMap<Long,HashSet<Long>> dadjacencyList, ArrayList<HashMap<Long,ArrayList<HashSet<Long>>>> markedNodesMapList,ArrayList<Mapping> mappings ,double alpha, double minThreshold, double thresHold) {

	ArrayList<Long> stackNodes = new ArrayList<Long>();
	HashSet<Long> expanded = new HashSet<Long> ();
	ArrayList<HashSet<Long>>markedNodes = new ArrayList<HashSet<Long>>();

	HashMap<Long,ArrayList<HashSet<Long>>> markedNodesMap = new HashMap<Long,ArrayList<HashSet<Long>>> ();

	push(stackNodes,sNodes,expanded);
	
	while(stackNodes.size() != 0) {

	    int length = stackNodes.size();
	    int markLength = markedNodes.size();

	    Long topsNode = (Long) stackNodes.get(length - 1);
	    if (expanded.contains(topsNode)) {
		stackNodes.remove(length - 1);
		markedNodesMap.put(topsNode,markedNodes);
		markedNodes.remove(markLength - 1);

	    } else {
	        
		HashSet<Long> newMarkedNodes = computeMarkedNodes(topsNode,tsCount,cNode,tcCount,dNodes,dcCount,dadjacencyList,markedNodes,markedMapListContains(topsNode,markedNodesMapList),mappings,alpha,minThreshold,thresHold);
		markedNodes.add(newMarkedNodes);

		    if (!markedRootContains(markedNodes,dNodes) && !markedMapListRootContains(topsNode,markedNodesMapList,dNodes)) { 
	
		    HashSet<Long> children = sadjacencyList.get(topsNode);
		    if (children != null)
			push(stackNodes,children,expanded);

		    }

		    expanded.add(topsNode);

		
	    }

	}
	return markedNodesMap;

    }   

    /**
       Depth first branch and bound algorithm for scalable computations of mappings.
     **/
    private ArrayList<Mapping> depthFirstBranchBound(HashSet<Long> sNodes, long tsCount, HashMap<Long,HashSet<Long>> sadjacencyList ,HashSet<Long> cNodes, long tcCount, HashMap<Long,HashSet<Long>> cadjacencyList, HashSet<Long> dNodes, long dcCount, HashMap<Long,HashSet<Long>> dadjacencyList, ArrayList<Mapping> mappings, double alpha, double minThreshold, double thresHold) {
	
	ArrayList<Long> stackNodes = new ArrayList<Long> ();
	HashSet<Long> expanded = new HashSet<Long> ();
	ArrayList<HashMap<Long,ArrayList<HashSet<Long>>>> markedNodesMapList = new ArrayList<HashMap<Long,ArrayList<HashSet<Long>>>> ();
	
	push(stackNodes,cNodes,expanded);
	
	while(stackNodes.size() != 0) {
	    int length = stackNodes.size();
	    int markLength = markedNodesMapList.size();
	    Long topNode = (Long) stackNodes.get(length - 1);
	    //System.out.println(length + ":" + markLength);
	    //System.out.println(topNode + ":" + expanded);
	    if (expanded.contains(topNode)) {
		stackNodes.remove(length - 1);
		markedNodesMapList.remove(markLength - 1);
	    } else {

		
		HashMap<Long,ArrayList<HashSet<Long>>> newMarkedNodesMap = depthFirstBranch(sNodes,tsCount,sadjacencyList,topNode,tcCount,cadjacencyList,dNodes,dcCount,dadjacencyList,markedNodesMapList,mappings,alpha,minThreshold,thresHold);
		markedNodesMapList.add(newMarkedNodesMap);
		//	System.out.println("------------------------");
		//	System.out.println(topNode);
		//	HashSet<Long> newMarkedNodes = computeMarkedNodes(sNode,tsCount,topNode,tcCount,dNodes,dcCount,dadjacencyList,markedNodes,mappings,alpha,minThreshold,thresHold);
		//	System.out.println(newMarkedNodes);
		//	if (markedContains(new Long(500302),markedNodes))
		//    System.out.println("CC");
		//		System.out.println("-------------------------");
		//	markedNodes.add(newMarkedNodes);
		HashSet<Long> children = cadjacencyList.get(topNode);
		if (children != null)
		    push(stackNodes,children,expanded);
	    
		expanded.add(topNode);
	    }
	}
	
	return mappings;

    }

    /**
       This function broadly takes in two nodes (say source and context), and performs a depth first (or breadth first) search on the destination dag. It generates a set of marked nodes and does not branch ahead in the sub-dag if a nodes is in the marked set. This allows to prune away large parts of the destination dag
     
    **/
    private HashSet<Long> computeMarkedNodes(long sNode, long tsCount, long cNode, long tcCount, HashSet<Long> dNodes, Long dcCount,  HashMap<Long,HashSet<Long>> adjacencyList, ArrayList<HashSet<Long>> markedNodes,ArrayList<HashSet<Long>> moreMarkedNodes,ArrayList<Mapping> mappings, double alpha, double minThreshold, double thresHold) {

	HashSet<Long> newMarkedNodes = new HashSet<Long>();   
	HashSet<Long> nextlevelNodes = new HashSet<Long> ();
	HashSet<Long> levelNodes = new HashSet<Long> ();
	HashSet<Long> temp;
	HashSet<Long> visited = new HashSet<Long>();

	union(levelNodes,dNodes,visited);

	while(levelNodes.size() != 0) {

	    Iterator levelIterator = levelNodes.iterator();

	    while (levelIterator.hasNext()) {
		   
		Long dcompNode = (Long) levelIterator.next();

	  	if (markedContains(dcompNode,markedNodes) || newMarkedNodes.contains(dcompNode) || markedContains(dcompNode,moreMarkedNodes)) 
		    continue;

		double bayesFactor = computeNodeMap(sNode,cNode,dcompNode,alpha);
		//	System.out.println(sNode + ":" + cNode + ":" + dcompNode + ":" + bayesFactor);
		if (bayesFactor <= minThreshold) {
		     newMarkedNodes.add(dcompNode);
		}
		else if (bayesFactor > thresHold) {
		    map(sNode,tsCount,dcompNode,dcCount,cNode,tcCount,bayesFactor,mappings);
		    expand(dcompNode,nextlevelNodes,adjacencyList,visited);		      
		} else {
		    expand(dcompNode,nextlevelNodes,adjacencyList,visited);    
	        }

	    }
	    levelNodes.clear();
	    temp = levelNodes;
	    levelNodes = nextlevelNodes;
	    nextlevelNodes = temp;
	}
	return newMarkedNodes;
	
	
    }

    /**
       Disk-based data structure for writing mappings.
     **/
     private void writeMappingObject(String objectname, ArrayList<Mapping> mappings) {
        try {
            FileOutputStream fos = new FileOutputStream(objectname);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mappings);
            oos.close();
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
        }

   }

    /**
       Computes name of the mapping object.
     **/
    private String computeName() {
	String sourceName = this.sourceOntology.name;
	String destName = this.destinationOntology.name;
	String contName = this.contextOntology.name;

	String name = sourceName+"\t"+destName+"\t"+contName+".obj";
	return name;
    }

    /**
       Breadth-first search for getting all concepts in an ontology.
     **/
    private ArrayList<Long> getAllConcepts(ArrayList<HashSet<Long>> topSort) {
	ArrayList<Long> allConcepts = new ArrayList<Long> ();
	for (int i=0; i < topSort.size(); i++) {
	    HashSet<Long> concepts = topSort.get(i);
	    Iterator cIterator = concepts.iterator();
	    while(cIterator.hasNext()) {
		allConcepts.add((Long) cIterator.next());
	    }

	}
	return allConcepts;
	

    }

    /**
       Computation of context-based maps using a brute-force O(N^3) algorithm.
     **/
    public ArrayList<Mapping> computeMappingsBruteForceCI() {
	System.out.println(this.sourceOntology.name + "\t" + this.destinationOntology.name + "\t" + this.contextOntology.name + "\tBayesFactor");

	//	System.out.println(this.sourceOntology.startIndex + ":" + this.sourceOntology.length + ":" + this.destinationOntology.startIndex + ":" + this.destinationOntology.length + ":" + this.contextOntology.startIndex + ":" + this.contextOntology.length);
	double thresHold = 5.0;
	double alpha = 2.0;
	ArrayList<Mapping> mappings = new ArrayList<Mapping> ();
	
	ArrayList<HashSet<Long>> topSortSource = sourceOntology.ReverseLevelBasedTopologicalSort();
	ArrayList<HashSet<Long>> topSortDestination = destinationOntology.ReverseLevelBasedTopologicalSort();
	ArrayList<HashSet<Long>> topSortContext = contextOntology.ReverseLevelBasedTopologicalSort();
	
	ArrayList<Long> allConceptsSource = getAllConcepts(topSortSource);
	ArrayList<Long> allConceptsDestination = getAllConcepts(topSortDestination);
	ArrayList<Long> allConceptsContext = getAllConcepts(topSortContext);
	//	System.out.println(allConceptsSource.size() + ":" + allConceptsDestination.size() + ":" + allConceptsContext.size());
	/*	long sourceStartIndex = sourceOntology.startIndex;
	long sourceLength = sourceOntology.length;
	long destinationStartIndex = destinationOntology.startIndex;
	long destinationLength = destinationOntology.length;
	long contextStartIndex = contextOntology.startIndex;
	long contextLength = contextOntology.length;
	*/

	long totalCountSource = searcher.getTotalConceptCount(sourceOntology.startIndex,sourceOntology.length);
	long totalCountDestination = searcher.getTotalConceptCount(destinationOntology.startIndex,destinationOntology.length);
	long totalCountContext = searcher.getTotalConceptCount(contextOntology.startIndex,contextOntology.length);
	

	for (int c=0; c < allConceptsContext.size(); c++) {
	    for (int s=0; s < allConceptsSource.size(); s++) {
		for (int d=0; d < allConceptsDestination.size(); d++) {
		    Long source = allConceptsSource.get(s);
		    Long context = allConceptsContext.get(c);
		    Long destination = allConceptsDestination.get(d);
		    double bayesFactor = computeNodeMap(source,context,destination,alpha);
		    if (bayesFactor > thresHold)
			map(source,totalCountSource,destination,totalCountDestination,context,totalCountContext,bayesFactor,mappings);
		}
	     }

	}
	writeMappingObject("BruteForce"+(computeName()),mappings);

	return mappings;

    }

    public ArrayList<Mapping> computeMappingsCI() {
	System.out.println(this.sourceOntology.name + "\t" + this.destinationOntology.name + "\t" + this.contextOntology.name + "\tBayesFactor");
	double thresHold = 5.0;
	double alpha = 2.0;
	double minThresHold = 0.0;
	ArrayList<Mapping> mappings = new ArrayList<Mapping> ();
	    
	ArrayList<HashSet<Long>> topSortSource = sourceOntology.ReverseLevelBasedTopologicalSort();
	ArrayList<HashSet<Long>> topSortDestination = destinationOntology.ReverseLevelBasedTopologicalSort();
	ArrayList<HashSet<Long>> topSortContext = contextOntology.ReverseLevelBasedTopologicalSort();
	
	//	for(int i=0; i < topSortDestination.size(); i++) 
	//  System.out.println(topSortDestination.get(i).size());
	long totalCountSource = searcher.getTotalConceptCount(sourceOntology.startIndex,sourceOntology.length);
	long totalCountDestination = searcher.getTotalConceptCount(destinationOntology.startIndex,destinationOntology.length);
	long totalCountContext = searcher.getTotalConceptCount(contextOntology.startIndex,contextOntology.length);
	
	long sourceConcepts = sourceOntology.length;
	long destinationConcepts = destinationOntology.length;
	long contextConcepts = contextOntology.length;
	  
	HashSet<Long> contextRoots = contextOntology.roots;
	HashSet<Long> sourceRoots = sourceOntology.roots;
	HashSet<Long> destinationRoots = destinationOntology.roots;

	//	ArrayList<HashSet<Long>> markedNodes = new ArrayList<HashSet<Long>> ();

	depthFirstBranchBound(sourceRoots,totalCountSource,sourceOntology.adjacencyList,contextRoots,totalCountContext,contextOntology.adjacencyList,destinationRoots,totalCountDestination,destinationOntology.adjacencyList,mappings,alpha,minThresHold,thresHold);

	writeMappingObject(computeName(),mappings);
	
	//	System.out.println(contextRoots);
	//HashSet<Long> newMarkedNodes = computeMarkedNodes((long) 5951525,totalCountSource,(long) 5792113,totalCountContext,destinationRoots,totalCountDestination,destinationOntology.adjacencyList,markedNodes,mappings,alpha,minThresHold,thresHold);

	//	System.out.println(newMarkedNodes.size());
	//	 computeMarkedNodes((long) 5951525,totalCountSource,(long) 5791876,totalCountContext,destinationRoots,totalCountDestination,destinationOntology.adjacencyList,newMarkedNodes,mappings,alpha,minThresHold,thresHold);
	/*

	Iterator srIterate = sourceRoots.iterator();
	Iterator drIterate = destinationRoots.iterator();
	Iterator crIterate = contextRoots.iterator();
	
	
	Long sourceNode = (long) srIterate.next();
	Long contextNode = (long) crIterate.next();
	Long destNode = (long) drIterate.next();
	
	double bayesFactor = computeNodeMap(sourceNode,contextNode,destNode, alpha);
	if (bayesFactor > thresHold)
	    map(sourceNode, totalCountSource, destNode, totalCountDestination, contextNode, totalCountContext, mappings);
	else if (bayesFactor < minThreshold)
	    markedDestNode.add(destNode);
	    
		
	*/ 
	
	
	/*
	System.out.println("Destination Roots : " + destinationRoots.size());
	System.out.println("Source Roots : " + sourceRoots.size());
	System.out.println("Context Roots : " + contextRoots.size());
	    
	Iterator cIterator = contextRoots.iterator();	
	
	long cNode = 5791998;
	long dNode = 5951525;
	long sNode = 500302;
	int i=0;
	System.out.println(sourceOntology.adjacencyList.keySet().size() + ":" + sourceOntology.length);
	System.out.println(destinationOntology.adjacencyList.keySet().size() + ":" + destinationOntology.length);
	System.out.println(contextOntology.adjacencyList.keySet().size() + ":" + contextOntology.length);
	
	
	HashSet<Long> ss = sourceOntology.adjacencyList.get(new Long(sNode));
	HashSet<Long> dd = destinationOntology.adjacencyList.get(new Long(dNode));
	HashSet<Long> cc = contextOntology.adjacencyList.get(new Long(cNode));

	Iterator s = ss.iterator();
	Iterator d = dd.iterator();
	Iterator c = cc.iterator();

	//	while(d.hasNext()) {
	    double b=0.0;
	    int v=0;
	    int m=0;
	    while (c.hasNext()) {
		long [] parentNode = {sNode};
		long [] Counts = searcher.getCountsUnderContext(parentNode,dNode,cNode);
		System.out.println(cNode + ":" + dNode + ":" + sNode);
		System.out.println(Counts[0] + ":" + Counts[1] + ":" + Counts[2] + ":" + Counts[3]);
		double bayesFactor = scoring.getBayesFactorCI(Counts,alpha);
		b += bayesFactor;
		System.out.println(bayesFactor);
		if (bayesFactor > 3) 
		    v++;
		System.out.println("------------");
		cNode = (Long) c.next();

		m++;
	    }
	    // dNode = (Long) d.next();
	    // s = ss.iterator();
	    System.out.println(v + ":" + m);
	    System.out.println("------------");
	    System.out.println("------------");

	    //	}
	/*		
	while (cIterator.hasNext()) {
	    Long contextNode = (Long) cIterator.next();
	    System.out.println(contextNode);
	    Iterator sIterator = sourceRoots.iterator();
	    while(sIterator.hasNext()) {
		Long sourceNode = (Long) sIterator.next();

		System.out.println(sourceNode);
		Iterator dIterator = destinationRoots.iterator();
		while(dIterator.hasNext()) {
		    Long destinationNode = (Long) dIterator.next();
		    System.out.println(destinationNode);
		    long [] parentNode = {sourceNode};
		    long [] Counts = searcher.getCountsUnderContext(parentNode,destinationNode,contextNode);
		    System.out.println(Counts[0] + ":" + Counts[1] + ":" + Counts[2] + ":" + Counts[3]);
		    double bayesFactor = scoring.getBayesFactorCI(Counts,alpha);
		    System.out.println(bayesFactor);
		    if (bayesFactor >= thresHold) {
			//	System.out.println(Counts[0] + ":" + Counts[1] + ":" + Counts[2] + ":" + Counts[3]);
			//	System.out.println(bayesFactor); 
			map(sourceNode, totalCountSource, destinationNode, totalCountDestination, contextNode, totalCountContext, mappings);
		    }
		}

	    }

	}
		
	*/

	return mappings;	
    }

    public static void main(String [] args) {
	long time = System.currentTimeMillis();
	String sourceID = args[0];
	String destinationID = args[1];
	String contextID = args[2];
	String indexName = args[3];
	String sourceidMap = args[4];
	String destinationidMap = args[5];
	String contextidMap = args[6];

	MappingAlgorithm algorithm = new MappingAlgorithm(sourceID,destinationID,contextID,indexName,sourceidMap,destinationidMap,contextidMap,true);
	algorithm.computeMappingsCI();
	System.out.println(System.currentTimeMillis() - time);


 
    }








}
