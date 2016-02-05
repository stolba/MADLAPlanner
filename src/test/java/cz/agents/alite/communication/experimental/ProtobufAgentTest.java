package cz.agents.alite.communication.experimental;



public class ProtobufAgentTest {



//	@Test
//	public void test() throws InterruptedException {
//
//		final MapReceiverTable directory = new MapReceiverTable();
//		String[] agentNames = {"5671","5672"};
//
//		for(String agent : agentNames){
//			directory.put(agent, "tcp://localhost:"+agent);
//		}
//
//		final List<ProtobufAgent> agents = new LinkedList<ProtobufAgent>();
//
//		for(final String agent : agentNames){
//
//			System.out.println("CREATE " + agent);
//			String adr = "tcp://localhost:"+agent;
//
//			ProtobufAgent a = new ProtobufAgent(agent,adr,directory);
//			agents.add(a);
////			directory.put(agent, adr);
//
////			Thread.sleep(100);
//		}
//
////		Thread.sleep(100);
//
//		for(final ProtobufAgent agent : agents){
//
//			new Thread(new Runnable(){
//
//				@Override
//				public void run() {
//					agent.waitRandom(0, 100);
//					System.out.println("RUN " + agent.id);
//					agent.initConversation();
//				}
//
//			}).start();
//
//
//		}
//
//		// Run for 5 seconds then quit
//        Thread.sleep(5000);
//        System.out.println("DONE!\n");
//
//        //TODO: die nicely!
//
//        for(ProtobufAgent a : agents){
//        	a.close();
//        	System.out.println(a.id + " received " + a.received + " msgs");
//        	assertTrue(a.received > 0);
//		}
//        Thread.sleep(500);
//	}

}
