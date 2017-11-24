package cz.agents.dimaptools.heuristic.relaxed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class RelaxationHeuristic implements HeuristicInterface {

	private final Logger LOGGER;

	public final Problem problem;
	public final Domain domain;
	
	protected final EvaluatorInterface evaluator;

	protected ArrayList<UnaryOperator> operators;
	protected Map<Integer,Map<Integer,Proposition>> propositions;
	protected ArrayList<Proposition> goalPropositions;

	private Map<Integer,Integer> costFunction = null;

	protected PriorityQueue<Proposition> explorationQueue;

	protected SuperState currentState;

	protected boolean initialized = false;

	public RelaxationHeuristic(Problem problem, EvaluatorInterface evaluator) {
		this(problem,evaluator,false,null);
	}
	
	public RelaxationHeuristic(Problem problem, EvaluatorInterface evaluator, boolean sortedPublicOperatorsUpfront) {
		this(problem, evaluator, sortedPublicOperatorsUpfront,null);
	}


	public RelaxationHeuristic(Problem problem, EvaluatorInterface evaluator, boolean sortedPublicOperatorsUpfront, Map<Integer,Integer> costFunction) {
		this.problem = problem;
		domain = problem.getDomain();
		this.evaluator = evaluator;
		this.costFunction = costFunction;

		LOGGER = Logger.getLogger(problem.agent + "." + RelaxationHeuristic.class);
//		LOGGER.setLevel(Level.WARN);

		operators = new ArrayList<UnaryOperator>();
		propositions = new HashMap<Integer,Map<Integer,Proposition>>();
		goalPropositions = new ArrayList<Proposition>();

		explorationQueue = new PriorityQueue<Proposition>();

		currentState = problem.initState;

		init(sortedPublicOperatorsUpfront);
	}

	/**
	 * Initialization of the EQ data structures
	 * @param sortedPublicOperatorsUpfront
	 */
	private void init(boolean sortedPublicOperatorsUpfront){
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " init ");

		//build propositions
		int prop_id = 0;
		for(int var = 0; var < domain.sizeGlobal(); ++var){
			if(domain.inDomainVar(var)){
				Map<Integer,Proposition> vals = new HashMap<Integer,Proposition>();

				for(int val = 0; val < domain.agentValMax;++val){
					if(domain.inDomainVal(val)){
						vals.put(val, new Proposition(prop_id++,var,val));
					}
				}

				propositions.put(var, vals);
			}
		}


		if(LOGGER.isDebugEnabled())LOGGER.info(domain.agent + " built " + prop_id + " propositions");

		if(sortedPublicOperatorsUpfront){
			List<Action> sortedPublicActions = new LinkedList<Action>(problem.getPublicActions());
			Collections.sort(sortedPublicActions, new Comparator<Action>() {

				@Override
				public int compare(Action o1, Action o2) {
//					LOGGER.info(o1.getLabel()+ "("+o1.hashCode()+")" + " vs. " + o2.getLabel()+"("+o1.hashCode()+")");
					//TODO HACK! is it safe?
					return Math.abs(o2.hashCode()) - Math.abs(o1.hashCode()) ;
				}
			});

			//build public parts of actions
			for(Action a : sortedPublicActions){
				List<Proposition> pre = new LinkedList<Proposition>();
				for(int var = 0; var < domain.sizeGlobal(); ++var){
					if(a.getPrecondition().isSet(var)){
						pre.add(propositions.get(var).get(a.getPrecondition().getValue(var)));
					}
				}
				for(int var = 0; var < domain.sizeGlobal(); ++var){
					if(a.getEffect().isSet(var) && domain.isPublicVar(var) && domain.isPublicVal(a.getEffect().getValue(var))){
						buildOperator(pre, var, a);
					}
				}
			}

			//build actions
			for(Action a : problem.getAllActions()){
				List<Proposition> pre = new LinkedList<Proposition>();
				for(int var = 0; var < domain.sizeGlobal(); ++var){
					if(a.getPrecondition().isSet(var)){
						pre.add(propositions.get(var).get(a.getPrecondition().getValue(var)));
					}
				}
				for(int var = 0; var < domain.sizeGlobal(); ++var){
					if(a.getEffect().isSet(var)){
						if(domain.isPublicVar(var) && domain.isPublicVal(a.getEffect().getValue(var))){
							continue;
						}
						buildOperator(pre, var, a);
					}
				}
			}
		}else{
			//build actions
			for(Action a : problem.getAllActions()){
				List<Proposition> pre = new LinkedList<Proposition>();
				for(int var = 0; var < domain.sizeGlobal(); ++var){
					if(a.getPrecondition().isSet(var)){
						pre.add(propositions.get(var).get(a.getPrecondition().getValue(var)));
					}
				}
				for(int var = 0; var < domain.sizeGlobal(); ++var){
					if(a.getEffect().isSet(var)){
						buildOperator(pre, var, a);
					}
				}
			}
		}

		if(LOGGER.isDebugEnabled())LOGGER.info(domain.agent + " built " + operators.size() + " unary operators");

		//cross reference actions
		for(UnaryOperator op : operators){
			for(Proposition p : op.precondition){
				p.preconditionOf.add(op);
			}
		}

		 initialized = true;

	}
	
	private void buildOperator(List<Proposition> pre, int var, Action a){
		int cost = costFunction == null ? (int)a.getCost() : costFunction.get(a.hashCode());
		UnaryOperator op = new UnaryOperator(operators.size(),a.hashCode(),a.getOwner(),cost,a.isProjection() && !a.isPure());
//		LOGGER.info("HMAX new OPERATOR:  " + op);
		op.precondition = pre;
		op.effect = propositions.get(var).get(a.getEffect().getValue(var));
		operators.add(op);
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " added"+(a.isPublic()?" public":"")+" operator " + a.getLabel() + "("+var+"-"+a.getEffect().getValue(var)+"):"+op.operatorsIndex);
	}

	public void buildGoalPropositions(SuperState goal){
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " build goal propositions: " + Arrays.toString(goal.getValues()) + ":" + domain.humanize(goal.getValues()));

		for(Proposition p : goalPropositions){
			p.isGoal = false;
		}
		goalPropositions.clear();

		for(int var = 0; var < domain.sizeGlobal(); ++var){
			if(goal.isSet(var)){
				Proposition p = propositions.get(var).get(goal.getValue(var));
				p.isGoal = true;
				goalPropositions.add(p);
			}
		}

	}
	
	public void clearGoalPropositions(){
		for(Proposition p : goalPropositions){
			p.isGoal = false;
		}
		goalPropositions.clear();
	}
	
	public void addGoalPropositions(SuperState goal){
		
		for(int var = 0; var < domain.sizeGlobal(); ++var){
			if(goal.isSet(var)){
				Proposition p = propositions.get(var).get(goal.getValue(var));
				p.isGoal = true;
				if(!goalPropositions.contains(p)){
					goalPropositions.add(p);
				}
			}
		}

	}

	/**
	 * Clear EQ
	 */
	public void setupExplorationQueue(){
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " setup exploration queue ");
		//clear queue
		explorationQueue.clear();

		for(int var = 0; var < domain.sizeGlobal(); ++var){
			if(domain.inDomainVar(var)){
				for(Proposition p : propositions.get(var).values()){
						p.cost = -1;
						p.distance = -1;
						p.marked = false;
						p.markedPub = false;
				}
			}
		}

		//setup operators
		for(UnaryOperator op : operators){
			op.unsatisfied_preconditions = op.precondition.size();
			op.cost = op.baseCost;
			if(op.unsatisfied_preconditions == 0){
				enqueueIfNecessary(op.effect,op.baseCost,op);
			}
		}
	}

	/**
	 * Prepare the initital state
	 * @param state
	 */
	public void setupExplorationQueueState(SuperState state){
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " build goal propositions: " + domain.humanize(state.getValues()));

		currentState = state;

		for(int var = 0; var < domain.sizeGlobal(); ++var){
			if(state.isSet(var) && domain.inDomainVar(var)){
				Proposition p = propositions.get(var).get(state.getValue(var));
				enqueueIfNecessary(p, 0, null);
			}
		}
	}

	/**
	 * Enqueue the proposition if it was either unreached, or is reached by a cheaper way
	 * @param p
	 * @param cost
	 * @param op
	 */
	public void enqueueIfNecessary(Proposition p, int cost, UnaryOperator op){
		if(p.cost == -1 || p.cost > cost){
			p.cost = cost;
			p.distance = cost;
			p.reachedBy = op;
			explorationQueue.add(p);
		}
	}

	public void relaxedExploration(){
//		LOGGER.info(domain.agent + debugPrint());
		
//		for(Proposition p : goalPropositions){
//			LOGGER.info(domain.agent + p.var+":"+problem.getDomain().humanizeVar(p.var) + ", " + p.val+":"+problem.getDomain().humanizeVal(p.val));
//			LOGGER.info(domain.agent + "... reached by " + (p.reachedBy==null?"null":problem.getAction(p.reachedBy.actionHash)));
//		}

		int unsolvedGoals = goalPropositions.size();
		while(!explorationQueue.isEmpty()){
			Proposition p = explorationQueue.poll();
//			LOGGER.info(domain.agent + " POLL " + p);

			if(p.cost < p.distance) continue;

			//if p is goal decrease the counter
			if(p.isGoal && --unsolvedGoals <= 0){
				//if counter is 0, check if all goals reached (some goal may have been counted more than once)
				boolean all_goals = true;
				for(Proposition g : goalPropositions){
					if(g.cost == -1){
						all_goals = false;
					}
				}
				if(all_goals)return;	//if all goals reached, finish
			}

			//increase cost of operator
			evaluator.evaluateOperators(p.preconditionOf, p.cost);

			//trigger operators
			for(UnaryOperator op : p.preconditionOf){
				--op.unsatisfied_preconditions;
				if(op.unsatisfied_preconditions == 0){
					enqueueIfNecessary(op.effect,op.cost,op);
				}
			}
		}

//		LOGGER.warn(domain.agent + "exploration queue empty!");

	}

	
	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " get heuristic: " + domain.humanize(state.getValues()));

		buildGoalPropositions(problem.goalSuperState);
		setupExplorationQueue();
		setupExplorationQueueState(state);
		relaxedExploration();

		if(callback != null){
			callback.heuristicComputed(new HeuristicResult(evaluator.getTotalCost(goalPropositions),evaluator.getHelpfulActions(state)));
		}
	}


	@Override
	public void processMessages() {
		
	}
	
	public String debugPrint(){
		String debug = "----------\n";
		
		debug += "currentState:" + currentState + "\n";
		debug += "explorationQueue:" + explorationQueue + "\n";
		
		debug += "propositions("+propositions.size()+"):\n";
		for(Map<Integer,Proposition> map : propositions.values()){
			for(Proposition p : map.values()){
				debug += p.toString() + ", " + domain.humanizeVar(p.var) + "-" + domain.humanizeVal(p.val) + ", goal:" + p.isGoal + ", cost:" + p.cost + "\n";
				if(p.reachedBy==null){
					debug += " reached by:null\n";
				}else{
					debug += " reached by: op:" + p.reachedBy + ", action" + problem.getAction(p.reachedBy.actionHash) + "\n";
				}
				debug += " precondition of:\n";
				for(UnaryOperator op : p.preconditionOf){
					debug += "   op:"+op.operatorsIndex + ", action:"+problem.getAction(op.actionHash);
				}
			}
		}
		
		debug += "\noperators("+operators.size()+"):\n";
		for(UnaryOperator op : operators){
			debug += op.toString() +"\n";
			debug += " action:"+problem.getAction(op.actionHash);
		}
		
		return debug;
	}

	public ArrayList<UnaryOperator> getOperators() {
		return operators;
	}

	public Map<Integer, Map<Integer, Proposition>> getPropositions() {
		return propositions;
	}

	public ArrayList<Proposition> getGoalPropositions() {
		return goalPropositions;
	}

}
