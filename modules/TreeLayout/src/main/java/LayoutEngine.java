package noname.nocompany.layout;

import org.gephi.graph.api.*;
import org.gephi.layout.spi.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class LayoutEngine {
    final static float TOP=1000.0f;
    private GraphModel graphModel;
    public MyNode myroot; // The root of the tree
    private int depth; // Depth of the tree
    float distance=1.0f;
    //
    public LayoutEngine() {
    }
    //
    public void setGraphModel(GraphModel gm) {
        graphModel = gm;
    }
    //
    public void doLayout() {
        Graph graph = graphModel.getGraph();
        expand(graph,myroot);
        firstWalk(myroot);
        secondWalk(myroot,-myroot.prelim);
    }
    /**
    *  Check that the graph is a directed rooted tree.
    *  and set the root node
    */
    public boolean isTree() {
        Graph graph = graphModel.getGraph();
        if( !graph.isDirected() ) {
            showError("the graph is not a directed graph");
            return false;
        }
        Node[] nodes = graph.getNodes().toArray();
        Node root=null;
        depth=0;
        for(int i=0;i<nodes.length;i++) {
            Node node=nodes[i];
            //Salgo su alla ricerca del root
            int l=0;
            boolean end=false;
            while(!end) {
                Node[] nodep = getParents(graph,node);
                if(nodep.length>1) {
                    showError("node Id="+node.getId()+" has "+nodep.length+" parents");
                    return false;
                }
                if(nodep.length==1) {
                    node=nodep[0];
                    l++;
                }
                if(nodep.length==0) {
                    end=true;
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
        myroot=new MyNode(root);
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
            if(v.leftSib!=null) v.prelim = v.leftSib.prelim+distance;
        } else {
            MyNode defaultAncestor = v.childs[0];
            for(int i=0;i<v.childs.length;i++) {
                MyNode w = v.childs[i];
                firstWalk(w);
                defaultAncestor=apportion(w,defaultAncestor);
            }
            executeShifts(v);
            float midpoint=0.5f*(
                   v.childs[0].prelim + v.childs[v.childs.length-1].prelim);
            if(v.leftSib!=null) {
                v.prelim = v.leftSib.prelim + distance;
                v.mod = v.prelim - midpoint;
            } else {
                v.prelim = midpoint;
            }
        }
    }
	  private MyNode ancestor(MyNode vmi, MyNode v, MyNode defaultAncestor) {
        return (vmi.ancestor.parent==v.parent) ? vmi.ancestor : defaultAncestor;
	}
    private void secondWalk(MyNode v,float m) {
        v.x=v.prelim+m;
        v.y=(float) -v.level;
        for(int i=0;i<v.childs.length;i++) {
            secondWalk(v.childs[i],m+v.mod);
        }
    }
    private MyNode apportion(MyNode v, MyNode defaultAncestor) {
        if(v.leftSib == null) {
            return defaultAncestor;
        } else {
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
                float shift=(vmi.prelim+smi) - (vpi.prelim+spi) +  distance;
                if(shift>0) {
                    moveSubTree(ancestor(vmi,v,defaultAncestor),v,shift);
                    spi += shift;
                    spo += shift;
                }
                smi += vmi.mod;
                spi += vpi.mod;
                smo += vmo.mod;
                spo += vpo.mod;
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
            return defaultAncestor;
        }
    }
    private void executeShifts(MyNode v) {
        float shift=0.0f;
        float change=0.0f;
        for(int i=v.childs.length-1;i>=0;i--) {
            MyNode w=v.childs[i];
            w.prelim += shift;
            w.mod += shift;
            change += w.change;
            shift += w.shift + change;
        }

    }
    private MyNode nextRight(MyNode v) {
        if(v.childs.length>0) return v.childs[v.childs.length-1];
        return v.thread;
    }
    private MyNode nextLeft(MyNode v) {
        if(v.childs.length>0) return v.childs[0];
        return v.thread;
    }
    private void moveSubTree(MyNode wm, MyNode wp, float shift) {
		    int subtrees=wp.getNumber()-wm.getNumber();
		    wp.change -= shift/subtrees;
		    wp.shift += shift;
		    wm.change += shift/subtrees;
		    wp.prelim += shift;
		    wp.mod += shift;
    }
}
