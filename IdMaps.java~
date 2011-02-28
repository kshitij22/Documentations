/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/
package ammo;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;

import java.sql.ResultSet;

/**
 * The class <code>IdMaps</code> encapsulates a mapping from ontology concepts 
 * to numerical identifiers.
 */
public class IdMaps implements Serializable {

    /**
     * Hashmap to map integer identifiers to string concepts.
     */
    HashMap<Long,String> idMaps;

    /**
     * Copy constructor for initializing identifiers.
     */

    public IdMaps(HashMap<Long,String> idMaps) {
	this.idMaps = idMaps;
    }

    /**
     * Constructor to restore hashmaps from disk-based datastructure.
     *
     */
    public IdMaps(String objectname) {
	this.idMaps = restoreIdMap(objectname);
    }

    public IdMaps() {

    }

    /**
     * Gets name of a concept identifier.
     * @param node Numerical identifier for node.
     * @return Concept name.
     */
    public String getName(Long node) {
	String name = idMaps.get(node);
	if (name == null)
	    return "";
	else
	    return name;
    }

    public String getID(String name) {
	

    }

    /**
     * Method to write a hashmap object to a disk-based data-structure.
     * @param filename Name of the file.
     * @param idMap Hashmap containing the mappings.
     * @return null.
     */
    public void writeIdMap(String filename,HashMap<Long,String> idMap) {
	try {
	    FileOutputStream fos = new FileOutputStream(filename);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(idMap);
	    oos.close();
	} catch (Exception e) {
	    System.out.println("Exception : " + e.getMessage());
	}
    }

     /**
     * Method to restore a hashmap object from a disk-based data-structure.
     * @param filename Name of the file.
     * @return idMap Hashmap containing the mappings.
     */
    public HashMap<Long,String> restoreIdMap(String filename) {
	try {
	    FileInputStream fis = new FileInputStream(filename);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    HashMap<Long,String> idMaps = (HashMap<Long,String>) ois.readObject();
	    ois.close();
	    return idMaps;
	} catch (Exception e) {
	    System.out.println("Exception : " + e.getMessage());
            return null;
	}

    }




    public static void main(String [] args) throws Exception {

	IdMaps idMap = new IdMaps(args[0]);
	System.out.println(idMap.getName(new Long(5951525)));
	/*   HashMap<Long,String> idMaps = new HashMap<Long,String> ();
	
	     Resource resource = new Resource("ncbo-obsdb1.sunet" , "obs_stage", "ammo", "ammo");

	     ResultSet rs = resource.statement.executeQuery("SELECT termName,conceptID from OBS_TT ");
	     rs.beforeFirst();
	     while(rs.next()) {
	     String name = rs.getString("termName");
	     Long id = rs.getLong("conceptID");
	     idMaps.put(id,name);
	     }
	     rs.close();

	     idMap.writeIdMap("idMaps.obj",idMaps);

	*/
    }
	    

} 