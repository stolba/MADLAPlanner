package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntObjectHashMap;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.model.Problem;

public class RelaxedPlanRequest {

    private final static Logger LOGGER = Logger.getLogger(RelaxedPlanRequest.class);

    private static int hashCodes = 1;
    private static int MAX_EXPECTED_AGENTS = 1000;

    private final int hash;

    private final HeuristicComputedCallback callback;
    Problem problem;

    private int expectedReplies;
    private RelaxedPlan rp = null;

    public RelaxedPlanRequest(int agentID,Problem problem, HeuristicComputedCallback callback) {
        hash = (hashCodes++)*MAX_EXPECTED_AGENTS + agentID;
        this.callback = callback;
        this.problem = problem;
        this.expectedReplies = 0;
        rp = new RelaxedPlan();
    }

    public void waitForReply(){
        ++expectedReplies;
    }

    public int waitingFor(){
        return expectedReplies;
    }
    
    public RelaxedPlan getRP(){
    	return rp;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelaxedPlanRequest other = (RelaxedPlanRequest) obj;
        if (hash != other.hash)
            return false;
        return true;
    }
    
    public void receiveReply(RelaxedPlan newRP, TIntObjectHashMap requests) {
    	receiveReply(newRP.toArray(), requests);
    }

    public void receiveReply(int[] newRP, TIntObjectHashMap requests) {
        --expectedReplies;

//		LOGGER.info("request " + hash + " waiting for " + expectedReplies);

        rp.addAll(newRP);

        if(expectedReplies < 0){
//			System.err.println("Request " + hash +" Yack! Received more replies than expected!");	//TODO: fix it! (ldFF)
            LOGGER.error("Request " + hash +" Yack! Received more replies than expected!");
        }

        if(expectedReplies == 0){
//			LOGGER.info("request " + hash + " completed");
            if(requests!=null)requests.remove(hash);
            
            Trace.it("heuristic:",hash,rp.getCost());
            
            System.out.println("RELAXED PLAN" + rp.humanize(problem) + "("+rp.getCost()+")");
    		
            
            callback.heuristicComputed(new HeuristicResult(rp.getCost()));
        }

    }
    
    public void receiveReply(int[] newRP, TIntObjectHashMap requests, int privateCost) {
        --expectedReplies;

//		LOGGER.info("request " + hash + " waiting for " + expectedReplies);

        rp.addAll(newRP);

        if(expectedReplies < 0){
//			System.err.println("Request " + hash +" Yack! Received more replies than expected!");	//TODO: fix it! (ldFF)
            LOGGER.error("Request " + hash +" Yack! Received more replies than expected!");
        }

        if(expectedReplies == 0){
//			LOGGER.info("request " + hash + " completed");
            if(requests!=null)requests.remove(hash);
            
            Trace.it("heuristic:",hash,rp.getCost());
            
            System.out.println(problem.agent + ": " + rp.humanize(problem) + "("+rp.getCost()+")");
    		
            
            callback.heuristicComputed(new HeuristicResult(rp.getCost()+privateCost));
        }

    }






}
