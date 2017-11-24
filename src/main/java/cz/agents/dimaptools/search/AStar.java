package cz.agents.dimaptools.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

/**
 * Reference implementation of A* Search
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class AStar implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(AStar.class);

    private final Problem problem;
    private final Set<Action> actions;
    private final PriorityQueue<SearchState> open = new PriorityQueue<SearchState>();
//    private final LinkedList<SearchState> open = new LinkedList<SearchState>();
    private final HashMap<Integer,SearchState> closed = new HashMap<Integer,SearchState>();

    private HeuristicInterface heuristic;
    
    private float maxG = 0;
    private float minH = Integer.MAX_VALUE;
    
    private float optSol = Integer.MAX_VALUE;
    private boolean endWithFirstSolution = true;



    public AStar(Problem problem) {
        this.problem = problem;
        actions = problem.getMyActions();

        heuristic = new HeuristicInterface() {

            @Override
            public void getHeuristic(State state, HeuristicComputedCallback callback) {
                callback.heuristicComputed(new HeuristicResult(0));
            }

            @Override
            public void processMessages() {
            }
        };

//		heuristic = new FFHeuristic(problem);
    }
    
    public AStar(Problem problem,Set<Action> useActions) {
        this.problem = problem;
        actions = useActions;

        heuristic = new HeuristicInterface() {

            @Override
            public void getHeuristic(State state, HeuristicComputedCallback callback) {
                callback.heuristicComputed(new HeuristicResult(0));
            }

            @Override
            public void processMessages() {
            }
        };

//		heuristic = new FFHeuristic(problem);
    }



    @Override
    public void plan(ConfigurationInterface config, SearchCallback planFoundCallback) {

        if(config.containsKey("heuristic")){
            heuristic = (HeuristicInterface) config.getObject("heuristic");
        }
        
        endWithFirstSolution = config.getBoolean("endWithFirstSolution", true);
        

        open.add(new SearchState(problem.initState));

        do{
            final SearchState state;

//            	System.out.println("OPEN: "+open);
            
            	if(open.isEmpty())return;
            
                state = open.poll();
                
                if(state.getGF()>optSol)return;
                
                closed.put(state.hashCode(),state);
                
//                System.out.println("POLL: "+state);
//                System.out.println("OPEN: "+open.size()+" : "+maxG +" : " + minH);
                
                
	            if(state.getHeuristicF() < minH){
	              	minH = state.getHeuristicF();
	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal [" + state.getParentActionOwner() + "] /h/: " + minH);
	            }
	            if(state.getGF() > maxG){
	               	maxG = state.getGF();
	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
	            }
	            
	            
	            List<SearchState> expandedStates = expand(state);
                for(final SearchState s : expandedStates){
                	
                	if(closed.containsKey(s.hashCode())){
                		continue;
                	}
                	
                	if (solutionFound(s)) {
                		if(s.getGF()>optSol)return;
                		optSol = s.getGF();
                      if (s.wasExpandedByMe(problem.agent)) {
                          List<String> plan = new LinkedList<String>();
                          s.reconstructPlan(plan);
                          planFoundCallback.planFound(plan);
                          if(endWithFirstSolution)return;
                      } else {
                          planFoundCallback.planNotFound();
                          return;
                      }
                  }
                	
                	heuristic.getHeuristic(s, new HeuristicComputedCallback(){

                        @Override
                        public void heuristicComputed(HeuristicResult result) {
                            s.setHeuristics(result.getValueF());
                            open.add(s);
                            
                        }
                    });
                }
                
            
             

        }while(true);
    }


    private List<SearchState> expand(SearchState state) {

//        LOGGER.info("expanding state with h=" + state.getHeuristic() + ", g+h=" + (state.getG()+state.getHeuristic()));

        List<SearchState> result = new LinkedList<SearchState>();

        for (Action action : actions) {
//        	LOGGER.info(action.getLabel() + " applicable? " + (action.isApplicableIn(state) ? "YES" : "NO"));
            if (action.isApplicableIn(state)) {
                result.add(state.transformBy(action));
            }
        }

        return result;
    }

    private boolean solutionFound(SearchState state) {
        if (state.unifiesWith(problem.goalSuperState)) {
//        	System.out.println("SOLUTION: " + state);
        	LOGGER.info("solution state g=" + state.getGF());
            LOGGER.info("OPEN-SIZE[" + problem.agent + "]" + open.size());
            LOGGER.info("CLOSED-SIZE[" + problem.agent + "]" + closed.size());

            return true;
        }

        return false;
    }

}
