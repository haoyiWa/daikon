package daikon.derive.unary;
import daikon.*;
import daikon.derive.*;
import daikon.derive.binary.*;
import utilMDE.*;

// originally from pass1.
public final class SequenceLength extends UnaryDerivation {

  public final int shift;

  public SequenceLength(VarInfo vi, int shift) {
    super(vi);
    this.shift = shift;         // typically 0 or -1
  }

  public static boolean applicable(VarInfo vi) {
    Assert.assert(vi.rep_type.isArray());

    if (vi.derived != null) {
      Assert.assert(vi.derived instanceof SequenceScalarSubsequence);
      return false;
    }
    // Don't do this for now, because we depend on being able to call
    // sequenceSize() later.
    // if (vi.name.indexOf("~.") != -1)
    //   return false;

    return true;
  }

  public ValueAndModified computeValueAndModified(ValueTuple vt) {
    int source_mod = base.getModified(vt);
    if (source_mod == ValueTuple.MISSING)
      return ValueAndModified.MISSING;
    Object val = base.getValue(vt);
    if (val == null) {
      return ValueAndModified.MISSING;
    }

    int len;
    ProglangType rep_type = base.rep_type;

    if (rep_type == ProglangType.INT_ARRAY) {
      len = ((long[])val).length;
    } else if (rep_type == ProglangType.DOUBLE_ARRAY) {
      len = ((double[])val).length;
    } else {
      len = ((Object[])val).length;
    }
    return new ValueAndModified(Intern.internedLong(len+shift), source_mod);
  }

  protected VarInfo makeVarInfo() {
    VarInfoName name = base.name.applySize();
    switch (shift) {
    case 0:
      break;
    case -1:
      name = name.applyDecrement();
      break;
    default:
      throw new UnsupportedOperationException("Unsupported shift: " + shift);
    }
    ProglangType ptype = ProglangType.INT;
    ProglangType rtype = ProglangType.INT;
    VarComparability comp = base.comparability.indexType(0);
    return new VarInfo(name, ptype, rtype, comp);
  }

}
