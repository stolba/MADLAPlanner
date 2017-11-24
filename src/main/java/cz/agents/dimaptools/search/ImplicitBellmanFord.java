package cz.agents.dimaptools.search;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

/**
 * See Heuristic Search: Theory and Application, pp. 61-62
 * @author stolba
 *
 */
public class ImplicitBellmanFord implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(ImplicitBellmanFord.class);

    private final Problem problem;
    private final Set<Action> actions;
    private final LinkedList<BFSearchState> open = new LinkedList<BFSearchState>();
    private final Map<Integer,BFSearchState> closed = new HashMap<Integer,BFSearchState>();
    private BFSearchState best=null;

    private final int n; 
    private final boolean allowRepeatingStates = false;



    public ImplicitBellmanFord(Problem problem) {
        this.problem = problem;
        actions = problem.getMyActions();
        n=actions.size()*10;

    }
    
    public ImplicitBellmanFord(Problem problem,Set<Action> useActions) {
        this.problem = problem;
        actions = useActions;
        n=actions.size()*10;
       
    }



    @Override
    public void plan(ConfigurationInterface config, SearchCallback planFoundCallback) {
    	
    	System.out.println("RUNNING IMPLICIT BELLMAN FORD");

        BFSearchState init = new BFSearchState(problem.initState);
        
        init.setF(0);

        open.add(init);

        while(!open.isEmpty()){
            final BFSearchState state;

            
            state = open.poll();
                
//            System.out.println("POLL: "+state);
              
//            //TODO: we should be able to move it behind the closed-list check as any state that is in closed was already seen
              //BUT what if the state's f is improved? 
//            if(state.unifiesWith(problem.goalSuperState)){
////            	System.out.println("SOLUTION FOUND: "+state);
//            	if((best == null || state.getF() < best.getF())){
////            		System.out.println("NEW BEST SOLUTION: "+state);
//            		best = state;
//            	}
//            }
            

            if (!closed.containsKey(state.hashCode())) {
            	
            	if(state.unifiesWith(problem.goalSuperState)){
//                	System.out.println("SOLUTION FOUND: "+state);
                	if((best == null || state.getF() < best.getF())){
//                		System.out.println("NEW BEST SOLUTION: "+state);
                		best = state;
                	}
                }

                closed.put(state.hashCode(),state);

                for (Action action : actions) {
                    if (action.isApplicableIn(state)) {
                    	BFSearchState succ = state.transformBy(action);
                        
                        boolean nonneg = improve(state,succ,action);
                        
                        if(!nonneg){
                        	planFoundCallback.planNotFound();
                        	return;
                        }
                    }
                }
                
            }

        }
        
        if(best!= null && best.unifiesWith(problem.goalSuperState)){
        	System.out.println("FINISHED! BEST SOLUTION: "+best);
        	LOGGER.info("solution state f=" + best.getF());
            LOGGER.info("OPEN-SIZE[" + problem.agent + "]" + open.size());
            LOGGER.info("CLOSED-SIZE[" + problem.agent + "]" + closed.size());
        
        	planFoundCallback.planFound(best.reconstructPlan(new LinkedList<String>()));
        }else{
        	planFoundCallback.planNotFound();
        }
    }
    
    private boolean improve (BFSearchState u, BFSearchState v, Action a){
    	if(open.contains(v)){
//    		System.out.println(" -- in open");
    		if(u.getF() + a.getCost() < v.getF()){
//    			System.out.println(" --- improve");
    			v.setParent(u);
    			if(v.getPathLength()>n-1){
    				LOGGER.warn("Negative cycle!");
    				return false; //detect negative cycle, n is number of nodes
    			}
    			
    			v.setParentAction(a);
    			v.setF(u.getF()+a.getCost());
    		}
    	}else if(closed.containsKey(v.hashCode())){
//    		System.out.println(" -- in closed");
    		if(u.getF() + a.getCost() < closed.get(v.hashCode()).getF()){
//    			System.out.println(" --- improve");
    			v.setParent(u);
    			if(v.getPathLength()>n-1){
    				LOGGER.warn("Negative cycle!");
    				return false; //detect negative cycle, n is number of nodes
    			}
    			
    			v.setParentAction(a);
    			v.setF(u.getF()+a.getCost());
    			if(allowRepeatingStates)closed.remove(v.hashCode());
    			open.add(v);
    		}
    	}else{
//    		System.out.println(" -- new");
    		v.setParent(u);
    		v.setParentAction(a);
			v.setF(u.getF()+a.getCost());
			open.add(v);
    	}
    	
    	return true;
    }


    
    
    
    private class BFSearchState extends State implements Comparable<BFSearchState> {

        private float f;
        private int path=1; 
        private BFSearchState parent = null;
        private Action parentAction = null;

        /**
         * Init
         * @param state
         */
    	public BFSearchState(State state) {
    		super(state);

    		f = Float.POSITIVE_INFINITY;
    	}


       

        public BFSearchState transformBy(Action action) {
            BFSearchState result = new BFSearchState(this);
            action.transform(result);
            return result;
        }
        
        
        public int compareTo(BFSearchState o) {
        	if(f > o.getF() ){
        		return 1;
        	}else if(f < o.getF()){
        		return -1;
        	}else{
        		return 0;
        	}
        }
        
        public int getPathLength(){
        	return path;
        }
        
        public LinkedList<String> reconstructPlan(LinkedList<String> plan){
        	if(parent==null){
        		return plan;
        	}else{
        		plan.addFirst("\n"+parentAction.getOwner()+" "+parentAction.getLabel()+" "+parentAction.hashCode());
        		return parent.reconstructPlan(plan);
        	}
        }

        @Override
        public String toString() {
            return "BFSearchState [f=" + f + ",path="+path+"] -> " + super.toString();// + "\n";
        }

		public float getF() {
			return f;
		}

		public void setF(float f) {
			this.f = f;
		}

		public BFSearchState getParent() {
			return parent;
		}

		public void setParent(BFSearchState parent) {
			this.parent = parent;
			this.path = parent.path + 1;
		}
		
		public Action getParentAction() {
			return parentAction;
		}

		public void setParentAction(Action parentAction) {
			this.parentAction = parentAction;
		}

		@Override
	    public int hashCode() {
	    	return super.hashCode();
	    }

    }

}
