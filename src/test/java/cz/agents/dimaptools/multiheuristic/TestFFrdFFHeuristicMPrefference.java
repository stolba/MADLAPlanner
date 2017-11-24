package cz.agents.dimaptools.multiheuristic;

import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationReplyHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.AddEvaluator;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;
import cz.agents.dimaptools.search.HeuristicOpenList;
import cz.agents.dimaptools.search.MultiheuristicDistributedBestFirstSearch;
import cz.agents.dimaptools.search.MultiheuristicDistributedBestFirstSearch.OpenSelectionStrategy;

public class TestFFrdFFHeuristicMPrefference extends AbstractDistributedAStarTest {

    @Test
    public void test() {
//		testProblem("truck-crane-a2");
//		testProblem("logistics-a2");
//		testProblem("logistics-a4");
//		testProblem("deconfliction-a4");
//		testProblem("rovers-a4");
//		testProblem("sokoban-a1");
//		testProblem("sokoban-a2");
    }

    @Override
    public void runSearch(DIMAPWorldInterface world){
        MultiheuristicDistributedBestFirstSearch search = new MultiheuristicDistributedBestFirstSearch(world);

        HeuristicOpenList hFF = new HeuristicOpenList("hFF",true,new RelaxationHeuristic(world.getProblem(), new FFEvaluator(world.getProblem())),false);

        new HeuristicOpenList("hAdd",false,new RelaxationHeuristic(world.getProblem(),new AddEvaluator(world.getProblem(),false)),false);
//		HeuristicOpenList hAdd = new HeuristicOpenList("hAdd",false,new RelaxationHeuristic(world.getProblem(),new AddEvaluator(world.getProblem(),false)));

        RecursiveDistributedRelaxationRequestHeuristic req = new RecursiveDistributedRelaxationRequestHeuristic(world, new FFEvaluator(world.getProblem()));
        RecursiveDistributedRelaxationReplyHeuristic rep = new RecursiveDistributedRelaxationReplyHeuristic(world, new FFEvaluator(world.getProblem()),req.getRequestProtocol());
        req.setReplyProtocol(rep.getReplyProtocol());

        HeuristicOpenList hrdFF = new HeuristicOpenList("hrdFF",true,req,rep,false);

//		search.plan(new MapConfiguration("hFF",hFF,"hAdd",hAdd), searchCallback);

//		search.plan(new MapConfiguration("hFF",hFF), searchCallback);
//		search.plan(new MapConfiguration("hrdFF",hrdFF), searchCallback);
        search.plan(new MapConfiguration("hFF",hFF,"hrdFF",hrdFF,"openSelectionStrategy", OpenSelectionStrategy.PREFFERENCE.name()), searchCallback);
    }

}
