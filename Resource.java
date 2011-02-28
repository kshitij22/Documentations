/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/

package ammo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.Statement;

/**
 * The <code>Resource</code> abstracts NCBO database and tables for efficient 
 * retrievel and manipulation of table rows.
 *
 * @author Kshitij Marwah (ksm@mit.edu)
 * @version 1.0
 */
public class Resource {

    /**
     * Handler for a TCP/IP connection for NCBO database.
     */
    Connection connection;

    /**
     * Characterizes a statement for the SQL query on the database.
     */
    Statement statement;

    /**
     * Characterizes a limit on the number of rows retrieved by a SQL query.
     */
    public long limit = 10000;

    /**
     * Constructor initializes a given resource with the server metrics and the
     * database to be abstracted.
     */
    public Resource (String serverName, String databaseName, String username, String password) {
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    connection = DriverManager.getConnection("jdbc:mysql://" + serverName + ":3306/" + databaseName + "?user=" + username + "&password=" + password);
	    statement = connection.createStatement();
	} catch (Exception e) {
	    System.out.println("Exception in initializing database: " + e.getMessage());
	}
    }
    
    /** This function is for getting a single row statistic. Mainly targeted to       * compute concept name, ontology name, count of ontology concepts, start 
      * of concept id.
      * @param tableName Name of table for SQL query to be executed.
      * @param inpColumnName Name of input column for SQL query.
      * @param outColumnName Name of output column for SQL query.
      * @param id Identifier characterizing the value of the input column.
      */
    public String getOntologyStatistics(String tableName, String inpColumnName, String outColumnName, String id) {
	try {
	    String result = "";
	    ResultSet resultset = statement.executeQuery("SELECT " + outColumnName +" AS result FROM " + tableName +  " WHERE " + inpColumnName + " = " + id + " LIMIT 1");
	    resultset.beforeFirst();
	    while(resultset.next()) {
		result = resultset.getString("result");
	    }
	    resultset.close();
	    return result;
	   
	} catch (Exception e) {
	    System.out.println("Exception : " + e.getMessage());
	    return null;
	}

    }

    /** 
     * This function returns the  adjacency List for a given ontology parent of      * a given concept, provided their are two tables : 
     * one representing the concept to meta (local) concept mapping and other re     * presenting meta concept to parent concept mapping. This is specifically       * tailored towards the OBS_STAGE database at NCBO.
     * @param id Identifier for ontology concepts.
     * @param len Number of concepts in an ontology.
     * @param adjacencyList Graph representation for the given ontology.
     * @return Adjacency list for the ontology.
     */
    public HashSet<Long> getOntologyGraphSpecOBS(long id, long len, HashMap<Long,HashSet<Long>> adjacencyList) {
	try {
	    HashSet<Long> roots = new HashSet<Long> ();
	    String query = "SELECT s.conceptID AS parent, c.conceptID AS child FROM OBS_CT AS c, OBS_ISAPT AS r, OBS_CT AS s where c.conceptID >= " + id + " AND c.conceptID < " + (id + len) + " AND c.localConceptID = r.localConceptID AND r.level=1 AND s.localConceptID = r.parentLocalConceptID";
	    ResultSet resultset = statement.executeQuery(query);
	    resultset.beforeFirst();
	    while(resultset.next()) {
		long parent = resultset.getLong("parent");
		long child = resultset.getLong("child");

		if (!adjacencyList.containsKey(parent)) {
		    HashSet<Long> childs = new HashSet<Long> ();
		    childs.add(child);
		    adjacencyList.put(parent,childs);
		} else {
		    HashSet<Long> childs = adjacencyList.get(parent);
		    childs.add(child);
		    adjacencyList.put(parent,childs);
		}
	    }
	    resultset.close();
	    query = "SELECT conceptID as root from OBS_CT where conceptID >= " + id + " AND conceptID < " + (id + len) + " AND isTopLevel = 1";
	    resultset = statement.executeQuery(query);
	    resultset.beforeFirst();
	    while(resultset.next()) {
		roots.add(resultset.getLong("root"));
	    }
	    resultset.close();
	    return roots;
	    
	} catch (Exception e) {
	    System.out.println("Exception : " + e.getMessage());
	    return null;
	}

    }

    /**
     * This function creates the graph adjacency list and also returns the set 
     * of roots in the ontology. It is designed specific to the 
     * NCBO's resource_index_test database.
     * @param id  Identifier for ontology concepts.
     * @param len Number of concepts in an ontology.
     * @param adjacencyList Graph representation for the given ontology.
     * @return Adjacency list for the ontology.
     */
    public HashSet<Long> getOntologyGraphSpecObs(long id, long len, HashMap<Long,HashSet<Long>> adjacencyList) {
	try {
	    HashSet<Long> roots = new HashSet<Long> ();
	    String query = "select concept_id AS child , parent_concept_id AS parent FROM obs_relation WHERE concept_id >= " + id + " AND concept_id < " + (id + len) + " AND level = 1";
	    ResultSet resultset = statement.executeQuery(query);
	    resultset.beforeFirst();
	    while(resultset.next()) {
		long parent = resultset.getLong("parent");
		long child = resultset.getLong("child");
			
		if (!adjacencyList.containsKey(parent)) {
                    HashSet<Long> childs = new HashSet<Long> ();
                    childs.add(child);
                    adjacencyList.put(parent,childs);
                } else {
                    HashSet<Long> childs = adjacencyList.get(parent);
                    childs.add(child);
                    adjacencyList.put(parent,childs);
                }

		
	    }
	    resultset.close();
	    query = "SELECT id as root from obs_concept where id >= " + id + " AND id < " + (id + len) + " AND is_toplevel = 1";
            resultset = statement.executeQuery(query);
            resultset.beforeFirst();
            while(resultset.next()) {
                roots.add(resultset.getLong("root"));
            }
            resultset.close();
            return roots;
	    
	} catch(Exception e) {
	    System.out.println(e.getMessage());
	    return null;
	}

    }

    /* This helper function is for executing and returning and iterator to a table,
     * would help in adding data from resource to a structure if the table is huge.
     */
    public ResultSet getResourceStatistics(String tableName, String outColumnName1, String outColumnName2, long startIndex) {
	try {
	    String query = "SELECT " + outColumnName1 + " AS element , " + outColumnName2 + " AS concept FROM " + tableName + " WHERE " + outColumnName1 + " >= " + startIndex + " AND " + outColumnName1 + " < " + (startIndex + limit);
	    ResultSet resultset = statement.executeQuery(query);
	    resultset.beforeFirst();
	    return resultset;
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return null;
	}
    }

    /**
     * This function iterates through the table and gets values. This is 
     * specifically tailored for obs based tables, in NCBO resource tables. 
     * This is supposed to run after the previous helper function.
     * @param resultset Data-structure containing the required data.
     * @return Data-array containing concepts returned.
     */
    public Long [] getResourceData(ResultSet resultset) {
	try {
	    
	    Long [] data = new Long[2];
	    if (resultset.next()) {
		data[0] = resultset.getLong("element");
		data[1] = resultset.getLong("concept");
		return data;
	    } else {
		resultset.close();
		return null;
	    }
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return null;
	}

    }

    /**
     * Close the database connection.
     * @return null.
     */
    public void close () {
	try {
	    statement.close();
	    connection.close();
	} catch (Exception e) {
	    System.out.println("Exception : " + e.getMessage());
	}
    }

    public static void main(String [] args) {
	Resource resource = new Resource("ncbo-obsdb1.sunet" , "obs_stage", "ammo", "ammo");
	String ontologyName = resource.getOntologyStatistics("OBS_OT","ontologyID","ontologyName","64");
	System.out.println(ontologyName);
	String conceptID = resource.getOntologyStatistics("OBS_CT", "ontologyID", "conceptID", "64");
	System.out.println(conceptID);
	String conceptLen = resource.getOntologyStatistics("OBS_CT", "ontologyID", "count(conceptID)", "64");
	System.out.println(conceptLen);
	HashMap<Long,HashSet<Long>> hm = new HashMap <Long,HashSet<Long>> ();
	HashSet<Long> leaves =  resource.getOntologyGraphSpecOBS(Long.parseLong(conceptID),Long.parseLong(conceptLen),hm);
	System.out.println(leaves.size());

	long time = System.currentTimeMillis();
	Resource resource2 = new Resource("ncbodev-obrdbmaster1.sunet","resource_index_test","ammo","ammo");
	String ontologyName2 = resource2.getOntologyStatistics("obs_ontology","id", "name","239");
	System.out.println(ontologyName2);
	String conceptID2 = resource2.getOntologyStatistics("obs_concept","ontology_id","id","239");
	System.out.println(conceptID2);
	String conceptLen2 = resource2.getOntologyStatistics("obs_concept","ontology_id","count(id)","239");
	System.out.println(conceptLen2);
	HashMap<Long,HashSet<Long>> hm2 = new HashMap <Long,HashSet<Long>> ();
	HashSet<Long> leaves2 = resource2.getOntologyGraphSpecObs(Long.parseLong(conceptID2),Long.parseLong(conceptLen2),hm2);
	System.out.println(leaves2.size());
	
	long startIndex = 10000;
	ResultSet res = resource2.getResourceStatistics("obr_bsm_annotation","element_id","concept_id", startIndex);
	Long [] data;
	while((data = resource2.getResourceData(res)) != null) {
	    System.out.println(data[0] + ":" + data[1]);
	    
	}
		System.out.println(System.currentTimeMillis() - time);

	/*
	Set set = hm.entrySet();
	Iterator i = set.iterator();
	while(i.hasNext()){
      		Map.Entry me = (Map.Entry)i.next();
      		System.out.println(me.getKey() + " : " + me.getValue() );
    	}
	
	for (int i=0 ; i < leaves.size(); i++)
	    System.out.println(leaves.get(i));
	*/
	    //System.out.println(hm.get(1).get(0));
    }
}
