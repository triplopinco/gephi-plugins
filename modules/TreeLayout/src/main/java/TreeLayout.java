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
    //Parameters
    private int hspace;
    private int vspace;

    public TreeLayout(TreeLayoutBuilder bld) {
        builder = bld;
    }

    @Override
    public void resetPropertiesValues() {
        hspace = 100;
        vspace = 100;
    }

    @Override
    public void initAlgo() {
        executing = isTree();
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        int nodeCount = graph.getNodeCount();
        Node[] nodes = graph.getNodes().toArray();


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

    private boolean isTree() {
        Graph g = graphModel.getGraph();
        if( !g.isDirected() ) {
          showError("the graph is not a directed graph");
          return false;
        }
        return true;
    }

    private void showError(String x) {
        String msg="Cannot layout this network:\n"+x;
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE); 
    }
}
