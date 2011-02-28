/*
AMMO: Automated Method For Mapping Ontology
A Bayesian-centric mapping approach for bioinformatics
*/

package ammo;

/**
 * The <code> ModelScoring </code> class encapsulates various model scoring 
 * heuristics for Bayesian network structure learning for ontology mapping.
 */
public class ModelScoring {

    /**
     * Characterizes the type of scoring method to be used.
     */
    ScoreType score;
    

    /**
     * Constructor to initialize the type of scoring model to be used.
     *
     */
    public ModelScoring(ScoreType score) {
	this.score = score;

    }

    /**
     * Sets the type of score.
     * @param score Type of score.
     * @return null.
     */
    public void setScoreType(ScoreType score) {
	this.score = score;
    }

    /**
     * Gets the model score.
     * @param Counts Statistics for model scoring.
     * @parameter Smoothing co-efficeint.
     * @return Model score.
     */
   public double getModelScore(long [] Counts, double parameter) {
	if (score == ScoreType.CI)
		return getBayesFactorCI(Counts,parameter);
	else if (score == ScoreType.BDeu)
		return getBDeuScore(Counts, parameter);
	else return -1.0;
	
  }
     /**
     * Approximation of the log gamma function.
     * @param x Double to be computed log of.
     * @return Value of the log gamma.
     */
    private double lgamma(double x) {
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
     * @return Model score
     */
    private  double MarginalLogLikelihood(long [] Counts, double alpha) {
	double mll = 0.0;
	double size = (double) Counts.length;
	double alphak = alpha/size;
	double totalCounts = 0.0;

	for (int i = 0; i < Counts.length; i++) {
	    mll += lgamma(alphak + (double) Counts[i]) - lgamma(alphak);
	    totalCounts += (double) Counts[i];
	}

	mll += lgamma(alpha) - lgamma(alpha + totalCounts);
	return mll;
    }

    /**
     * Method computes model of dependency between ontology concepts.
     * @param CT Counts from the given model.
     * @param alpha Smoothing co-efficient
     * @return Model score
     */
    private double CIModelDependence(long [] Counts, double alpha) {
	return MarginalLogLikelihood(Counts, alpha);
    }
    
    /**
     * Method to compute marginal counts for a given child.
     * @param Counts Statistics from the given model.
     * @return Long Array with marginal counts.
     */
    private long [] getMarginalCountChild(long [] Counts) {
	int start = 0;
	int length = Counts.length;
	long[]  MarginalCounts = {0,0};
	while (start < length) {
	    MarginalCounts[start%2] += Counts[start];
	    start ++;
	}

	return MarginalCounts;
    }

     /**
     * Method to compute marginal counts for a given parent.
     * @param Counts Statistics from the given model.
     * @param parent Parent identifier.
     * @return Long Array with marginal counts.
     */
    private long [] getMarginalCountParent(long [] Counts, int parent) {
	int start = 0;
	int length = Counts.length;
        long [] MarginalCounts = {0,0};
	int step = (int) Math.pow(2.0, (int) parent);
	int tCount = 0;
	while (start < length) {
	    for (int i=start; i< start+step; i++) 	    
	    	MarginalCounts[tCount] += Counts[i];	
	    start += step;
	    if (tCount == 0)
		tCount = 1;
	    else
		tCount = 0;		    
	}
	return MarginalCounts;

    }

     /**
     * Method computes model of independence between ontology concepts.
     * @param CT Counts from the given model.
     * @param alpha Smoothing co-efficient.
     * @return Model score
     */
    private double CIModelIndependence(long [] Counts, double alpha) {
	int length = Counts.length;
 	int numParents = (int) (Math.log(length/2)/Math.log(2));
	long [] MarginalCountsChild = getMarginalCountChild(Counts);
	//	System.out.println(MarginalCountsChild[0] + ":" + MarginalCountsChild[1]);
	double mll = MarginalLogLikelihood(MarginalCountsChild,alpha);
	for (int parent = 1; parent <= numParents; parent ++)
		mll += MarginalLogLikelihood(getMarginalCountParent(Counts,parent),alpha);

	
	return mll;   
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
   public double getBayesFactorCI(long [] Counts, double alpha) {
	return CIModelDependence(Counts,alpha)-CIModelIndependence(Counts,alpha);

	}

  public double getBDeuScore(long [] Counts, double ess) {
	int instances = Counts.length;
	int start = 0;
	double score = 0.0;
	for (start = 0; start < instances; start += 2)
		score += ( lgamma(ess) - lgamma(Counts[start] + Counts[start + 1] + ess) ) + (lgamma(Counts[start] + ess) - lgamma(ess)) + (lgamma(Counts[start + 1]) - lgamma(ess)); 

	return score;
	}      

}
