package daikon.inv.binary.twoSequence;

import daikon.*;
import daikon.inv.Invariant;
import daikon.inv.binary.twoScalar.*;
import utilMDE.Assert;
import java.util.Iterator;
import org.apache.log4j.Category;

// Requires that the lengths are the same.  Determines a comparison that
// holds for all (a[i], b[i]) pairs.


public class PairwiseIntComparison
  extends TwoSequence
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  /**
   * Debug tracer
   **/
  public static final Category debug =
    Category.getInstance ("daikon.inv.binary.twoSequence.PairwiseIntComparison");

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff PairwiseIntComparison invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  final static boolean debugPairwiseIntComparison = false;

  public IntComparisonCore core;

  protected PairwiseIntComparison(PptSlice ppt) {
    super(ppt);
    core = new IntComparisonCore(this);
  }

  protected PairwiseIntComparison(PptSlice ppt, boolean only_eq) {
    super(ppt);
    core = new IntComparisonCore(this, only_eq);
  }

  public static PairwiseIntComparison instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;

    VarInfo var1 = ppt.var_infos[0];
    VarInfo var2 = ppt.var_infos[1];

    if ((SubSequence.isObviousDerived(var1, var2))
        || (SubSequence.isObviousDerived(var2, var1))) {
      Global.implied_noninstantiated_invariants++;
      return null;
    }

    boolean only_eq = false;
    if (! (var1.type.elementIsIntegral() && var2.type.elementIsIntegral())) {
      only_eq = true;
    }

    PairwiseIntComparison result = new PairwiseIntComparison(ppt, only_eq);
    // Don't instantiate if the variables can't have order
    if (!result.var1().aux.getFlag(VarInfoAux.HAS_ORDER) ||
	!result.var2().aux.getFlag(VarInfoAux.HAS_ORDER)) {
      if (debug.isDebugEnabled()) {
	debug.debug ("Not instantitating for because order has no meaning: " +
		     result.var1().name + " and " + result.var2().name);
      }
      return null;
    }
    return result;

  }

  protected Object clone() {
    PairwiseIntComparison result = (PairwiseIntComparison) super.clone();
    result.core = (IntComparisonCore) core.clone();
    result.core.wrapper = result;
    return result;
  }

  protected Invariant resurrect_done_swapped() {
    core.swap();
    return this;
  }

  public String repr() {
    return "PairwiseIntComparison" + varNames() + ": "
      + core.repr();
  }

  public String format_using(OutputFormat format) {
    if (format == OutputFormat.DAIKON) return format_daikon();
    if (format == OutputFormat.IOA) return format_ioa();
    if (format == OutputFormat.ESCJAVA) return format_esc();
    if (format == OutputFormat.SIMPLIFY) return format_simplify();
    if (format == OutputFormat.JML) return format_jml();

    return format_unimplemented(format);
  }

  public String format_daikon() {
    String comparator = core.format_comparator();
    return var1().name.name() + " " + comparator + " " + var2().name.name()
      + " (elementwise)";
  }

  /* IOA */
  public String format_ioa() {
    if (var1().isIOASet() || var2().isIOASet())
      return "Not valid for sets: " + format();
    String comparator = core.format_comparator_ioa();
    VarInfoName.QuantHelper.IOAQuantification quant1 = new VarInfoName.QuantHelper.IOAQuantification(var1());
    VarInfoName.QuantHelper.IOAQuantification quant2 = new VarInfoName.QuantHelper.IOAQuantification(var2());

    return quant1.getQuantifierExp() + quant1.getVarIndexed(0) + " " +
      comparator + " " + quant2.getVarIndexed(0) + quant1.getClosingExp();
  }

  public String format_esc() {
    String comparator = core.format_comparator();
    String[] form =
      VarInfoName.QuantHelper.format_esc(new VarInfoName[]
	{ var1().name, var2().name }, true); // elementwise
    return form[0] + "(" + form[1] + " " + comparator + " " + form[2] + ")" + form[3];
  }

  public String format_simplify() {
    String comparator = core.format_comparator();
    if ("==".equals(comparator)) {
      comparator = "EQ";
    }
    String[] form =
      VarInfoName.QuantHelper.format_simplify(new VarInfoName[]
	{ var1().name, var2().name }, true); // elementwise
    return form[0] + "(" + comparator + " " + form[1] + " " + form[2] + ")" + form[3];
  }

  public String format_jml() {
    String comparator = core.format_comparator();
    String quantResult[] =
      VarInfoName.QuantHelper.format_jml(new VarInfoName[]
	{ var1().name, var2().name }, true);
    return quantResult[0] + quantResult[1] + " " + comparator + " " + quantResult[2] + quantResult[3];
  }

  public void add_modified(long[] a1, long[] a2, int count) {
    if (a1.length != a2.length) {
      flowThis();
      destroy();
      return;
    }
    int len = a1.length;
    // int len = Math.min(a1.length, a2.length);

    for (int i=0; i<len; i++) {
      long v1 = a1[i];
      long v2 = a2[i];
      core.add_modified(v1, v2, count);
      if (falsified)
        return;
    }
  }

  protected double computeProbability() {
    return core.computeProbability();
  }

  public boolean isSameFormula(Invariant other)
  {
    return core.isSameFormula(((PairwiseIntComparison) other).core);
  }

  public boolean isExclusiveFormula(Invariant other)
  {
    if (other instanceof PairwiseIntComparison) {
      return core.isExclusiveFormula(((PairwiseIntComparison) other).core);
    }
    return false;
  }

  // Look up a previously instantiated invariant.
  public static PairwiseIntComparison find(PptSlice ppt) {
    Assert.assert(ppt.arity == 2);
    for (Iterator itor = ppt.invs.iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if (inv instanceof PairwiseIntComparison)
        return (PairwiseIntComparison) inv;
    }
    return null;
  }

}
