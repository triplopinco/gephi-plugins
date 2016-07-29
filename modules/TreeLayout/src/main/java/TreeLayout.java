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
    private MyNode defaultAncestor;
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
        MyNode myroot=new MyNode(root);
        expand(graph,myroot);
        firstWalk(myroot);
        secondWalk(myroot,-myroot.prelim);
        //root.setX(0.f);root.setY(1000.0f);
        graph.readUnlock();
        executing = false;
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
    private void expand(Graph g, MyNode mn) {
       Node[] childs = getChilds(g,mn.node);
       mn.childs = new MyNode[childs.length];
       for(int i=0;i<childs.length;i++) {
           MyNode x = new MyNode(childs[i]);
           mn.childs[i]=x;
           x.parent=mn;
           if(i>0) x.leftSib=mn.childs[i-1];
           x.level=mn.level+1;
           expand(g,x);
       }
    }
    private void firstWalk(MyNode v) {
        if(v.childs.length==0) {
            v.prelim=0.0f;
            if(v.leftSib!=null) v.prelim = v.leftSib.prelim+hspace;
        } else {
            defaultAncestor = v.childs[0];
            for(int i=0;i<v.childs.length;i++) {
                MyNode w = v.childs[i];
                firstWalk(w);
                apportion(w);
            }
            executeShifts(v);
            float midpoint=0.5f*(
                   v.childs[0].prelim + v.childs[v.childs.length-1].prelim);
            if(v.leftSib!=null) {
                v.prelim = v.leftSib.prelim + hspace;
                v.mod = v.prelim - midpoint;
            } else {
                v.prelim = midpoint;
            }
        }
    }
    private void secondWalk(MyNode v,float pr) {
    }
    private void apportion(MyNode v) {
        if(v.leftSib != null) {
            MyNode vpi = v;
            MyNode vpo = v;
            MyNode vmi = v.leftSib;
            MyNode vmo = vpi.parent.childs[0];
            float spi=vpi.mod;
            float spo=vpo.mod;
            float smi=vmi.mod;
            float smo=vmo.mod;
            while(nextRight(vmi)!=null && nextLeft(vpi)!=null) {
                vmi = nextRight(vmi);
                vpi = nextLeft(vpi);
                vmo = nextLeft(vmo);
                vpo = nextRight(vpo);
                vpo.ancestor = v;
                float shift=(vmi.prelim+smi) - (vpi.prelim+spi) +  hspace;
                if(shift>0) {
                    moveSubTree(ancestor(vmi,v),v,shift);
                    spi = spi + shift;
                    spo = spo + shift;
                }
                smi = smi + vmi.mod;
                spi = spi + vpi.mod;
                smo = smo + vmo.mod;
                spo = spo + vpo.mod;
            }
            if(nextRight(vmi)!=null && nextRight(vpo)==null) {
                vpo.thread=nextRight(vmi);
                vpo.mod=vpo.mod+smi-spo;
            }
            if(nextLeft(vpi)!=null && nextLeft(vmo)==null) {
                vmo.thread=nextLeft(vpi);
                vmo.mod=vmo.mod+spi-smo;
                defaultAncestor=v;
            }
        }
    }
    private void executeShifts(MyNode v) {
    }
    private MyNode nextRight(MyNode v) {
        return null;
    }
    private MyNode nextLeft(MyNode v) {
        return null;
    }
    private void moveSubTree(MyNode wm, MyNode wp, float shift) {
    }
    private MyNode ancestor(MyNode vmi,MyNode v) {
        return null;
    }
}

