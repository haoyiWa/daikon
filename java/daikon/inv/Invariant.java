package daikon.inv;

import daikon.*;

// Base implementation for Invariant objects.
// Intended to be subclassed but not to be directly instantiated.
// I should probably rename this to "Invariant" and get rid of that interface.o

public abstract class Invariant {
  public PptSlice ppt;			// includes values, number of samples, VarInfos, etc.

  // Has to be public because of wrappers.
  public boolean no_invariant = false;

  // Subclasses should set these; Invariant never does.

  // The probability that this could have happened by chance alone.
  // (The name "confidence" is a bit of a misnomer.)
  //   0 = could never have happened; that is, we are fully confident that this
  //       invariant is a real invariant
  //   (0..1) = greater to lesser likelihood of coincidence
  //   1 = must have happened by chance
  //   2 = we haven't yet seen enough data to know whether this invariant is
  //       true, much less its justification
  public final static double PROBABILITY_UNKNOWN = 2;
  //   3 = delete this invariant; we know it's not true
  public final static double PROBABILITY_NEVER = 3;

  // The probability that the invariant occurred by chance must be less
  // than this in order for it to be displayed.  Maybe it should be
  // user-settable rather than final.
  public final static double probability_limit = .01;

  // // Do I want to have this cache at all?  It may not be that expensive
  // // to compute from scratch, and I may not be interested in it that often.
  // protected double probability_cache = 0;
  // protected boolean probability_cache_accurate = false;

  // If confidence == 0, then this invariant can be eliminated.
  public double getProbability() {
    return computeProbability();
    // if (!probability_cache_accurate) {
    //   probability_cache = computeProbability();
    //   probability_cache_accurate = true;
    // }
    // return probability_cache;
  }
  protected abstract double computeProbability();

  public boolean justified() {
    return (!no_invariant) && (getProbability() <= probability_limit);
  }

  // Has to be public because of wrappers.
  public void destroy() {
    no_invariant = true;
    ppt.removeInvariant(this);
  }

  // Implementations of this need to examine all the data values already
  // in the ppt.  Or, don't put too much work in the constructor and instead
  // have the caller do that.
  protected Invariant(PptSlice ppt_) {
    ppt = ppt_;
    // probability_cache_accurate = false;
    ppt.invs.add(this);
    // System.out.println("Added invariant " + this + " to Ppt " + ppt.name + " = " + ppt + "; now has " + ppt.invs.size() + " invariants in " + ppt.invs);
  }

  // Regrettably, I can't declare a static abstract method.
  // // The return value is probably ignored.  The new Invariant installs
  // // itself on the PptSlice, and that's what really matters (right?).
  // public static abstract Invariant instantiate(PptSlice ppt);

  public boolean usesVar(VarInfo vi) {
    return ppt.usesVar(vi);
  }

  // For use by subclasses.
  /** Put a string representation of the variable names in the StringBuffer. */
  public void varNames(StringBuffer sb) {
    // sb.append(this.getClass().getName());
    ppt.varNames(sb);
  }

  /** Return a string representation of the variable names. */
  public String varNames() {
    return ppt.varNames();
  }

  public String name() {
    return this.getClass().getName() + varNames();
  }

  /**
   * Returns true if this invariant is necessarily true, due to derived
   * variables, other invariants, etc.
   * Intended to be overridden by subclasses.
   */
  public final boolean isObvious() {
    // We don't need to do this because we won't add obvious-derived
    // invariants to lists in the first place.
    // return isObviousDerived() || isObviousImplied();
    return isObviousImplied();
  }

  /**
   * Returns true if this invariant is necessarily true, due to being implied
   * by other (more basic or preferable to report) invariants.
   * Intended to be overridden by subclasses.
   */
  public boolean isObviousImplied() {
    return false;
  }



  /**
   * Returns true if this invariant implies the argument invariant.
   * Intended to be overridden by subclasses.
   */
  public boolean implies(Invariant inv) {
    return false;
  }


  // For printing invariants, there are two interfaces:
  //   repr gives a low-level representation
  //   format gives a high-level representation (eg, for user output)

  public abstract String repr();
  public abstract String format();


  // Different invariants use different arities here.
  // public abstract void add(ValueTuple vt, int count);

  // public abstract String toString();

}


// class invariant:
//
//     def is_exact(self):
//         return self.values == 1
//
//     def is_unconstrained(self):
//         if self.unconstrained_internal == None:
//             self.format()
//         return self.unconstrained_internal
//
//     def format(self, args=None):
//         if self.samples == 0:
//             self.unconstrained_internal = false
//             return None
//
//         self.unconstrained_internal = false
//
//         if args == None:
//             args = map(lambda x: x.name, self.var_infos)
//         if (type(args) in [types.ListType, types.TupleType]) and (len(args) == 1):
//             args = args[0]
//
//         if self.one_of:
//             # If it can be None, print it only if it is always None and
//             # is an invariant over non-derived variable.
//             if self.can_be_None:
//                 if ((len(self.one_of) == 1)
//                     and self.var_infos):
//                     some_nonderived = false
//                     for vi in self.var_infos:
//                     	some_nonderived = some_nonderived or not vi.is_derived
//                     if some_nonderived:
//                         return "%s = uninit" % (args,)
//             elif len(self.one_of) == 1:
//                 return "%s = %s" % (args, self.one_of[0])
//             ## Perhaps I should unconditionally return this value;
//             ## otherwise I end up printing ranges more often than small
//             ## numbers of values (because when few values and many samples,
//             ## the range always looks justified).
//             # If few samples, don't try to infer a function over the values;
//             # just return the list.
//             elif (len(self.one_of) <= 3) or (self.samples < 100):
//                 return "%s in %s" % (args, util.format_as_set(self.one_of))
//         self.unconstrained_internal = true
//         return None
//
//     # Possibly add an optional "args=None" argument, for formatting.
//     def diff(self, other):
//         """Returns None or a description of the difference."""
//         # print "diff(invariant)"
//         inv1 = self
//         inv2 = other
//         assert inv1.__class__ == inv2.__class__
//         if inv1.is_unconstrained() and inv2.is_unconstrained():
//             return None
//         if inv1.is_unconstrained() ^ inv2.is_unconstrained():
//             return "One is unconstrained but the other is not"
//         if inv1.one_of and inv2.one_of and inv1.one_of != inv2.one_of:
//             return "Different small number of values"
//         if inv1.can_be_None ^ inv2.can_be_None:
//             return "One can be None but the other cannot"
//         # return "invariant.diff: no differences"	# debugging
//         return None
