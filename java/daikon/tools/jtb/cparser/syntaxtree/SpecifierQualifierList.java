//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> TypeSpecifier() [ SpecifierQualifierList() ]
 *       | TypeQualifier() [ SpecifierQualifierList() ]
 */
public class SpecifierQualifierList implements Node {
   public NodeChoice f0;

   public SpecifierQualifierList(NodeChoice n0) {
      f0 = n0;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
