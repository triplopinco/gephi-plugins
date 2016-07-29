package noname.nocompany.layout;

import org.gephi.graph.api.Node;

public class MyNode {
    Node node; // this node
    MyNode parent;
    MyNode leftSib;
    MyNode thread;
    MyNode[] childs;
    MyNode ancestor;
    float mod;
    float prelim;
    int level;
    //
    public MyNode(Node n) {
      node = n;
      parent=null;
      leftSib=null;
      thread=null;
      ancestor=this;
      mod = 0.0f;
      prelim=0.0f;
      level=0;
    }
}

