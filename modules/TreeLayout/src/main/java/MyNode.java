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
    float change;
    float shift;
    float x,y;
    int level;
    int number;
    //
    public MyNode(Node n) {
      node = n;
      parent=null;
      leftSib=null;
      thread=null;
      ancestor=this;
      mod = 0.0f;
      prelim=0.0f;
      change=0.0f;
      shift=0.0f;
      x=0.0f;
      y=0.0f;
      level=0;
      number=0;
    }
    public int getNumber() {
        if(number==0) {
          MyNode[] b=parent.childs;
          for(int i=0;i<b.length;i++) b[i].number=i+1;
        }
        return number;
    }
}

