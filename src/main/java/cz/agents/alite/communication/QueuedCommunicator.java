package cz.agents.alite.communication;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QueuedCommunicator extends DefaultCommunicator implements PerformerCommunicator {

	private final List<MessageHandler> userMessageHandlers = new CopyOnWriteArrayList<MessageHandler>();
	private final List<QueuedMessageHandler> qMessageHandlers = new CopyOnWriteArrayList<QueuedMessageHandler>();

	public QueuedCommunicator(String address) {
		super(address);

	}
	
	public void handleMessageClass(Class<?> cls){
		QueuedMessageHandler qmh = new QueuedMessageHandler(cls);
		super.addMessageHandler(qmh);
		qMessageHandlers.add(qmh);
	}
	
	@Override
    public void addMessageHandler(MessageHandler handler) {
        userMessageHandlers.add(handler);
    }


    @Override
    public void removeMessageHandler(MessageHandler handler) {
    	userMessageHandlers.remove(handler);
    }

	@Override
	public boolean performReceiveNonblock() {
		boolean received = false;
		
		for(QueuedMessageHandler qmh : qMessageHandlers){
			if(qmh.hasMessage()){
				received = true;
				Message msg = qmh.pullMessage();
				for (MessageHandler messageHandler : userMessageHandlers) {
		            messageHandler.notify(msg);
		        }
			}
		}
		
		return received;
	}
	
	@Override
	public void performReceiveBlock(long timeoutMs) {
		for(QueuedMessageHandler qmh : qMessageHandlers){
			boolean received = false;
			
			while(!received){
				if(qmh.hasMessage()){
					received = true;
					Message msg = qmh.pullMessage();
					for (MessageHandler messageHandler : userMessageHandlers) {
			            messageHandler.notify(msg);
			        }
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void performClose() {
		// not used
	}

	

}
