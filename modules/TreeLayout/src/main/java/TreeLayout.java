package noname.nocompany.layout;

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
    private LayoutEngine engine;
    public TreeLayout(TreeLayoutBuilder bld) {
        builder = bld;
        engine=new LayoutEngine();
    }

    @Override
    public void resetPropertiesValues() {
        hspace = 200;
        vspace = 200;
    }

    @Override
    public void initAlgo() {
        executing = engine.isTree();
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        //
        engine.doLayout();
        setPosition(engine.myroot);
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
        engine.setGraphModel(gm);
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
    void setPosition(MyNode n) {
        //System.out.println(n.node.getLabel()+" "+n.x+" "+n.y);
        n.node.setX(n.x*hspace);
        n.node.setY(n.y*vspace);
        for(int i=0;i<n.childs.length;i++) {
            setPosition(n.childs[i]);
        }
    }
}

