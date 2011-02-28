/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/

package ammo;

import java.io.File;

import ammo.CountCollector;

import org.apache.lucene.store.Directory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Filter;

/**
 *  The <code>CachingStatisticsSearching</code> class encapsulates the idea of searching a B-Tree index from database available at the National Center Of Biomedical Ontology (NCBO). 
 *
 * @author Kshitij Marwah (ksm@mit.edu)
 * @version 1.0
 */

public class CachingStatisticsSearching {

    /**
     * Handler to the disk-based B-tree data structure for searching.
     *
     */
    public IndexSearcher searcher;

    /**
     * Constructor initializes the handler for searching the disk-based 
     * B-tree index.
     */
    public CachingStatisticsSearching(String indexName) {
	try {
	    File file = new File(indexName);
	    Directory directory = new SimpleFSDirectory(file);
	    searcher = new IndexSearcher(directory,true);
	} catch (Exception e) {
	    System.out.println("Exception1 : " + e.getMessage());
	}
    }

    /**
     * Initializes a hashmap filter for efficient searching.
     * @param concept Concept to be searched.
     * @param length Number of concepts in the ontology.
     * @return Filter data-structure.
     */
    public Filter getBitMapHashFilter(long concept, long length) {
	try {
	    return NumericRangeFilter.newLongRange("concepts", concept,concept+length, true,true);
	} catch (Exception e) {
	    System.out.println("Exception2 : " + e.getMessage());
	    return null;
	}
    }

    /**
     * Generates a query for give ontology concepts.
     * @param concept Concept id to be searched.
     * @param length Total number of concepts.
     * @return Generated query for the data-structure.
     */
    public Query generateQuery(long concept, long length) {
	try {
	    if (length == 0)
		return NumericRangeQuery.newLongRange("concepts",concept,concept+length,true,true);
	    else 
		return NumericRangeQuery.newLongRange("concepts",concept,concept+length,true,false);
	} catch (Exception e) {
	    System.out.println("Exception3 : " + e.getMessage());
	    return null;
	}
    }

    /**
     * Helper function to get instance and convert to byte data-structure.
     * @param instance Long representing an instance.
     * @param plength Length of string.
     * @return Byte-array representing the conversion.
     */
    private byte [] getInstance(long instance, long plength) {
	String bString = Long.toBinaryString(instance);
	long blength = (long) bString.length();
	for(int i=0; i < plength - blength; i++)
	    bString = "0" + bString;
	//System.out.println(bString);
	return bString.getBytes();
	

    }

    /**
     * Method to get counts for a concept and the parents under a given context      * per instance.
     * @param parentConcept Array of parent concepts in a given ontology. 
     * @param childConcept Child concept for a given ontology.
     * @param contextConcept Context concept for a given ontology.
     * @param instance Long representing an instance.
     * @return Long-array representing the possible counts.
     */
    private long [] getCountsUnderContextPerInstance(long [] parentConcept, long childConcept, long contextConcept, long instance) {
	try {
	    
	    long [] Count = {-1,-1};
	    long pLength = (long) parentConcept.length;
	    byte [] bInstance = getInstance(instance,pLength);
	    //  System.out.println("Length : " + pLength);
	    int iLength = bInstance.length;
	    BooleanFilter booleanFilter1 = new BooleanFilter();
	    BooleanFilter booleanFilter0 = new BooleanFilter();
	    booleanFilter1.add(new FilterClause(getBitMapHashFilter(contextConcept,0), BooleanClause.Occur.MUST));
	    booleanFilter0.add(new FilterClause(getBitMapHashFilter(contextConcept,0), BooleanClause.Occur.MUST));
	    long pConcept = -1;
	    CountCollector counts = new CountCollector();

	    for(long start = 0; start < iLength; start++) { 
		int istart = (int) start;
		if ((bInstance[istart] - 48) == 1) {
		    //System.out.println(parentConcept[istart]);
		    booleanFilter1.add(new FilterClause(getBitMapHashFilter(parentConcept[istart],0), BooleanClause.Occur.MUST));
		    booleanFilter0.add(new FilterClause(getBitMapHashFilter(parentConcept[istart],0), BooleanClause.Occur.MUST));
		    pConcept = parentConcept[istart];
		
		}
		else { 
		    //System.out.println(parentConcept[istart]);
		    booleanFilter1.add(new FilterClause(getBitMapHashFilter(parentConcept[istart],0), BooleanClause.Occur.MUST_NOT));
		    booleanFilter0.add(new FilterClause(getBitMapHashFilter(parentConcept[istart],0), BooleanClause.Occur.MUST_NOT));
		}
	    }
	    if (pConcept != -1) {
		booleanFilter0.add(new FilterClause(getBitMapHashFilter(childConcept,0), BooleanClause.Occur.MUST_NOT));
		searcher.search(generateQuery(pConcept,0),booleanFilter0,counts);
		Count[0] = counts.count;
	    } 
	    searcher.search(generateQuery(childConcept,0),booleanFilter1,counts);
	    Count[1] = counts.count;
	    return Count;
	} catch(Exception e) {
	    System.out.println("Exception4 : " + e.getMessage());
	    return null;
	}
}

   
    /**
     * Method to get counts for a concept and the parents under a given context.
     * @param parentConcept Array of parent concepts in a given ontology. 
     * @param childConcept Child concept for a given ontology.
     * @param contextConcept Context concept for a given ontology.
     * @return Long-array representing the possible counts.
     */
    public long [] getCountsUnderContext(long [] parentConcept, long childConcept, long contextConcept) {
	try {
	long pLength = parentConcept.length;
	long iLength = (long) Math.pow(2.0,(double) pLength);
	long [] Count = new long[2*((int)iLength)];
	long sum = 0;
	CountCollector counts = new CountCollector();
	searcher.search(generateQuery(contextConcept,0), counts);
	
	long contextCount = counts.count;
	//System.out.println(contextCount);
	for(long start = 0; start < iLength; start = start + 1) {
	    int istart = (int) start;
	    long [] tCount = getCountsUnderContextPerInstance(parentConcept, childConcept, contextConcept, start); 

	    // System.out.println("Here2 : " + tCount[0] + ":" + tCount[1]);
	    Count[2*istart] = tCount[0];
	    Count[2*istart + 1] = tCount[1];
	}
	for(long start = 1; start < Count.length; start++) {
	    
	    sum += Count[(int) start];
	}
	//System.out.println(contextCount + ":" + sum);
	Count[0] = contextCount - sum;
	return Count;
	} catch (Exception e) {
	    System.out.println("Exception5 : " + e.getMessage());
	    return null;
	}
    }

    /**
     * Getting total concept count in the data-structure for a given ontology.
     * @param concept Starting concept identifier for a given ontology.
     * @param length Total number of concepts in the ontology.
     * @return Total concept count.
     */
    public long getTotalConceptCount(long concept, long length) {
	try {
	    CountCollector counts = new CountCollector();
	    searcher.search(generateQuery(concept,length),counts);
	    return (long) counts.count;
	} catch(Exception e) {
	    System.out.println("Exception6 : " + e.getMessage());
	    return -1;
	}

    }

    /**
     * Getting total concept count in the data-structure for a given concept.
     * @param concept Concept identifier for a given ontology.
     * @return Total count for the given concept.
     */
    public long getConceptCount(long concept) {
	try {
	    CountCollector counts = new CountCollector();
	    searcher.search(generateQuery(concept,0),counts);
	    return (long) counts.count;
	} catch(Exception e) {
	    System.out.println("Exception7 : " + e.getMessage());
	    return -1;
	}

    }

    public static void main(String[] args) throws Exception {
	long time = System.currentTimeMillis();
	 CachingStatisticsSearching css = new CachingStatisticsSearching("./numeric-index-all-EAT-OBR");
	//   CachingStatisticsSearching css = new CachingStatisticsSearching("./numeric-index-bsm");
	
	long[] parent = {30658};
	//long [] counts = css.getCountsUnderContext(parent,3990186,4001841);
	

	for (int j=0; j < 100; j++) {

	    	CountCollector cc = new CountCollector();
		BooleanFilter b = new BooleanFilter();
		//	for(int j=0; j < 1; j++) {
		    b.add(new FilterClause(css.getBitMapHashFilter(5652316+j,0),BooleanClause.Occur.MUST));
		    b.add(new FilterClause(css.getBitMapHashFilter(2324125+j,0),BooleanClause.Occur.MUST_NOT));  
		    //	}

	    for(int i=0; i < 100; i++) {
	   
	
		css.searcher.search(css.generateQuery(4641234+i,10),b,cc);
		//System.out.println(cc.count);
	    }
	}
	System.out.println(System.currentTimeMillis() - time);
	//System.out.println(css.getTotalConceptCount(306558,1000));
	
	//	System.out.println(css.getConceptCount(6486823));
	
	//System.out.println(counts[0] + ":" + counts[1] + ":" + counts[2] + ":" + counts[3]);
    }

}