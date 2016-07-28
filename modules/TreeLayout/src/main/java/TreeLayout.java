package noname.nocompany.layout;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.*;
import org.gephi.layout.spi.*;

/**
 * This algorithm dipose a tree with the root in the top center and
 * the childs downward.
 * It depends on 2 parameters :
 * the horizontal and the vertical spacing
 *
 * @author Enrico Franco
 */
public class TreeLayout implements Layout {

    private final LayoutBuilder builder;
    private GraphModel graphModel;
    private boolean executing = false;
    private Node root; // The root of the tree
    private int depth; // Depth of the tree
    //Parameters
    private int hspace;
    private int vspace;

    public TreeLayout(TreeLayoutBuilder bld) {
        builder = bld;
    }

    @Override
    public void resetPropertiesValues() {
        hspace = 200;
        vspace = 200;
    }

    @Override
    public void initAlgo() {
        executing = isTree();
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        //
        root.setX(0.f);root.setY(1000.0f);
        ArrayList<Node> todo = new ArrayList<Node>();
        todo.add(root);
        float hdist=(float) hspace;
        //
        while(todo.size()>0) {
            ArrayList<Node> nexttodo = new ArrayList<Node>();
            for(Node current : todo) {
                Node[] childs = getChilds(graph,current);
                if(childs.length==0) continue;
                if(childs.length==1) {
                    childs[0].setX(current.x());
                    childs[0].setY(current.y()-((float) vspace));
                    nexttodo.add(childs[0]);       
                } else {
                    float f1 = hdist*(getChilds(graph,childs[0]).length-1);
                    float max=-1.0f;
                    for(int i=1;i<childs.length;i++) {
                        float f2 = hdist*(getChilds(graph,childs[i]).length-1);
                        if((f1+f2)/2.0f+hdist > max) max=(f1+f2)/2.0f + hdist;
                        f1 = f2;
                    }
                    float dx=max/((float) childs.length-1);
                    for(int i=0;i<childs.length;i++) {
                        childs[i].setX(current.x()-max/2.0f+dx*i);
                        childs[i].setY(current.y()-((float) vspace));
                        nexttodo.add(childs[i]);
                    }
                }
            }
            todo=nexttodo;
        }
        graph.readUnlock();
    }

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String TREELAYOUT = "Tree Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Horizontal Space",
                    TREELAYOUT,
                    "Horizontal distance between nodes",
                    "getHspace", "setHspace"));
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Vertical Space",
                    TREELAYOUT,
                    "Vertical distance between nodes",
                    "getVspace", "setVspace"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        graphModel = gm;
    }

    public int getHspace() {
        return hspace;
    }

    public void setHspace(Integer x) {
        hspace = x;
    }

    public int getVspace() {
        return vspace;
    }

    public void setVspace(Integer x) {
        vspace = x;
    }
    /**
    *  Check that the graph is a directed rooted tree
    *  and set the root node
    */
    private boolean isTree() {
        Graph graph = graphModel.getGraph();
        if( !graph.isDirected() ) {
            showError("the graph is not a directed graph");
            return false;
        }
        Node[] nodes = graph.getNodes().toArray();
        root=null;
        depth=0;
        for(int i=0;i<nodes.length;i++) {
            Node node=nodes[i];
            //Salgo su alla ricerca del root
            int l=0;
            boolean end=false;
            while(!end) {
                Node[] nodep = getParents(graph,node);
                if(nodep.length==0) {
                    end=true;
                }
                if(nodep.length==1) {
                    node=nodep[0];
                    l++;
                }
                if(nodep.length>1) {
                    showError("node Id="+node.getId()+" has "+nodep.length+" parents");
                    return false;
                }
            }
            if(root == null) root = node;
            else {
                if(root != node) {
                    showError("more than one root found: "+node.getId()+" , "+root.getId());
                    return false;
                }
            }
            if(l>depth) depth=l;
        }
        return true;
    }

    private void showError(String x) {
        String msg="Cannot layout this network:\n"+x;
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE); 
    }
    
    private Node[] getParents(Graph g,Node n) {
        ArrayList<Node> parents = new ArrayList<Node>();
        Node[] vicini = g.getNeighbors(n).toArray();
        for(int i=0;i<vicini.length;i++) {
            if( g.getEdge(vicini[i],n) != null) parents.add(vicini[i]);
        }
        return parents.toArray(new Node[0]);
    }

    private Node[] getChilds(Graph g,Node n) {
        ArrayList<Node> childs = new ArrayList<Node>();
        Node[] vicini = g.getNeighbors(n).toArray();
        for(int i=0;i<vicini.length;i++) {
            if( g.getEdge(n,vicini[i]) != null) childs.add(vicini[i]);
        }
        return childs.toArray(new Node[0]);
    }

}
