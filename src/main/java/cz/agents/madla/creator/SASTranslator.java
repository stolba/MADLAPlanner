package cz.agents.madla.creator;

import java.io.IOException;

import org.apache.log4j.Logger;

import cz.agents.alite.creator.Creator;

public class SASTranslator implements Creator {

    private final static Logger LOGGER = Logger.getLogger(SASTranslator.class);


    private static final String TRANSLATOR = "./misc/fd/src/translate/translate.py";
    private static final String PREPROCESSOR = "./preprocess-runner";
    private static final String RESOURCES = "./";
    private static final String RENAMER = "mv output ";

    private String domainFileName;
    private String problemFileName;
    private String sasFileName;
    private String agentFileName;



    @Override
    public void init(String[] args) {

        for(int i=0;i<args.length;i++){
            System.out.println(args[i]);
        }

        sasFileName = args[4];
        domainFileName = args[1];
        problemFileName = args[2];
        agentFileName = args[3];
        

    }



    @Override
    public void create() {
        
        LOGGER.info(">>> CREATION");
        LOGGER.info(">>>   sas: " + sasFileName);
        LOGGER.info(">>>   agents: " + agentFileName);

        runTranslate();
        
        runPreprocess();
        
        runRename();
        
    }


    




    private void runTranslate(){
        try {
            String cmd = TRANSLATOR + " " + RESOURCES+domainFileName + " " + RESOURCES+problemFileName;
            LOGGER.info("RUN: " + cmd);
            Process pr = Runtime.getRuntime().exec(cmd);

            pr.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void runPreprocess() {
        try {
            String cmd = PREPROCESSOR;
            LOGGER.info("RUN: " + cmd);
            Process pr = Runtime.getRuntime().exec(cmd);
            pr.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void runRename() {
        try {
            String cmd = RENAMER + RESOURCES+sasFileName;
            LOGGER.info("RUN: " + cmd);
            Process pr = Runtime.getRuntime().exec(cmd);
            pr.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



}
