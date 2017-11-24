package cz.agents.dimaptools.heuristic.landmarks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.Proposition;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.MaxEvaluator;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class LMCutHeuristicFD extends RelaxationHeuristic{

    private final Logger LOGGER = Logger.getLogger(LMCutHeuristicFD.class);


    private Proposition artificialGoal;
    private UnaryOperator goalOperator;

    public LMCutHeuristicFD(Problem problem) {
        super(problem,new MaxEvaluator(problem));

//		LOGGER = Logger.getLogger(problem.agent + "." + LMCutHeuristic.class);

        buildGoalPropositions(problem.goalSuperState);
        buildArtificialGoal(problem.goalSuperState);
    }

    public void buildArtificialGoal(SuperState goal){
        //TODO: cleanup

        artificialGoal = new Proposition(propositions.size()+1,-1,-1);
        artificialGoal.isGoal = true;
        artificialGoal.cost = -1;
        goalOperator = new UnaryOperator(operators.size()+1, -1, problem.agent,0,false);
        goalOperator.precondition = new LinkedList<Proposition>();
        goalOperator.effect = artificialGoal;
//		artificialGoal.reachedBy = goalOperator;
        operators.add(goalOperator);


        for(int var = 0; var < domain.sizeGlobal(); ++var){
            if(goal.isSet(var)){
                Proposition p = propositions.get(var).get(goal.getValue(var));
                goalOperator.precondition.add(p);
                p.preconditionOf.add(goalOperator);
            }
        }

        goalPropositions.add(artificialGoal);

    }

    @Override
    public void getHeuristic(State state, HeuristicComputedCallback callback) {
        int totalCost = 0;

        setupExplorationQueue();
        setupExplorationQueueState(state);

        // The following two variables could be declared inside the loop
        // ("second_exploration_queue" even inside second_exploration),
        // but having them here saves reallocations and hence provides a
        // measurable speed boost.
        List<UnaryOperator> cut = new LinkedList<UnaryOperator>();
        LinkedList<Proposition> secondExplorationQueue = new LinkedList<Proposition>();
        firstExploration(state);
        printEQ(state);
        // validate_h_max();  // too expensive to use even in regular debug mode
        if (artificialGoal.cost == -1){
            callback.heuristicComputed(new HeuristicResult(LARGE_HEURISTIC,null));
            return;
        }

//	    int num_iterations = 0;
        while (artificialGoal.cost != 0) {
//            num_iterations++;
            //cout << "h_max = " << artificial_goal.h_max_cost << "..." << endl;
            //cout << "total_cost = " << total_cost << "..." << endl;
//	        mark_goal_plateau(&artificial_goal);

            //secondExploration
            findCut(state, secondExplorationQueue, cut);

            if(cut.isEmpty()){
                LOGGER.warn("Cut is empty!");
            }else{
                LOGGER.info("Cut:\n");
                LOGGER.info(cut);
                for(UnaryOperator op : cut){
                    LOGGER.info("     " + problem.getAction(op.actionHash) + "\n");
                }
            }

            int cut_cost = Integer.MAX_VALUE;
            for (UnaryOperator op : cut) {
                cut_cost = Math.min(cut_cost, op.baseCost);
            }
            for (UnaryOperator op : cut){
                op.cost -= cut_cost;
            }
            //cout << "{" << cut_cost << "}" << flush;
            totalCost += cut_cost;

            //firstExplorationIncremental
            recomputeHMax(cut);
            printEQ(state);
            // validate_h_max();  // too expensive to use even in regular debug mode
            // TODO: Need better name for all explorations; e.g. this could
            //       be "recompute_h_max"; second_exploration could be
            //       "mark_zones" or whatever.
            cut.clear();

            // TODO: Make this more efficient. For example, we can use
            //       a round-dependent counter for GOAL_ZONE and BEFORE_GOAL_ZONE,
            //       or something based on total_cost, in which case we don't
            //       need a per-round reinitialization.
//	        for (int var = 0; var < propositions.size(); var++) {
//	            for (int value = 0; value < propositions[var].size(); value++) {
//	                RelaxedProposition &prop = propositions[var][value];
//	                if (prop.status == GOAL_ZONE || prop.status == BEFORE_GOAL_ZONE)
//	                    prop.status = REACHED;
//	            }
//	        }
//	        artificial_goal.status = REACHED;
//	        artificial_precondition.status = REACHED;
        }
        //cout << "[" << total_cost << "]" << flush;
        //cout << "**************************" << endl;

        LOGGER.info(domain.agent + " LMCut: " + totalCost);

        callback.heuristicComputed(new HeuristicResult(totalCost,null));
    }



    private void firstExploration(State state) {
        while(!explorationQueue.isEmpty()){
            Proposition p = explorationQueue.poll();

            if(p.equals(artificialGoal)){
                System.out.println("!");
            }

//			if(p.cost < p.distance) continue;

//			if(artificialGoal.cost >= 0){
//				return;
//			}

            //compute cost
            evaluator.evaluateOperators(p.preconditionOf, p.cost);

            //trigger operators
            for(UnaryOperator op : p.preconditionOf){
                op.setHMaxSupporter(p, p.cost);
                --op.unsatisfied_preconditions;
                if(op.unsatisfied_preconditions == 0){
                    enqueueIfNecessary(op.effect,op.cost,op);
                }
            }
        }
    }

    private void printEQ(State state){
        String out = "--- Exploration Queue: ---";

        int cost = 0;
        boolean empty = false;

        while(!empty){
            empty = true;

            String O = "";
            for(UnaryOperator op : operators){
                if(op.cost == cost){
                    empty = false;
                    O = O + " [";
                    for(Proposition p : op.precondition) O = O + "(" + p.var + "=" + p.val + ")";
                    O = O + "-->(" + op.effect.var + "=" + op.effect.val + ")]:"+op.operatorsIndex+"("+op.cost+") " ;
                }
            }
            out += "\n" + O;

            String P = "";
            for(Map<Integer,Proposition> map : propositions.values()){
                for(Proposition p : map.values()){
                    if(p.cost == cost){
                        empty = false;
                        P = P + " [" + p.var + "=" + p.val + "]:"+p.cost + " ";
                    }
                }
            }
            if(cost == artificialGoal.cost){
                P = P + " [" + artificialGoal.var + "=" + artificialGoal.val + "]:"+artificialGoal.cost + " ";
            }
            out += "\n" + P;

            ++cost;


        }

        out += "\n --- --- ---";
        LOGGER.info(out);

    }

    private void recomputeHMax(List<UnaryOperator> cut) {

        for (UnaryOperator op : cut) {
            SuperState eff = problem.getAction(op.actionHash).getEffect();
            for(int var : eff.getSetVariableNames().toArray()){
                Proposition p = propositions.get(var).get(eff.getValue(var));
                enqueueIfNecessary(p, op.cost,p.reachedBy);
            }
        }

        while(!explorationQueue.isEmpty()){
            Proposition p = explorationQueue.poll();

//			if(p.cost < p.distance) continue;

            //trigger operators
            for(UnaryOperator op : p.preconditionOf){
                if(op.hMaxSupporter.equals(p)){
                    int old_supp_cost = op.hMaxSupporterCost;
                    if(old_supp_cost > p.cost){
                        op.updateHMaxSupporter();
                        int new_supp_cost = op.hMaxSupporterCost;
                        if (new_supp_cost != old_supp_cost) {
                            int target_cost = new_supp_cost + op.baseCost;
                            if(op.actionHash != -1){
                                SuperState eff =  problem.getAction(op.actionHash).getEffect();
                                for(int var : eff.getSetVariableNames().toArray()){
                                    Proposition e = propositions.get(var).get(eff.getValue(var));
                                    enqueueIfNecessary(e, target_cost,e.reachedBy);
                                }
                            }else{
                                enqueueIfNecessary(goalOperator.effect, target_cost,goalOperator);
                            }
                        }
                    }
                }
            }
        }
    }


    private void findCut(State state,LinkedList<Proposition> secondExplorationQueue,List<UnaryOperator> cut) {
        for(UnaryOperator op : operators){
            if(op.precondition.size() == 0){
                secondExplorationQueue.push(op.effect);
            }
        }

        for(int var = 0; var < domain.sizeGlobal(); ++var){
            if(state.isSet(var) && domain.inDomainVar(var)){
                Proposition p = propositions.get(var).get(state.getValue(var));
                secondExplorationQueue.push(p);
            }
        }

        while(!secondExplorationQueue.isEmpty()){
            Proposition p = secondExplorationQueue.pop();
            //trigger operators
            for(UnaryOperator op : p.preconditionOf){
                if(op.hMaxSupporter.equals(p)){
                    boolean reachedGoalZone = false;
                    if(goalPropositions.contains(op.effect)){
                        reachedGoalZone = true;
                        cut.add(op);
                    }
                    if (!reachedGoalZone) {
                        secondExplorationQueue.push(op.effect);
                    }
                }
            }
        }

    }


    @Override
    public void processMessages() {

    }











}
