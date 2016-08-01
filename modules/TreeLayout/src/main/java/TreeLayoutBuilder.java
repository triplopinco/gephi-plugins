package noname.nocompany.layout;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Layout builder for the Tree layout.
 * 
 * @author Enrico Franco
 */
@ServiceProvider(service = LayoutBuilder.class)
public class TreeLayoutBuilder implements LayoutBuilder {

    @Override
    public String getName() {
        return "Tree Layout";
    }

    @Override
    public LayoutUI getUI() {
        return new LayoutUI() {

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public JPanel getSimplePanel(Layout layout) {
                return null;
            }

            @Override
            public int getQualityRank() {
                return -1;
            }

            @Override
            public int getSpeedRank() {
                return -1;
            }
        };
    }

    @Override
    public Layout buildLayout() {
        return new TreeLayout(this);
    }
}
