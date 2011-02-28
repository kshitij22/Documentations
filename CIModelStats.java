/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/

package ammo;

/**
 * The <code> CIModelStats </code> class encapsulates various model scoring 
 * heuristics for Bayesian network structure learning for ontology mapping.
 */
public class CIModelStats {

    /**
     * Approximation of the log gamma function.
     * @param x Double to be computed log of.
     * @return Value of the log gamma.
     */
public double lgamma(double x) {
      double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
      double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                       + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                       +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
      return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    /**
     * Method computes the log likelihood of a given model conditioned on 
     * the given counts.
     *
     * @param CT Counts from the given model.
     * @param alpha Smoothing co-efficient
     * @param bic Penalizing factor
     * @return Model score
     */
    public double mll(int[] CT, double alpha, int bic) {
        double mll = 0.0;
        double size = (double) CT.length;
        double ak = alpha/size;
        double N = 0.0;
        for (int i = 0; i < CT.length; i++) {
            mll += lgamma(ak + (double) CT[i]) - lgamma(ak);
            N += (double) CT[i];
        }
        mll += lgamma(alpha) - lgamma(alpha + N);
        if (bic > 0 ) {
            mll = mll - (size*0.5*Math.log(size));
        }
        return mll;
    }

    /**
     * Method computes model of dependency between ontology concepts.
     * @param CT Counts from the given model.
     * @param alpha Smoothing co-efficient
     * @param bic Penalizing factor
     * @return Model score
     */
    public double mdep (int[] CT, double alpha,int bic) {
        double mdep = mll (CT,alpha,bic);
        return mdep;
    }

    /**
     * Method computes model of independence between ontology concepts.
     * @param CT Counts from the given model.
     * @param alpha Smoothing co-efficient
     * @param bic Penalizing factor
     * @return Model score
     */
    public double mind (int [] CT, double alpha,int bic) {                                                                                                                                                               
        int [] CT1 = new int[] {CT[0] + CT[1],CT[2] + CT[3]};                                                                                                                                                            
        int [] CT2 = new int[] {CT[0] + CT[2],CT[1] + CT[3]};                                                                                                                                                            
	//	System.out.println(CT1[0] + ":" + CT1[1]);
	//System.out.println(CT2[0] + ":" + CT2[1]);
        double mind = mll(CT1,alpha,bic) + mll(CT2,alpha,bic);                                                                                                                                                           
        return mind;                                                                                                                                                                                                     
    }                                                                                                                                                                                                                    
    /**
     * Method to compute bayes factor between model of dependency and
     * independence.
     *
     * @param CT Counts from the given model.
     * @param alpha Smoothing co-efficient
     * @param bic Penalizing factor
     * @return Bayes Factor
     */
    public double bayesFactor (int [] CT, double alpha, int bic) {                                                                                                                                                       
        double d = (mdep(CT,alpha,bic));                                                                                                                                                                                 
        double i = (mind(CT,alpha,bic));                                                                                                                                                                                 
	//  System.out.println(d + ":" + i);                                                                                                                                                                               
        //System.out.println(d/i);                                                                                                                                                                                       
        return d-i;                                                                                                                                                                                                      
    }                             


 public double getBDeuScore(int [] Counts, double ess) {
        int instances = Counts.length;
        int start = 0;
        double score = 0.0;
        for (start = 0; start < instances; start += 2)
                score += ( lgamma(ess) - lgamma(Counts[start] + Counts[start + 1] + ess) ) + (lgamma(Counts[start] + ess) - lgamma(ess)) + (lgamma(Counts[start + 1]) - lgamma(ess));
    
        return score;
        }

    public static void main(String [] args) {
	CIModelStats cms = new CIModelStats();
	
		for (int i=0; i <= 1000; i++) {
		     int [] Counts = {1016,28,95,6};
		     System.out.println(i + ":" +cms.bayesFactor(Counts,2.0,0));
	     }
	
		    //     int [] Counts = {28,1,4+i,1};
		    //  System.out.println(cms.getBDeuScore(Counts,1.0/2.0));
		//		}
    }
    

}