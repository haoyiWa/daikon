//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> DeclarationSpecifiers()
 * f1 -> ( Declarator() | [ AbstractDeclarator() ] )
 */
public class ParameterDeclaration implements Node {
   public DeclarationSpecifiers f0;
   public NodeChoice f1;

   public ParameterDeclaration(DeclarationSpecifiers n0, NodeChoice n1) {
      f0 = n0;
      f1 = n1;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
