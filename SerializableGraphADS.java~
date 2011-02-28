package ammo;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

public class SerializableGraphADS {

    GraphADS ontologyDAG;

    public SerializableGraphADS(String ontologyID) {
	this.ontologyDAG = new GraphADS(ontologyID);
	
    }
    public SerializableGraphADS() {

    }

    public void writeSerializableObject(String filename) {
	try {
	    FileOutputStream fos = new FileOutputStream(filename);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(this.ontologyDAG);
	    oos.close();
	       } catch (Exception e) {
	    System.out.println("Exception " + e.getMessage());
	}
    }

    public GraphADS restoreSerializableObject(String objectname) {
	try {
	    GraphADS ontologyDAG;
	    FileInputStream fis = new FileInputStream(objectname);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    ontologyDAG = (GraphADS) ois.readObject();
	    ois.close();
	    return ontologyDAG;
	} catch (Exception e) {
	    
	    System.out.println("Exception : " + e.getMessage());
            return null;
	}

    }

    public static void main(String [] args) throws Exception {
	
	SerializableGraphADS sga = new SerializableGraphADS();
	GraphADS ga = sga.restoreSerializableObject("serialObjectsOBS_Roots/" + args[0] + ".obj");
	
	IdMaps im = new IdMaps("idMaps/"+args[0]+"idMaps.obj");
	
	HashMap<Long,HashSet<Long>> hm = ga.adjacencyList;

	Set set = hm.entrySet();
	Iterator i = set.iterator();
	
	while(i.hasNext()){
	    Map.Entry me = (Map.Entry)i.next();
	    Long s = (Long) me.getKey();
	    HashSet<Long> l = (HashSet<Long>) me.getValue();
	    
	    Iterator j = l.iterator();
	    while(j.hasNext()) {
		Long d = (Long) j.next();
		System.out.println(im.getName(s) + "\t" + im.getName(d));
	    }
	    
	    // System.out.println(me.getKey() + " : " + me.getValue() );
	}




	/*	//SerializableGraphADS sga = new SerializableGraphADS(args[0]);
	//sga.writeSerializableObject(args[0]+".obj");
	Resource resource = new Resource("ncbo-obsdb1.sunet" , "obs_stage", "ammo", "ammo");

	//	Resource resource = new Resource("ncbodev-obrdbmaster1.sunet", "resource_index_test", "ammo", "ammo");
	ResultSet rs = resource.statement.executeQuery("SELECT ontologyID FROM OBS_OT");
	rs.beforeFirst();
	while(rs.next()) {
	    
	    String id = rs.getString("ontologyID");
	    System.out.println(id);
	    SerializableGraphADS sga = new SerializableGraphADS(id);
	    long start = sga.ontologyDAG.startIndex;
	    long end = start + sga.ontologyDAG.length;
	    
	    HashMap<Long,String> idMaps = new HashMap<Long,String> ();
	    IdMaps idMap = new IdMaps();
	    Resource resource2 =  new Resource("ncbo-obsdb1.sunet" , "obs_stage", "ammo", "ammo");

	    ResultSet rs2 = resource.statement.executeQuery("SELECT termName,conceptID from OBS_TT where conceptID >= " + start + " AND conceptID < " + end);
	    rs2.beforeFirst();
	    while(rs2.next()) {
		String name = rs2.getString("termName");
		Long conceptid = rs2.getLong("conceptID");
		idMaps.put(conceptid,name);
	    }
	    rs2.close();

	    idMap.writeIdMap(id+"idMaps.obj",idMaps);
		//sga.writeSerializableObject(id+".obj");
	}
	rs.close();*/
    }
	

}
