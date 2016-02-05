package cz.agents.madla.creator;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.DefaultPerformerCommunicator;
import cz.agents.alite.communication.Message;
import cz.agents.alite.communication.PerformerCommunicator;
import cz.agents.alite.communication.content.error.ErrorContent;
import cz.agents.alite.communication.content.error.ErrorMessageHandler;
import cz.agents.alite.communication.zeromq.MapReceiverTable;
import cz.agents.alite.communication.zeromq.ZeroMQCommunicationChannel;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.DefaultDIMAPWorld;
import cz.agents.madla.communication.protocol.ProtobufEncoder;

public class ProtobufCreator extends SASCreator {
	
	private final static Logger LOGGER = Logger.getLogger(SASCreator.class);
	
	private int minPort = 5000 + (int)(System.currentTimeMillis()%1000);
	private int currentPort = 0;
	
	private MapReceiverTable directory = new MapReceiverTable();
	

	
	public PerformerCommunicator initProtobufCommunicator(String id){
    	DefaultPerformerCommunicator communicator = new DefaultPerformerCommunicator(id);
        
        int port = minPort + (currentPort++);
        String address = "tcp://localhost:"+port;
        
        communicator.addPerformerChannel(new ZeroMQCommunicationChannel(communicator, id, address, directory));
        directory.addEntry(id, address);
        
        communicator.addMessageHandler(new ErrorMessageHandler() {
			
			@Override
			public void handleMessage(Message message, ErrorContent content) {
				LOGGER.error("Messaging error!", content.getData());
				throw new RuntimeException(content.getData());
			}
		});
        
        return communicator;
    }
    
    public DIMAPWorldInterface initWorld(String agentName, int totalAgents){
    	
        return new DefaultDIMAPWorld(
        		agentName, 
        		initProtobufCommunicator(agentName), 
        		new ProtobufEncoder(), 
        		preprocessor.getProblemForAgent(agentName),
        		totalAgents
        		);
    }

}
