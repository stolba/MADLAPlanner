package cz.agents.dimaptools.input.addl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Representation of the Agent Domain Description Language file
 * @author komenda
 *
 */
public class ADDLObject {

    private final List<String> agentList = new LinkedList<String>();

    public ADDLObject(List<String> agentList) {
        this.agentList.addAll(agentList);
    }

    public Iterator<String> agentsIterator() {
        return agentList.iterator();
    }

    public int getAgentCount() {
        return agentList.size();
    }

    public List<String> getAgentList() {
        return agentList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : agentList) {
            sb.append(s);
            sb.append(" ");
        }
        sb.setLength(sb.length() - 1);

        return "(:agents " + sb + ")";
    }


}
