package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.pareto;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by joza on 16.8.2016.
 */
public class ParetoCutter3 {

    private Node[][] allNodes = new Node[1000][10000];
    private long maxId = 0;

    public void addNode(Node node){
        long x = node.getId() / 10000;
        long y = node.getId() - x * 10000;
        allNodes[(int) x][(int) y] = node;
        maxId = node.getId();
    }

    public List<Node> nodesDominatedByThis(Node node) {
        List<Node> dominatedNodes = new ArrayList<>();
        for(int x=0; x<1000; x++){
            for(int y=0; y<10000; y++){
                long id = 1000 * x + y;
                if(id > maxId) return dominatedNodes;
                Node old = allNodes[x][y];
                if(old != null && node.dominates(old))
                    dominatedNodes.add(old);
            }
        }
        return dominatedNodes;
    }

    public boolean isThisNodeDominated(Node node){
        for(int x=0; x<1000; x++){
            for(int y=0; y<10000; y++){
                long id = 1000 * x + y;
                if(id > maxId) return false;
                Node old = allNodes[x][y];
                if(old != null && old.dominates(node))
                    return true;
            }
        }
        return false;
    }

    public void removeSubTree(Node root){
        Stack<Node> stack = new Stack<>();
        stack.add(root);
        while (!stack.isEmpty()){
            Node node = stack.pop();
            //synchronized (node){
                if(node.children != null) {
                    for (Node child : node.children) {
                        if(child != null)
                            stack.add(child);
                    }
                }
            //}
            removeNode(node);
        }
    }

    private void removeNode(Node node) {
        long x = node.getId() / 10000;
        long y = node.getId() - x * 10000;
        allNodes[(int) x][(int) y] = null;
    }

}
