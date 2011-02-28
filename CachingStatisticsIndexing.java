/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/

package ammo;

import java.io.File;

import java.util.ArrayList;

import java.sql.ResultSet;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;


/**
 * The <code>CachingStatisticsIndexing</code> class encapsulates the idea of creation of a B-Tree index from database available at the National Center Of Biomedical Ontology (NCBO). 
 *
 * @author Kshitij Marwah (ksm@mit.edu)
 * @version 1.0
*/
public class CachingStatisticsIndexing {

    /**
     * Characterizes the name of the disk based B-Tree data structure for 
     * index storage.
    */
    String indexName;

    /**
     * Characterizes the name of the table from NCBO database that needs to
     * be indexed.
     */
    String tableName;

    /**
     * Handler to the disk-based data structure.
     *
     */
    IndexWriter writer;

    /**
     * Handler to a sub-set of rows from NCBO tables.
     *
     */
    ResultSet resultset;

    /**
     * Class representing NCBO database.
     */
    Resource resource;

    /**
     * Limit characterizing the number of documents in memory before writing on      * to the disk.
     */
    int flushLimit = 10000;

    /**
     * Constructor initializes available resource and the handler for the
     * disk-based datastructure.
     */
    public CachingStatisticsIndexing(String tableName, String indexName, boolean append) {
	this.indexName = indexName;
	this.tableName = tableName;
	resource = new Resource("ncbodev-obrdbmaster1.sunet", "resource_index_test", "ammo", "ammo");
	
	writer = saveIndex(indexName,append);
    }

    /**
     * Writes the memory based B-tree index onto the disk.
     *
     * @param indexName Name of the file containing the index
     * @param append Boolean term to append on existing index or not
     * @return Handler to the index
     */
    private IndexWriter saveIndex(String indexName, boolean append) {
	try {
	    File file = new File(indexName);
	    Directory directory = new SimpleFSDirectory(file);
	    Analyzer analyzer = new WhitespaceAnalyzer();
	    return new IndexWriter(directory,analyzer,append,IndexWriter.MaxFieldLength.UNLIMITED);
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return null;
	}
    }

    /**
     * Creates a Lucene document from an arraylist of concepts.
     * @param Element Id of element to be indexed.
     * @param Concepts List of concept identifires to be indexed.
     * @return Lucene document.
     */
    private Document makeDocument(Long element, ArrayList<Long> concepts) { 
	try {
	    Document document = new Document();
	    NumericField nElem = new NumericField("element");
	    nElem.setLongValue(element);
	    document.add(nElem);

	    for(int i=0 ; i < concepts.size(); i++) {
		NumericField nCon = new NumericField("concepts");
		nCon.setLongValue(concepts.get(i));
		document.add(nCon);
	    }
	    return document;
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return null;
	}
	    
    }

    /**
     * Flushes the index from the memory to the disk.
     * @return null
     */
    private void flushIndex() {
	try {
	    writer.optimize();
	    writer.close();
	} catch (Exception e) {
	    System.out.println("Error flushing index : " + e.getMessage());
	}
    }

    
    /**
     * Resets the index handler.
     * @return null
     */
    private void resetIndex() {
	try {
	    flushIndex();
	    writer = saveIndex(indexName,false);
	} catch (Exception e) {
	    System.out.println("Error reseting index : " + e.getMessage());
	}
    }

    /**
     * Adds document to the disk-based index.
     * @param Document Document containing concept identifiers.
     * @param Count Integer specifying the number of documents added.
     * @return Count of the documents added.
     */
    private long addDocument(Document document, long count) {
	try {
	    writer.addDocument(document);
	    if (count == flushLimit) {
		resetIndex();
		return 0;
	    } else {
		return count;
	    }
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return -1;
	}
    }

    /**
     * Method indexes a given NCBO database resource into a file-based
     * b-tree structure.
     * @return null
     */
    public void resourceIndexer() {
	Long [] data = null;
	long tempElement = 1;
	long countDocs = 0;
	long countRows = 0;
	long element = 0,concept;
	long startIndex = 1;
	ArrayList<Long> concepts = new ArrayList<Long> ();
       
	boolean cont = true;

	while(cont) { 
	    resultset = resource.getResourceStatistics(tableName,"element_id","concept_id", startIndex);

	    while ((data = resource.getResourceData(resultset)) != null) {
		countRows ++;
		element = data[0];
		concept = data[1];
		//System.out.println(element);
		if (element > tempElement) {
		    //System.out.println(element);
		    countDocs ++;
		    Document document = makeDocument(tempElement,concepts);
		    concepts.clear();
		    concepts.add(concept);
		    tempElement = element;
		    countDocs = addDocument(document,countDocs);
		} else {
		    concepts.add(concept);
		}

	    }
	    System.out.println(countDocs);
	    Document document = makeDocument(element,concepts);
	    concepts.clear();
	    resetIndex();
	    if (countRows == 0) {
		cont = false;
	    }
	    else
		countRows = 0;
	    startIndex += resource.limit;
	    resetIndex();
	}
	flushIndex();
    }

    public static void main(String [] args) {
	long time = System.currentTimeMillis();
	CachingStatisticsIndexing csi = new CachingStatisticsIndexing("obr_bsm_annotation","numeric-index-bsm",true);
	csi.resourceIndexer();
	System.out.println(time - System.currentTimeMillis());

    }

}



