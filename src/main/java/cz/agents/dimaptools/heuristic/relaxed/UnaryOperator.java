package cz.agents.dimaptools.heuristic.relaxed;

import java.util.List;

public class UnaryOperator {

    public final int operatorsIndex;
    public final int actionHash;
    public final int baseCost;

    public int unsatisfied_preconditions;
    public int cost = -1;

    String agent;
    
    public final boolean shouldRequest;

    public List<Proposition> precondition;
    public Proposition effect;

    public int hMaxSupporterCost;
    public Proposition hMaxSupporter;


    public UnaryOperator(int operatorNo, int actionHash, String agent, int baseCost, boolean shouldRequest) {
        super();
        this.operatorsIndex = operatorNo;
        this.actionHash = actionHash;
        this.agent = agent;
        this.baseCost = baseCost;
        this.shouldRequest = shouldRequest;
    }

    public UnaryOperator(int operatorNo, int actionHash, String agent, boolean shouldRequest) {
        this(operatorNo, actionHash, agent,1,shouldRequest);
    }

    public void setHMaxSupporter(Proposition supporter, int cost){
        hMaxSupporter = supporter;
        hMaxSupporterCost = cost;
    }

    public void updateHMaxSupporter() {
        for (Proposition p : precondition){
            if (p.cost > hMaxSupporter.cost){
                hMaxSupporter = p;
            }
        }
        hMaxSupporterCost = hMaxSupporter.cost;
    }


    @Override
    public String toString() {
//        return "UnaryOperator [operatorNo=" + operatorsIndex +",hash=" + actionHash
//                + ", unsatisfied_preconditions=" + unsatisfied_preconditions
//                + ", cost=" + cost + ", agent=" + agent + ", precondition="
//                + precondition + ", effect=" + effect + "]";
        
        return "UnaryOperator [hash=" + actionHash
                + ", baseCost=" + baseCost + ", agent=" + agent + "]";
    }



}
