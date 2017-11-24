package cz.agents.dimaptools.lp;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SolverCPLEX extends LPSolver {
	
	private String cmd = "./cplex-runner";
	
	private final static Logger LOGGER = Logger.getLogger(SolverCPLEX.class);


	@Override
	public LPSolution solveLP(String LP) {
		writeAndSolveLP(LP);
		return readSolution();
	}
	
	private void writeAndSolveLP(String LP){
		String LPfile = "problem.clp";
		writeFile(LP, LPfile);
		
		try {
            LOGGER.info("RUN: " + cmd);
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            //builder.redirectOutput(new File("lp_solve.out"));
            Process pr = builder.start();
            

            pr.waitFor();
            LOGGER.error(pr.getErrorStream().read());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	private LPSolution readSolution(){
		
		try {	
	         File inputFile = new File("problem.sol");
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         doc.getDocumentElement().normalize();
	         
	         LPSolution lpsol = new LPSolution();
	         
	         Node header = doc.getElementsByTagName("header").item(0);
	         String ov = header.getAttributes().getNamedItem("objectiveValue").getNodeValue();
	         System.out.println("ov: " + ov);
	         lpsol.setObjectiveValue(Float.parseFloat(ov));
	         lpsol.setSolutionStatus(header.getAttributes().getNamedItem("solutionStatusString").getNodeValue());
	         
	         NodeList nodes = doc.getElementsByTagName("variable");
	         for (int i = 0; i < nodes.getLength(); i++) {
	        	 
	             Node n = nodes.item(i);
	             String var = n.getAttributes().getNamedItem("name").getNodeValue();
	 			 float val = Float.parseFloat(n.getAttributes().getNamedItem("value").getNodeValue());
	 			 lpsol.setVariableValue(var, val);
	         }
	        	 
	         return lpsol;
	         
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		
		return null;
	   
		
		
	}

	@Override
	public Solver isSolver() {
		return Solver.CPLEX;
	}

}
