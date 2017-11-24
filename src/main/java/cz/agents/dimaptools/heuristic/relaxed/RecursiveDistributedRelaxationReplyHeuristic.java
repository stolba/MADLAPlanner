package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntHashSet;

import java.util.Arrays;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.DistributedReplyHeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.State;

public class RecursiveDistributedRelaxationReplyHeuristic extends RelaxationHeuristic implements DistributedReplyHeuristicInterface {

    private final Logger LOGGER = Logger.getLogger(RecursiveDistributedRelaxationReplyHeuristic.class);

    private final String id = "REP";
    private DistributedHeuristicRequestProtocol requestProtocol = null;
    private final DistributedHeuristicReplyProtocol replyProtocol;


    public RecursiveDistributedRelaxationReplyHeuristic(DIMAPWorldInterface world, EvaluatorInterface evaluator) {
        super(world.getProblem(),evaluator,true);

        replyProtocol = new DistributedHeuristicReplyProtocol(
                world.getCommunicator(),
                world.getAgentName(),
                world.getEncoder()){

            @Override
            public void receiveHeuristicRequestMessage(HeuristicRequestMessage req, String sender) {
                if(LOGGER.isDebugEnabled())LOGGER.debug(replyProtocol.getAddress() + "("+id+")" + " handle request from " + sender + ": " + req.humanize(problem.getDomain()));

                processRequest(req,sender);
            }

        };

    }

    public RecursiveDistributedRelaxationReplyHeuristic(DIMAPWorldInterface world,EvaluatorInterface evaluator,DistributedHeuristicRequestProtocol requestProtocol) {
        this(world,evaluator);
        this.requestProtocol = requestProtocol;
    }

    /* (non-Javadoc)
	 * @see cz.agents.dimaptools.heuristic.relaxed.DistributedReplyHeuristicInterface#getReplyProtocol()
	 */
    @Override
	public DistributedHeuristicReplyProtocol getReplyProtocol(){
        return replyProtocol;
    }

    /* (non-Javadoc)
	 * @see cz.agents.dimaptools.heuristic.relaxed.DistributedReplyHeuristicInterface#setRequestProtocol(cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol)
	 */
    @Override
	public void setRequestProtocol(DistributedHeuristicRequestProtocol requestProtocol){
        this.requestProtocol = requestProtocol;
    }


    @Override
    public void getHeuristic(State state, HeuristicComputedCallback callback) {
        //XXX: exception?
        LOGGER.warn("This method should not be called on the Reply heuristic");
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

                if(problem.getAction(op.actionHash).isProjection()){
                    publicRP.add(op.operatorsIndex);
                }

            }
        }

    }


    /**
     * This method used to process messages, but now is used only to periodically check ithere are any local requests to process
     */
    @Override
    public void processMessages() {

    }


    /**
     * Builds an EQ, finds used public actions and returns reply
     * @param req
     * @param agent
     */
    // XXX: this resembles RelaxationHeuristics.getHeuristics + some of the sub-calls, it might be clearer if appropriately reused
    public void processRequest(HeuristicRequestMessage req,String agent){

        if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " process request: " + req);
        Trace.it("receive", "'" + domain.agent, "'" + agent, id, req.getRequestHash(), req.getRecursionDepth(), req.getState(domain).hashCode());
//		LOGGER.info(domain.agent + "("+id+")" + " receiveRequest("+req.getRequestHash() +") for op-"+Arrays.toString(req.getRequestedValues()));




        int totalCost = 0;

        //setup goal
        for(Proposition p : goalPropositions){
            p.isGoal = false;
        }
        goalPropositions.clear();
        for(int op : req.getRequestedValues()){
            for(Proposition p : operators.get(op).precondition){
                p.isGoal = true;
                goalPropositions.add(p);
            }

            totalCost += operators.get(op).baseCost;
        }

        //relaxed exploration
        setupExplorationQueue();
        setupExplorationQueueState(req.getState(domain));
        relaxedExploration();

        //evaluate cost
        totalCost += evaluator.getTotalCost(goalPropositions);

        if(LOGGER.isDebugEnabled() && totalCost >= HeuristicInterface.LARGE_HEURISTIC){
            LOGGER.warn(domain.agent + "("+id+")" + " ABOUT TO SEND LH, requested:"+problem.getAction(operators.get(req.getRequestedValues()[0]).actionHash));
//			for(Proposition p : goalPropositions){
//				if(p.cost==-1){
//					LOGGER.warn(domain.agent + "("+id+")" + "     unreachable:" + p + ", var:"+domain.humanizeVar(p.var)+",var:" + domain.humanizeVal(p.val) + ", in current state:"+(currentState.isSet(p.var)?domain.humanizeVal(currentState.getValue(p.var)):"UNDEF"));
//				}
//			}
//			LOGGER.warn(domain.agent + "("+id+")" + "     from state:" + currentState);
//			LOGGER.info(debugPrint());
//			processRequest(req,agent);
        }

        //find used public actions
        TIntHashSet publicRP = new TIntHashSet();
        for(Proposition g : goalPropositions){
            //TODO should reuse if computing RP for HA
            markPublic(g,publicRP);
        }

        int[] usedPublicActionIDs = new int[publicRP.size()];
        int i = 0;
        for( Integer op : publicRP.toArray()){
            usedPublicActionIDs[i++] = op;
        }

        //send reply
        HeuristicReplyWithPublicActionsMessage re = new HeuristicReplyWithPublicActionsMessage(req.getRequestHash(), Arrays.hashCode(req.getStateValues()), 0, totalCost, usedPublicActionIDs,req.getRecursionDepth());

        if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " send reply: " + re);



//		LOGGER.info(domain.agent + "("+id+")" + " sendReply("+re.getRequestHash() +")");

        if(requestProtocol!=null && agent.equals(domain.agent)){
            //TODO: this is a bug, should be called on the other instance's protocol
            //XXX: this todo seems obsolete
            requestProtocol.receiveHeuristicReplyWithPublicActionsMessage(re, domain.agent);
        }else{
            DataAccumulator.getAccumulator().heuristicReplyMessages ++;
            DataAccumulator.getAccumulator().totalBytes += re.getBytes();

            replyProtocol.sendHeuristicReplyWithPublicActionsMessage(re,agent);
        }
    }




}
