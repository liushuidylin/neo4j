package cn.golaxy;

import org.apache.commons.compress.utils.Lists;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @Author: duyilin@golaxy.cn
 * @Date: Created in 15:55 2020/9/23
 */


public class LoopDetectionProducer {
    /**
     * 运行环境/上下文
     */
    @Context
    public GraphDatabaseService  db;


    public List<List<Integer>> createGraphList(String label){
        Label l = Label.label(label);
        ResourceIterator<Node> nodes= db.findNodes(l);
        List<Node> nodeList = Lists.newArrayList(nodes);

        List<List<Integer>> adj = new ArrayList<>(nodeList.size());
        for (int i = 0; i < nodeList.size(); i++) {
            adj.add(new LinkedList<>());
        }
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            Iterable<Relationship> relationships = node.getRelationships(Direction.OUTGOING);
            Iterator<Relationship> iterator = relationships.iterator();
            while (iterator.hasNext()){
                Relationship relationship = iterator.next();
                Node node1 = relationship.getEndNode();
                Integer index = nodeList.indexOf(node1);
                adj.get(i).add(index);
            }
        }
        return adj;
    }


    /**
     * 判断第i个节点是否有环
     * @param i
     * @param visited
     * @param recStack
     * @return
     */
    public boolean isCyclicUtil(int i, boolean[] visited, boolean[] recStack, List<List<Integer>> adj){
        if (recStack[i])
            return true;
        if (visited[i])
            return false;

        visited[i] = true;
        recStack[i] = true;
        List<Integer> children = adj.get(i);

        for (Integer c: children)
            if (isCyclicUtil(c, visited, recStack, adj))
                return true;
        recStack[i] = false;
        return false;
    }

    /**
     * 判断是否是环
     * @param label ：节点个数
     * @return
     */
    @Procedure(name = "my.isCyclic", mode = Mode.READ)
    @Description("检测是否成环")
    public Stream<CyclicResult> isCyclic(@Name("label") String label) {
        List<CyclicResult> output = new ArrayList<>();
        List<List<Integer>> adj = createGraphList(label);
        Integer length = adj.size();
        boolean[] visited = new boolean[length]; //没有被访问过
        boolean[] recStack = new boolean[length]; //不属于递归堆栈
        for (int i = 0; i < length; i++){
            if (isCyclicUtil(i, visited, recStack, adj)){
                output.add(new CyclicResult(true));
                return output.stream();
            }
        }
        output.add(new  CyclicResult(false));
        return output.stream();

//        List<CyclicResult> output = new ArrayList<>();
//        List<List<Integer>> adj = createGraphList(label);
//        List<Object> a = new ArrayList<>();
//        for (int i = 0; i < adj.size(); i++) {
//            a.add(adj.get(i));
//        }
//        output.add(new CyclicResult(a));
//        return output.stream();
    }

    public static class CyclicResult {
        public Boolean cyclic;

        public CyclicResult(Boolean cyclic) {
            this.cyclic = cyclic;
        }
    }
}
