//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> SpecifierQualifierList()
 * f1 -> StructDeclaratorList()
 * f2 -> ";"
 */
public class StructDeclaration implements Node {
   public SpecifierQualifierList f0;
   public StructDeclaratorList f1;
   public NodeToken f2;

   public StructDeclaration(SpecifierQualifierList n0, StructDeclaratorList n1, NodeToken n2) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
   }

   public StructDeclaration(SpecifierQualifierList n0, StructDeclaratorList n1) {
      f0 = n0;
      f1 = n1;
      f2 = new NodeToken(";");
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
