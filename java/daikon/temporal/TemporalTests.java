package daikon.temporal;

import junit.framework.*;
import java.util.*;

class LetterEvent extends Event
{
    String name;

    LetterEvent(String n)
    {
	name = n;
    }

    public boolean forwardSharesTypeWith(Event e)
    {
	return (e instanceof LetterEvent);
    }

    public boolean forwardMatches(Event e)
    {
	LetterEvent l = (LetterEvent)e;

	return l.name.equals(name);
    }

    public String toString()
    {
	return name;
    }
}

//FIXME: Needs some major restructuring for efficiency/clarity.
public class TemporalTests extends TestCase
{
    Event A = new LetterEvent("A");
    Event B = new LetterEvent("B");
    Event C = new LetterEvent("C");
    Event D = new LetterEvent("D");

    public static void main(String[] args)
    {
	junit.textui.TestRunner.run(new TestSuite(TemporalTests.class));
    }

    public TemporalTests(String name)
    {
	super(name);
    }

    ScopeGlobal global;

    ScopeBefore beforeA;
    ScopeAfter afterA;

    ScopeBefore beforeB;
    ScopeAfter afterB;

    ScopeBefore beforeC;
    ScopeAfter afterC;

    ScopeBefore beforeD;
    ScopeAfter afterD;

    public void generateBasicScopes()
    {
	global = new ScopeGlobal();

	beforeA = new ScopeBefore(A);
	afterA = new ScopeAfter(A);

	beforeB = new ScopeBefore(B);
	afterB = new ScopeAfter(B);

	beforeC = new ScopeBefore(C);
	afterC = new ScopeAfter(C);

	beforeD = new ScopeBefore(D);
	afterD = new ScopeAfter(D);
    }

    public void generateSimpleScopes()
    {
	generateBasicScopes();

	global.doDynamicInstantiation = false;

	global.addChild(beforeA);
	global.addChild(afterA);
    }

    public void generateSimpleNestedScopes()
    {
	generateBasicScopes();

	global.doDynamicInstantiation = false;

	global.addChild(beforeA);
	beforeA.addChild(beforeB);
	beforeA.addChild(afterC);

	global.addChild(afterA);
	afterA.addChild(beforeD);
	afterA.addChild(afterD);
    }


    public void testBasicEnterAndExit()
    {
	//Can we do very basic entering and exiting without drooling
	//(or at least without drooling on ourselves)?
	generateSimpleNestedScopes();

	assertTrue(!global.isActive());
	assertTrue(!beforeA.isActive());
	assertTrue(!afterA.isActive());

	global.enter();

	assertTrue(global.isActive());
	assertTrue(beforeA.isActive());
	assertTrue(!afterA.isActive());

	global.exit();

	assertTrue(!global.isActive());
	assertTrue(!beforeA.isActive());
	assertTrue(!afterA.isActive());
    }

    public void testBasicScopeEventProcessing()
    {
	generateSimpleNestedScopes();

	global.enter();

	assertTrue(!global.seenEvent(A));
	assertTrue(!global.seenEvent(B));
	assertTrue(!global.seenEvent(C));

	assertTrue(!beforeA.seenEvent(A));
	assertTrue(!beforeA.seenEvent(B));
	assertTrue(!beforeA.seenEvent(C));

	assertTrue(!afterA.seenEvent(A));
	assertTrue(!afterA.seenEvent(B));
	assertTrue(!afterA.seenEvent(C));

	global.processEvent(B);

	assertTrue(global.isActive());
	assertTrue(beforeA.isActive());
	assertTrue(!afterA.isActive());

	assertTrue(global.seenEvent(B));
	assertTrue(beforeA.seenEvent(B));
	assertTrue(!afterA.seenEvent(B));

	global.processEvent(A);

	assertTrue(global.isActive());
	assertTrue(!beforeA.isActive());
	assertTrue(afterA.isActive());

	assertTrue(global.seenEvent(A));
	assertTrue(!beforeA.seenEvent(A));
	assertTrue(!afterA.seenEvent(A));

	global.processEvent(C);

	assertTrue(global.isActive());
	assertTrue(!beforeA.isActive());
	assertTrue(afterA.isActive());

	assertTrue(global.seenEvent(C));
	assertTrue(!beforeA.seenEvent(C));
	assertTrue(afterA.seenEvent(C));

	global.exit();
    }

    public void testNestedScopeEventProcessing()
    {
	generateSimpleNestedScopes();

	global.enter();

	assertTrue(beforeA.isActive());
	assertTrue(beforeB.isActive());

	global.processEvent(D);

	assertTrue(!beforeD.seenEvent(D));
	assertTrue(!afterD.seenEvent(D));
	assertTrue(!afterA.seenEvent(D));

	assertTrue(beforeA.seenEvent(D));
	assertTrue(beforeB.seenEvent(D));
	assertTrue(!afterC.seenEvent(D));

	global.processEvent(C);

	assertTrue(afterC.isActive());

	global.processEvent(D);

	assertTrue(afterC.seenEvent(D));

	global.processEvent(B);

	assertTrue(afterC.isActive());
	assertTrue(!beforeB.isActive());

	assertTrue(!afterA.seenEvent(C));

	global.processEvent(A);

	assertTrue(beforeD.isActive());
	assertTrue(!afterD.isActive());

	global.processEvent(C);

	assertTrue(afterA.seenEvent(C));
	assertTrue(beforeD.seenEvent(C));
	assertTrue(!afterD.seenEvent(C));

	global.processEvent(D);

	assertTrue(!beforeD.isActive());
	assertTrue(afterD.isActive());

	global.exit();
    }

    public void testSimpleInvariants()
    {
	generateSimpleScopes();

	TemporalInvariant g_alwaysB, g_eventuallyB, bA_alwaysB, bA_eventuallyB, aA_alwaysB, aA_eventuallyB;

	g_alwaysB = new AlwaysInvariant(global, B);
	g_eventuallyB = new EventuallyInvariant(global, B);
	bA_alwaysB = new AlwaysInvariant(beforeA, B);
	bA_eventuallyB = new EventuallyInvariant(beforeA, B);
	aA_alwaysB = new AlwaysInvariant(afterA, B);
	aA_eventuallyB = new EventuallyInvariant(afterA, B);

	assertTrue(!g_alwaysB.isFalsified);
	assertTrue(g_alwaysB.numConfirmingSequences == 0);

	//Sequence 1: CABB

	global.enter();

	assertTrue(!g_alwaysB.isFalsified);
	assertTrue(g_alwaysB.numConfirmingSequences == 0);

	global.processEvent(C);

	assertTrue(g_alwaysB.isFalsified);
	assertTrue(bA_alwaysB.isFalsified);
	assertTrue(!g_eventuallyB.isFalsified);

	global.processEvent(A);

	assertTrue(bA_eventuallyB.isFalsified);

	global.processEvent(B);

	assertTrue(!aA_alwaysB.isFalsified);
	assertTrue(!aA_eventuallyB.isFalsified);
	assertTrue(!g_eventuallyB.isFalsified);
	assertTrue(g_alwaysB.isFalsified);

	assertTrue(aA_alwaysB.numConfirmingSequences == 0);
	assertTrue(g_alwaysB.numConfirmingSequences == 0);

	global.processEvent(B);

	assertTrue(!aA_alwaysB.isFalsified);
	assertTrue(!aA_eventuallyB.isFalsified);
	assertTrue(g_alwaysB.isFalsified);

	global.exit();

	assertTrue(g_eventuallyB.numConfirmingSequences == 1);
	assertTrue(aA_alwaysB.numConfirmingSequences == 1);
	assertTrue(aA_eventuallyB.numConfirmingSequences == 1);

	global.enter();
	global.exit();

	assertTrue(g_eventuallyB.isFalsified);

	//Could probably do more here
    }

    public void testCoreBetweenScope()
    {
	ScopeBetween betAB;

	global = new ScopeGlobal();
	global.doDynamicInstantiation = false;
	betAB = new ScopeBetween(A, B);

	//Test 1 - very simple

	global.addChild(betAB);

	global.enter();

	assertTrue(!betAB.isActive());

	global.processEvent(C);

	assertTrue(!betAB.isActive());

	global.processEvent(A);

	assertTrue(betAB.isActive()); //thinks its active now

	global.processEvent(C);

	global.processEvent(B);

	assertTrue(!betAB.isActive());

	assertTrue(betAB.seenEvent(C)); //did see the C

	global.processEvent(A);

	assertTrue(betAB.isActive());

	global.processEvent(D);

	global.exit();

	assertTrue(!betAB.isActive());
	assertTrue(!betAB.seenEvent(D)); //scope didn't ever enter

	//Test 2 - now with an invariant

	global = new ScopeGlobal();
	global.doDynamicInstantiation = false;
	betAB = new ScopeBetween(A, B);

	AlwaysInvariant alwaysC = new AlwaysInvariant(betAB, C);

	//to be added later
	EventuallyInvariant eventuallyD = new EventuallyInvariant(D);

	global.addChild(betAB);

	global.enter();

	global.processEvent(A);

	global.processEvent(C);

	global.processEvent(B);

	assertTrue(!alwaysC.isFalsified);
	assertTrue(alwaysC.numConfirmingSequences == 1);

	betAB.addChild(eventuallyD);

	global.processEvent(A);

	global.processEvent(D);

	assertTrue(alwaysC.isFalsified); //thinks its falsified

	assertTrue(!eventuallyD.isFalsified);
	assertTrue(eventuallyD.happenedOnceInScope);

	global.exit();

	assertTrue(!alwaysC.isFalsified); //should still be kosher
	assertTrue(!betAB.seenEvent(D));
	assertTrue(!eventuallyD.isFalsified);

	assertTrue(eventuallyD.numConfirmingSequences == 0);
    }

    public void testCoreAfterUntilScope()
    {
	ScopeAfterUntil auAB;

	global = new ScopeGlobal();
	global.doDynamicInstantiation = false;
	auAB = new ScopeAfterUntil(A, B);

	global.addChild(auAB);

	global.enter();

	assertTrue(!auAB.isActive());

	global.processEvent(B);

	assertTrue(!auAB.isActive());

	global.processEvent(A);

	assertTrue(auAB.isActive());

	global.processEvent(C);

	assertTrue(auAB.isActive());

	global.processEvent(B);

	assertTrue(!auAB.isActive());

	global.processEvent(A);

	assertTrue(auAB.isActive());

	global.exit();

	assertTrue(!auAB.isActive());

	EventuallyInvariant eventuallyC = new EventuallyInvariant(auAB, C);
	EventuallyInvariant eventuallyB = new EventuallyInvariant(auAB, B);

	//Sequence ACB satisfies both

	global.enter();
	global.processEvent(A);

	assertTrue(auAB.isActive());

	global.processEvent(C);

	assertTrue(eventuallyC.happenedOnceInScope);
	assertTrue(!eventuallyC.isFalsified);

	global.processEvent(B);

	assertTrue(!auAB.isActive());
	assertTrue(!eventuallyC.isFalsified);
	assertTrue(eventuallyC.numConfirmingSequences == 1);

	global.exit();

	assertTrue(!eventuallyC.isFalsified);

	assertTrue(eventuallyC.numConfirmingSequences == 1);
	assertTrue(!eventuallyB.isFalsified);
	assertTrue(eventuallyB.numConfirmingSequences == 1);

	//Sequence ABCACB falsifies first, satisfies second, with count 3

	//FIXME: A little uncertain about these happenedOnceInScope tests... hmm..
	assertTrue(!eventuallyC.happenedOnceInScope);

	global.enter();

	assertTrue(!auAB.isActive());

	assertTrue(!eventuallyC.happenedOnceInScope);

	global.processEvent(A);

	assertTrue(auAB.isActive());

	assertTrue(!eventuallyC.happenedOnceInScope);

	global.processEvent(B);

	assertTrue(!auAB.isActive());

	assertTrue(eventuallyC.isFalsified);

	global.processEvent(C);
	global.processEvent(A);
	global.processEvent(C);
	global.processEvent(B);
	global.exit();

	assertTrue(eventuallyC.isFalsified);
	assertTrue(!eventuallyB.isFalsified);
	assertTrue(eventuallyB.numConfirmingSequences == 3);

	//Sequence ABA falsifies second

	global.enter();
	global.processEvent(A);
	global.processEvent(B);
	global.processEvent(A);
	global.exit();

	assertTrue(eventuallyB.isFalsified);
    }

    //FIXME: Build a querying system for the invariant tree, and make
    //some approximate correctness tests, etc.
    //FIXME: These should be fully automated! no really!
    public void testBasicDynamicInstantiation()
    {
	global = new ScopeGlobal();

	global.enter();


	//Event A creates after A *, eventually A, always A
	global.processEvent(A);
	//	global.printState();

	/*Event B creates:
	    before b, after a,
	    between a and b,
	    after a until b,

	    after a, *
	    after b, *

	    b responds to a
	    before b, eventually a
	    eventually B
	    (falsifies always A)
	*/
	global.processEvent(B);
	//	global.printState();

	/*Event B changes nothing*/
	global.processEvent(B);
	//	global.printState();

	/*Event C:
	    after a, eventually c
	    after a, NO ALWAYS C

	    eventually C
	    c responds to a

	    before c, after a,
	    between a and c,
	    after a until c,

	    and more.. argh
	*/

	global.processEvent(C);
	//	global.printState();

	global.exit();
    }

}
