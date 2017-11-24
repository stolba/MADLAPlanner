package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntHashSet;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;

public class RecursiveDistributedRelaxationRestrictedReplyHeuristic extends RecursiveDistributedRelaxationReplyHeuristic {

//	private final Logger LOGGER = Logger.getLogger(RecursiveDistributedRelaxationRestrictedReplyHeuristic.class);


	
    public RecursiveDistributedRelaxationRestrictedReplyHeuristic(DIMAPWorldInterface world, EvaluatorInterface evaluator,DistributedHeuristicRequestProtocol requestProtocol) {
		super(world, evaluator, requestProtocol);
	}



    /**
     * Determine which projected actions were used to achieve a proposition (i.e. public RP)
     * @param goal
     * @param publicRP
     */
    public void markPublic(Proposition goal, TIntHashSet publicRP){
        if(!goal.markedPub){
            goal.markedPub = true;
            UnaryOperator op = goal.reachedBy;
            if(op!=null){
                for(Proposition p : op.precondition){
                    markPublic(p,publicRP);
                }

                if(op.cost==op.baseCost && problem.getAction(op.actionHash).isProjection()){
                    publicRP.add(op.operatorsIndex);
                }

            }
        }

    }




}
