package daikon.inv.binary.twoSequence;

import daikon.*;
import daikon.inv.Invariant;
import daikon.inv.binary.twoScalar.*;
import java.lang.reflect.*;


/**
 * That each element from one sequence relates to each corresponding
 * element in another sequence by a function.
 **/

public class PairwiseFunctionUnary
  extends TwoSequence
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff PairwiseFunctionUnary invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  public FunctionUnaryCore core;

  protected PairwiseFunctionUnary(PptSlice ppt, String methodname, Method function, boolean inverse) {
    super(ppt);
    core = new FunctionUnaryCore(this, methodname, function, inverse);
  }

  public static PairwiseFunctionUnary instantiate(PptSlice ppt, String methodname, Method function, boolean inverse) {
    if (!dkconfig_enabled) return null;
    PairwiseFunctionUnary result =
      new PairwiseFunctionUnary(ppt, methodname, function, inverse);
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
    PairwiseFunctionUnary result = (PairwiseFunctionUnary) super.clone();
    result.core = (FunctionUnaryCore) core.clone();
    result.core.wrapper = result;
    return result;
  }

  protected Invariant resurrect_done_swapped() {
    core.swap();
    return this;
  }

  public String repr() {
    return "PairwiseFunctionUnary" + varNames() + ": " + core.repr();
  }

  public String format_using(OutputFormat format) {
    if (format == OutputFormat.IOA) {
      return format_ioa();
    }

    return core.format_using(format, var1().name, var2().name);
  }

  /* IOA */
  public String format_ioa() {
    if (var1().isIOASet() || var2().isIOASet())
      return "Not valid for sets: " + format();
    VarInfoName.QuantHelper.IOAQuantification quant1 = new VarInfoName.QuantHelper.IOAQuantification(var1());
    VarInfoName.QuantHelper.IOAQuantification quant2 = new VarInfoName.QuantHelper.IOAQuantification(var2());

    return quant1.getQuantifierExp()
      + core.format_using(OutputFormat.IOA,
			  quant1.getVarName(0),
			  quant2.getVarName(0))
      + quant1.getClosingExp();
  }

  public void add_modified(long[] x_arr, long[] y_arr, int count) {
    if (x_arr.length != y_arr.length) {
      flowThis();
      destroy();
      return;
    }
    int len = x_arr.length;
    // int len = Math.min(x_arr.length, y_arr.length);

    for (int i=0; i<len; i++) {
      long x = x_arr[i];
      long y = y_arr[i];

      core.add_modified(x, y, count);
      if (falsified)
        return;
    }
  }

  protected double computeProbability() {
    return core.computeProbability();
  }

  public boolean isSameFormula(Invariant other)
  {
    return core.isSameFormula(((PairwiseFunctionUnary) other).core);
  }

}
