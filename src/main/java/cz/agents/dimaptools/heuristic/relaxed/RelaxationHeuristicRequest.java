package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntObjectHashMap;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;

public class RelaxationHeuristicRequest {

    private final static Logger LOGGER = Logger.getLogger(RelaxationHeuristicRequest.class);

    private static int hashCodes = 1;
    private static int MAX_EXPECTED_AGENTS = 1000;

    private final int hash;

    private final HeuristicComputedCallback callback;

    private int expectedReplies;
    private int heuristicValue = 0;

    public RelaxationHeuristicRequest(int agentID, HeuristicComputedCallback callback) {
        hash = (hashCodes++)*MAX_EXPECTED_AGENTS + agentID;
        this.callback = callback;
        this.expectedReplies = 0;
    }

    public void waitForReply(){
        ++expectedReplies;
    }

    public int waitingFor(){
        return expectedReplies;
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
        RelaxationHeuristicRequest other = (RelaxationHeuristicRequest) obj;
        if (hash != other.hash)
            return false;
        return true;
    }

    public void receiveReply(int newHeuristicValue, TIntObjectHashMap requests) {
        --expectedReplies;

//		LOGGER.info("request " + hash + " waiting for " + expectedReplies);

        heuristicValue += newHeuristicValue;

        if(expectedReplies < 0){
//			System.err.println("Request " + hash +" Yack! Received more replies than expected!");	//TODO: fix it! (ldFF)
            LOGGER.error("Request " + hash +" Yack! Received more replies than expected!");
        }

        if(expectedReplies == 0){
//			LOGGER.info("request " + hash + " completed");
            if(requests!=null)requests.remove(hash);
            
            Trace.it("heuristic:",hash,heuristicValue);
            
            callback.heuristicComputed(new HeuristicResult(heuristicValue));
        }

    }






}
