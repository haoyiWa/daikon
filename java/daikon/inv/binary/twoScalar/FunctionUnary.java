package daikon.inv.binary.twoScalar;

import daikon.*;
import daikon.inv.Invariant;
import java.lang.reflect.*;


public class FunctionUnary
  extends TwoScalar
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff FunctionUnary invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  public FunctionUnaryCore core;

  protected FunctionUnary(PptSlice ppt, String methodname, Method function, boolean inverse) {
    super(ppt);
    core = new FunctionUnaryCore(this, methodname, function, inverse);
  }

  public static FunctionUnary instantiate(PptSlice ppt, String methodname, Method function, boolean inverse) {
    if (!dkconfig_enabled) return null;
    return new FunctionUnary(ppt, methodname, function, inverse);
  }

  protected Object clone() {
    FunctionUnary result = (FunctionUnary) super.clone();
    result.core = (FunctionUnaryCore) core.clone();
    result.core.wrapper = result;
    return result;
  }

  protected Invariant resurrect_done_swapped() {
    core.swap();
    return this;
  }

  public String repr() {
    return "FunctionUnary" + varNames() + ": " + core.repr();
  }

  public String format_using(OutputFormat format) {
    return core.format_using(format, var1().name, var2().name);
  }

  public void add_modified(long x_int, long y_int, int count) {
    core.add_modified(x_int, y_int, count);
  }


  protected double computeProbability() {
    return core.computeProbability();
  }

  public boolean isExact() {
    return true;
  }

  public boolean isSameFormula(Invariant other)
  {
    return core.isSameFormula(((FunctionUnary) other).core);
  }
}
