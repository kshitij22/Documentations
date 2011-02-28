package ammo;

import java.util.ArrayList;

import java.io.ObjectInputStream;
import java.io.FileInputStream;

public class MappingProcessing {


    ArrayList<Mapping> mappings;

    double cutOff = 0.5;

    double sourceMaxInfo = 0.0;

    double destinationMaxInfo = 0.0;

    double contextMaxInfo = 0.0;

    public MappingProcessing(String objectName) {
	try {

	    FileInputStream fis = new FileInputStream(objectName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    this.mappings = (ArrayList<Mapping>) ois.readObject();
	    ois.close();
	    getPruningInfo();
	    System.out.println(mappings.size());

	} catch (Exception e) {
	    System.out.println("Exception : " + e.getMessage());
	}

    }

    private void getPruningInfo() {

	for (int i=0; i < mappings.size(); i++) {
	    Mapping map = mappings.get(i);
	    if (map.sourceInfo > sourceMaxInfo)
		sourceMaxInfo = map.sourceInfo;
	    if (map.destinationInfo > destinationMaxInfo)
		destinationMaxInfo = map.destinationInfo;
	    if (map.contextInfo > contextMaxInfo)
		contextMaxInfo = map.contextInfo;
	    
	}
	//	System.out.println(sourceMaxInfo + ":" + destinationMaxInfo + ":" + contextMaxInfo);

    }


    private boolean inRange(Mapping map) {
	boolean result = true;
	if (map.sourceInfo <= sourceMaxInfo*cutOff || map.destinationInfo <= destinationMaxInfo*cutOff || map.contextInfo <= contextMaxInfo*cutOff)
	    result = false;
	return result;
    }


    public void printPrunedNodes() {
	for (int i=0; i < mappings.size(); i++) {
	    Mapping map = mappings.get(i);
	    if (inRange(map))
		map.printMap();

	}

    }

    public static void main (String [] args) {
	String objectname = args[0];
	System.out.println(args[0]);
	MappingProcessing mp = new MappingProcessing(objectname);
	mp.printPrunedNodes();
    }

}