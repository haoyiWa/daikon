package daikon.tools.jtb.cparser.customvisitor;

import daikon.tools.jtb.cparser.syntaxtree.*;
import daikon.tools.jtb.cparser.visitor.*;
import java.util.*;
import java.io.*;

public class Printer extends DepthFirstVisitor {

  private PrintWriter out;
  private StringBuffer buffer;
  private ArrayList filter;
  public static List badExpressions;
  private File file;

  static {
    ArrayList temp = new ArrayList();
    temp.add("(unsigned)");
    temp.add("(unsignedshortint)");
    temp.add("(__ctype)");
    temp.add("fgets");
    badExpressions = Collections.unmodifiableList(temp);
  }

  public Printer(String fileName) throws IOException {
    buffer = new StringBuffer();
    out = new PrintWriter(new FileOutputStream(fileName));
  }

  public void visit(NodeToken n) {
    buffer.append(n.tokenImage);
  }

  public void close() throws IOException {
    out.close();
  }

  public void println() {
    out.println();
  }

  public void print(Object o) {
    out.print(o.toString());
  }

  public boolean shouldPrint(String curr, int index) {
    //if the
    return (index >= 0 &&
            ((index+curr.length() == buffer.length()) ||
             (index >=1 && !Character.isLetterOrDigit(buffer.charAt(index-1)))
             ||
             !Character.isLetterOrDigit(buffer.charAt(index+curr.length())))
            );

  }

  public void commit() {
    boolean okToPrint = true;
    //if the expression contains any of the
    //strings that should be filtered, don't
    //print it
    for (int i = 0; i < filter.size(); i++) {
      String curr = (String)filter.get(i);
      int index = buffer.toString().indexOf(curr);
      if (shouldPrint(curr, index)) {
	okToPrint = false;
	break;
      }
    }
    if (okToPrint) {
      //replaceNulls();
      out.println(buffer);
    }
    buffer = new StringBuffer();
  }

  public void setFilter(ArrayList filter) {
    this.filter = filter;
    this.filter.addAll(badExpressions);
  }


  public void visit(LogicalANDExpression n) {
    if (n.f0 !=null) {
      n.f0.accept(this);
    }
    n.f1.accept(this);
  }

  public void visit(LogicalORExpression n) {
    if (n.f0 !=null) {
      n.f0.accept(this);
    }
    n.f1.accept(this);
  }

  public void visit(EqualityExpression n) {
    if (n.f0 != null) {
      n.f0.accept(this);
    }
    n.f1.accept(this);
  }

  public void visit(RelationalExpression n) {
    if (n.f0 != null) {
      n.f0.accept(this);
    }
    n.f1.accept(this);
  }

}
