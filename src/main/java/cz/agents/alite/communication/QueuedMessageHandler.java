package cz.agents.alite.communication;

import java.util.concurrent.LinkedBlockingQueue;

public class QueuedMessageHandler implements MessageHandler {

    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
    private final Class<?> dataType;

    private final String owner;

    public QueuedMessageHandler(Class<?> dataType){
        this.dataType = dataType;
        this.owner = null;
    }

    /**
     *
     * @param dataType
     * @param owner If owner is set, all messages with sender.equals(owner), the message is filtered.
     */
    public QueuedMessageHandler(Class<?> dataType, String owner) {
		this.dataType = dataType;
		this.owner = owner;
	}

	@Override
    public void notify(Message message) {
        if(dataType.isInstance(message.getContent().getData()) && !message.getSender().equals(owner)){
//        	System.out.println("RECEIVE QueuedMessageHandler: " + message);
            synchronized (QueuedMessageHandler.this) {
                messageQueue.add(message);

//                System.out.println(owner + " QueuedMessageHandler: \n");
//                for(Message m : messageQueue){
//                	System.out.println("\t " + m.getSender() + ": " + m.getContent().getData());
//                }

                QueuedMessageHandler.this.notify();
            }
        }
    }

    public boolean hasMessage(){
        return !messageQueue.isEmpty();
    }

    public int queueSize(){
        return messageQueue.size();
    }

    public Message pullMessage(){
        return messageQueue.poll();
    }

    public void waitForMessage(){
        try {
            synchronized (this) {
                if (!messageQueue.isEmpty()) {
                    return;
                }
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
