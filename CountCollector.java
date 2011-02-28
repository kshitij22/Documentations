package ammo;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.index.IndexReader;


public class CountCollector extends Collector {

    int count;

    public CountCollector() {
	count = 0;
    }

    public void setScorer(Scorer scorer) {

    }

    public boolean acceptsDocsOutOfOrder() {
	return true;
    }

    public void collect(int doc) {
	count ++;
    }

    public void setNextReader(IndexReader reader, int docBase) {
	this.count = 0;
    }

}