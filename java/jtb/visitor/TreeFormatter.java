//
// Stub of this file Generated by JTB 1.3.2
// Modified by Kevin Tao, taokr@@cs.purdue.edu
// Updated for Java 1.5 by Carlos Pacheco.
//

package jtb.visitor;

import java.util.Enumeration;
import java.util.Vector;
import jtb.syntaxtree.*;

/**
 * A skeleton output formatter for your language grammar.  Using the
 * add() method along with force(), indent(), and outdent(), you can
 * easily specify how this visitor will format the given syntax tree.
 * See the JTB documentation for more details.
 *
 * Pass your syntax tree to this visitor, and then to the TreeDumper
 * visitor in order to "pretty print" your tree.
 */
public class TreeFormatter extends DepthFirstVisitor
{

   private Vector<FormatCommand> cmdQueue = new Vector<FormatCommand>();
   private boolean lineWrap;
   private int wrapWidth;
   private int indentAmt;
   private int curLine = 1;
   private int curColumn = 1;
   private int curIndent = 0;

   /**
    * The default constructor assumes an indentation amount of 3 spaces
    * and no line-wrap.  You may alternately use the other constructor to
    * specify your own indentation amount and line width.
    */
    public TreeFormatter()
    {
        this(3, 0);
    }

   /**
    * This constructor accepts an indent amount and a line width which is
    * used to wrap long lines.  If a token's beginColumn value is greater
    * than the specified wrapWidth, it will be moved to the next line and
    * indented one extra level.  To turn off line-wrapping, specify a
    * wrapWidth of 0.
    *
    * @param   indentAmt   Amount of spaces per indentation level.
    * @param   wrapWidth   Wrap lines longer than wrapWidth.  0 for no wrap.
    */
   public TreeFormatter(int indentAmt, int wrapWidth) {
      this.indentAmt = indentAmt;
      this.wrapWidth = wrapWidth;

      if ( wrapWidth > 0 )
         lineWrap = true;
      else
         lineWrap = false;
   }

   /**
    * Accepts a NodeListInterface object and performs an optional format
    * command between each node in the list (but not after the last node).
    */
   protected void processList(NodeListInterface n) {
      processList(n, null);
   }

   protected void processList(NodeListInterface n, FormatCommand cmd) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this);
         if ( cmd != null && e.hasMoreElements() )
            cmdQueue.addElement(cmd);
      }
   }

   /**
    * A Force command inserts a line break and indents the next line to
    * the current indentation level.  Use "add(force());".
    */
   protected FormatCommand force() { return force(1); }
   protected FormatCommand force(int i) {
      return new FormatCommand(FormatCommand.FORCE, i);
   }

   /**
    * An Indent command increases the indentation level by one (or a
    * user-specified amount).  Use "add(indent());".
    */
   protected FormatCommand indent() { return indent(1); }
   protected FormatCommand indent(int i) {
      return new FormatCommand(FormatCommand.INDENT, i);
   }

   /**
    * An Outdent command is the reverse of the Indent command: it reduces
    * the indentation level.  Use "add(outdent());".
    */
   protected FormatCommand outdent() { return outdent(1); }
   protected FormatCommand outdent(int i) {
      return new FormatCommand(FormatCommand.OUTDENT, i);
   }

   /**
    * A Space command simply adds one or a user-specified number of
    * spaces between tokens.  Use "add(space());".
    */
   protected FormatCommand space() { return space(1); }
   protected FormatCommand space(int i) {
      return new FormatCommand(FormatCommand.SPACE, i);
   }

   /**
    * Use this method to add FormatCommands to the command queue to be
    * executed when the next token in the tree is visited.
    */
   protected void add(FormatCommand cmd) {
      cmdQueue.addElement(cmd);
   }


   /**
    * Executes the commands waiting in the command queue, then inserts the
    * proper location information into the current NodeToken.
    *
    * If there are any special tokens preceding this token, they will be
    * given the current location information.  The token will follow on
    * the next line, at the proper indentation level.  If this is not the
    * behavior you want from special tokens, feel free to modify this
    * method.
    */
   public void visit(NodeToken n) {
      for ( Enumeration<FormatCommand> e = cmdQueue.elements(); e.hasMoreElements(); ) {
         FormatCommand cmd = e.nextElement();
         switch ( cmd.getCommand() ) {
         case FormatCommand.FORCE :
            curLine += cmd.getNumCommands();
            curColumn = curIndent + 1;
            break;
         case FormatCommand.INDENT :
            curIndent += indentAmt * cmd.getNumCommands();
            break;
         case FormatCommand.OUTDENT :
            if ( curIndent >= indentAmt )
               curIndent -= indentAmt * cmd.getNumCommands();
            break;
         case FormatCommand.SPACE :
            curColumn += cmd.getNumCommands();
            break;
         default :
            throw new TreeFormatterException(
               "Invalid value in command queue.");
         }
      }

      cmdQueue.removeAllElements();

      //
      // Handle all special tokens preceding this NodeToken
      //
      if ( n.numSpecials() > 0 )
         for ( Enumeration<NodeToken> e = n.specialTokens.elements();
               e.hasMoreElements(); ) {
            NodeToken special = e.nextElement();

            //
            // -Place the token.
            // -Move cursor to next line after the special token.
            // -Don't update curColumn--want to keep current indent level.
            //
            placeToken(special, curLine, curColumn);
            curLine = special.endLine + 1;
         }

      placeToken(n, curLine, curColumn);
      curLine = n.endLine;
      curColumn = n.endColumn;
   }

   /**
    * Inserts token location (beginLine, beginColumn, endLine, endColumn)
    * information into the NodeToken.  Takes into account line-wrap.
    * Does not update curLine and curColumn.
    */
   private void placeToken(NodeToken n, int line, int column) {
      int length = n.tokenImage.length();

      //
      // Find beginning of token.  Only line-wrap for single-line tokens
      //
      if ( !lineWrap || n.tokenImage.indexOf('\n') != -1 ||
           column + length <= wrapWidth )
         n.beginColumn = column;
      else {
         ++line;
         column = curIndent + indentAmt + 1;
         n.beginColumn = column;
      }

      n.beginLine = line;

      //
      // Find end of token; don't count \n if it's the last character
      //
      for ( int i = 0; i < length; ++i ) {
         if ( n.tokenImage.charAt(i) == '\n' && i < length - 1 ) {
            ++line;
            column = 1;
         }
         else
            ++column;
      }

      n.endLine = line;
      n.endColumn = column;
   }


   //
   // User-generated visitor methods below
   //
    public void visit(CompilationUnit n)
    {
        if(n.f0.present())
        {
            n.f0.accept(this);
            add(force(2));
        }
        if(n.f1.present())
        {
            processList(n.f1, force());
            add(force(2));
        }
        if(n.f2.present())
        {
            processList(n.f2, force(2));
            add(force());
        }
        n.f3.accept(this);
    }

    public void visit(VariableDeclarator n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            processList((NodeSequence)n.f1.node, space());
        }
    }

    public void visit(VariableDeclaratorId n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            processList(n.f1);
    }

    public void visit(VariableInitializer n)
    {
        n.f0.accept(this);
    }

    public void visit(ArrayInitializer n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            NodeSequence seq = (NodeSequence)n.f1.node;
            NodeListOptional nlo = (NodeListOptional)seq.elementAt(1);
            add(space());
            seq.elementAt(0).accept(this);
            for(Enumeration e = nlo.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
        if(n.f2.present())
            n.f2.accept(this);
        add(space());
        n.f3.accept(this);
    }

    public void visit(FormalParameters n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            NodeSequence seq = (NodeSequence)n.f1.node;
            NodeListOptional nlo = (NodeListOptional)seq.elementAt(1);
            seq.elementAt(0).accept(this);
            for(Enumeration e = nlo.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
        n.f2.accept(this);
    }

    public void visit(ConstructorDeclaration n)
    {
        if(n.f0.present())
        {
            n.f0.accept(this);
            add(space());
        }
        n.f1.accept(this);
        n.f2.accept(this);
        if(n.f3.present())
        {
            add(space());
            processList((NodeSequence)n.f3.node, space());
        }
        add(force());
        n.f4.accept(this);
        add(indent());
        if(n.f5.present())
        {
            add(force());
            n.f5.accept(this);
        }
        if(n.f6.present())
        {
            add(force());
            processList(n.f6, force());
        }
        add(outdent());
        add(force());
        n.f7.accept(this);
    }

    public void visit(ExplicitConstructorInvocation n)
    {
        n.f0.accept(this);
    }

    public void visit(Initializer n)
    {
        if(n.f0.present())
        {
            n.f0.accept(this);
            add(force());
        }
        n.f1.accept(this);
    }

    public void visit(PrimitiveType n)
    {
        n.f0.accept(this);
    }

    public void visit(ResultType n)
    {
        n.f0.accept(this);
    }

    public void visit(Name n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            processList(n.f1);
    }

    public void visit(NameList n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
    }

    public void visit(Expression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            processList((NodeSequence)n.f1.node, space());
        }
    }

    public void visit(AssignmentOperator n)
    {
        n.f0.accept(this);
    }

    public void visit(ConditionalExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            processList((NodeSequence)n.f1.node, space());
        }
    }

    public void visit(ConditionalOrExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(ConditionalAndExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(InclusiveOrExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(ExclusiveOrExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(AndExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(EqualityExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(InstanceOfExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            processList((NodeSequence)n.f1.node, space());
        }
    }

    public void visit(RelationalExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(ShiftExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(AdditiveExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(MultiplicativeExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()))
                add(space());

        }
    }

    public void visit(UnaryExpression n)
    {
        if(n.f0.which == 0)
            processList((NodeSequence)n.f0.choice, space());
        else
            n.f0.accept(this);
    }

    public void visit(PreIncrementExpression n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
    }

    public void visit(PreDecrementExpression n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
    }

    public void visit(UnaryExpressionNotPlusMinus n)
    {
        n.f0.accept(this);
    }

    public void visit(CastLookahead n)
    {
        n.f0.accept(this);
    }

    public void visit(PostfixExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            n.f1.accept(this);
    }

    public void visit(CastExpression n)
    {
        n.f0.accept(this);
    }

    public void visit(PrimaryExpression n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            processList(n.f1);
    }

    public void visit(PrimaryPrefix n)
    {
        n.f0.accept(this);
    }

    public void visit(PrimarySuffix n)
    {
        n.f0.accept(this);
    }

    public void visit(Literal n)
    {
        n.f0.accept(this);
    }

    public void visit(BooleanLiteral n)
    {
        n.f0.accept(this);
    }

    public void visit(NullLiteral n)
    {
        n.f0.accept(this);
    }

    public void visit(Arguments n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            n.f1.accept(this);
        n.f2.accept(this);
    }

    public void visit(ArgumentList n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
    }

    public void visit(AllocationExpression n)
    {
        NodeSequence seq = (NodeSequence)n.f0.choice;
        if(n.f0.which == 0)
        {
            seq.elementAt(0).accept(this);
            add(space());
            seq.elementAt(1).accept(this);
            seq.elementAt(2).accept(this);
        } else
        {
            seq.elementAt(0).accept(this);
            add(space());
            for(int i = 1; i < seq.size(); i++)
                seq.elementAt(i).accept(this);

        }
    }

    public void visit(ArrayDimsAndInits n)
    {
        if(n.f0.which == 0)
            n.f0.accept(this);
        else
            processList((NodeSequence)n.f0.choice, space());
    }

    public void visit(LabeledStatement n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
    }

    public void visit(Block n)
    {
        n.f0.accept(this);
        add(indent());
        if(n.f1.present())
        {
            add(force());
            processList(n.f1, force());
        }
        add(outdent());
        add(force());
        n.f2.accept(this);
    }

    public void visit(LocalVariableDeclaration n)
    {
        if(n.f0.present())
        {
            n.f0.accept(this);
            add(space());
        }
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        if(n.f3.present())
        {
            for(Enumeration e = n.f3.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
    }

    public void visit(EmptyStatement n)
    {
        n.f0.accept(this);
    }

    public void visit(StatementExpression n)
    {
        if(n.f0.which == 0 || n.f0.which == 1)
        {
            n.f0.accept(this);
        } else
        {
            NodeSequence seq = (NodeSequence)n.f0.choice;
            NodeOptional opt = (NodeOptional)seq.elementAt(1);
            seq.elementAt(0).accept(this);
            if(opt.present())
            {
                NodeChoice choice = (NodeChoice)opt.node;
                if(choice.which == 0 || choice.which == 1)
                {
                    choice.accept(this);
                } else
                {
                    add(space());
                    processList((NodeSequence)choice.choice, space());
                }
            }
        }
    }

    public void visit(SwitchStatement n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        add(space());
        n.f3.accept(this);
        add(force());
        n.f4.accept(this);
        add(indent());
        if(n.f5.present())
        {
            for(Enumeration e = n.f5.elements(); e.hasMoreElements(); add(outdent()))
            {
                NodeSequence seq = (NodeSequence)e.nextElement();
                NodeListOptional nlo = (NodeListOptional)seq.elementAt(1);
                add(force());
                seq.elementAt(0).accept(this);
                add(indent());
                if(nlo.present())
                {
                    add(force());
                    processList((NodeListOptional)seq.elementAt(1), force());
                }
            }

        }
        add(outdent());
        add(force());
        n.f6.accept(this);
    }

    public void visit(SwitchLabel n)
    {
        processList((NodeSequence)n.f0.choice, space());
    }

    public void visit(IfStatement n)
    {
        boolean isBlock = n.f4.f0.which == 2;
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        add(space());
        n.f3.accept(this);
        if(!isBlock)
            add(indent());
        add(force());
        n.f4.accept(this);
        if(!isBlock)
            add(outdent());
        if(n.f5.present())
        {
            NodeSequence seq = (NodeSequence)n.f5.node;
            isBlock = ((Statement)seq.elementAt(1)).f0.which == 2;
            add(force());
            seq.elementAt(0).accept(this);
            if(!isBlock)
                add(indent());
            add(force());
            seq.elementAt(1).accept(this);
            if(!isBlock)
                add(outdent());
        }
    }

    public void visit(WhileStatement n)
    {
        boolean isBlock = n.f4.f0.which == 2;
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        add(space());
        n.f3.accept(this);
        if(!isBlock)
            add(indent());
        add(force());
        n.f4.accept(this);
        if(!isBlock)
            add(outdent());
    }

    public void visit(DoStatement n)
    {
        boolean isBlock = n.f1.f0.which == 2;
        n.f0.accept(this);
        if(!isBlock)
            add(indent());
        add(force());
        n.f1.accept(this);
        if(!isBlock)
            add(outdent());
        add(force());
        n.f2.accept(this);
        add(space());
        n.f3.accept(this);
        add(space());
        n.f4.accept(this);
        add(space());
        n.f5.accept(this);
        n.f6.accept(this);
    }

    public void visit(ForInit n)
    {
        n.f0.accept(this);
    }

    public void visit(StatementExpressionList n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            for(Enumeration e = n.f1.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
    }

    public void visit(ForUpdate n)
    {
        n.f0.accept(this);
    }

    public void visit(BreakStatement n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            n.f1.accept(this);
        }
        n.f2.accept(this);
    }

    public void visit(ContinueStatement n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            n.f1.accept(this);
        }
        n.f2.accept(this);
    }

    public void visit(ReturnStatement n)
    {
        n.f0.accept(this);
        if(n.f1.present())
        {
            add(space());
            n.f1.accept(this);
        }
        n.f2.accept(this);
    }

    public void visit(ThrowStatement n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        n.f2.accept(this);
    }

    public void visit(SynchronizedStatement n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        add(space());
        n.f3.accept(this);
        add(force());
        n.f4.accept(this);
    }

    public void visit(TryStatement n)
    {
        n.f0.accept(this);
        add(force());
        n.f1.accept(this);
        if(n.f2.present())
        {
            NodeSequence seq;
            for(Enumeration e = n.f2.elements(); e.hasMoreElements(); seq.elementAt(4).accept(this))
            {
                seq = (NodeSequence)e.nextElement();
                add(force());
                seq.elementAt(0).accept(this);
                add(space());
                seq.elementAt(1).accept(this);
                seq.elementAt(2).accept(this);
                seq.elementAt(3).accept(this);
                add(force());
            }

        }
        if(n.f3.present())
        {
            add(force());
            processList((NodeSequence)n.f3.node, force());
        }
    }

    public void visit(PackageDeclaration n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        n.f2.accept(this);
    }

    public void visit(ImportDeclaration n)
    {
        n.f0.accept(this);
        add(space());
        if(n.f1.present())
        {
            n.f1.accept(this);
            add(space());
        }
        n.f2.accept(this);
        if(n.f3.present())
            n.f3.accept(this);
        n.f4.accept(this);
    }

    public void visit(Modifiers n)
    {
        if(n.f0.present())
        {
            processList(n.f0, space());
            add(space());
        }
    }

    public void visit(TypeDeclaration n)
    {
        n.f0.accept(this);
    }

    public void visit(ClassOrInterfaceDeclaration n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
        {
            n.f2.accept(this);
            add(space());
        }
        if(n.f3.present())
        {
            n.f3.accept(this);
            add(space());
        }
        if(n.f4.present())
        {
            n.f4.accept(this);
            add(space());
        }
        add(force());
        n.f5.accept(this);
    }

    public void visit(ExtendsList n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
            processList(n.f2, space());
    }

    public void visit(ImplementsList n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
            processList(n.f2, space());
    }

    public void visit(EnumDeclaration n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
        {
            n.f2.accept(this);
            add(space());
        }
        n.f3.accept(this);
        add(space());
    }

    public void visit(EnumBody n)
    {
        n.f0.accept(this);
        add(space());
        if(n.f1.present())
        {
            processList((NodeSequence)n.f1.node, space());
            add(space());
        }
        if(n.f2.present())
        {
            n.f2.accept(this);
            add(space());
        }
        if(n.f3.present())
        {
            processList((NodeSequence)n.f3.node, space());
            add(space());
        }
        n.f4.accept(this);
    }

    public void visit(EnumConstant n)
    {
        n.f0.accept(this);
        add(space());
        if(n.f1.present())
        {
            n.f1.accept(this);
            add(space());
        }
        if(n.f2.present())
        {
            n.f2.accept(this);
            add(space());
        }
    }

    public void visit(TypeParameters n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        if(n.f2.present())
            processList(n.f2, space());
        n.f3.accept(this);
    }

    public void visit(TypeParameter n)
    {
        n.f0.accept(this);
        add(space());
        if(n.f1.present())
            n.f1.accept(this);
    }

    public void visit(TypeBound n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
            processList(n.f2, space());
    }

    public void visit(ClassOrInterfaceBody n)
    {
        n.f0.accept(this);
        add(indent());
        if(n.f1.present())
        {
            add(force());
            Enumeration e = n.f1.elements();
            do
            {
                if(!e.hasMoreElements())
                    break;
                NodeChoice choice = ((ClassOrInterfaceBodyDeclaration)e.nextElement()).f0;
                choice.accept(this);
                if(!e.hasMoreElements())
                    break;
                switch(choice.which)
                {
                case 4: // '\004'
                    add(force());
                    break;

                default:
                    add(force(2));
                    break;
                }
            } while(true);
        }
        add(outdent());
        add(force());
        n.f2.accept(this);
    }

    public void visit(ClassOrInterfaceBodyDeclaration n)
    {
        n.f0.accept(this);
    }

    public void visit(FieldDeclaration n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
        {
            for(Enumeration e = n.f2.elements(); e.hasMoreElements(); processList((NodeSequence)e.nextElement(), space()));
        }
        n.f3.accept(this);
    }

    public void visit(MethodDeclaration n)
    {
        if(n.f0.present())
        {
            n.f0.accept(this);
            add(space());
        }
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        if(n.f3.present())
        {
            add(space());
            processList((NodeSequence)n.f3.node, space());
        }
        if(n.f4.which == 0)
            add(force());
        n.f4.accept(this);
    }

    public void visit(MethodDeclarator n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        if(n.f2.present())
            processList(n.f2);
    }

    public void visit(FormalParameter n)
    {
        if(n.f0.present())
        {
            n.f0.accept(this);
            add(space());
        }
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
        {
            n.f2.accept(this);
            add(space());
        }
        n.f3.accept(this);
    }

    public void visit(Type n)
    {
        n.f0.accept(this);
    }

    public void visit(ReferenceType n)
    {
        n.f0.accept(this);
    }

    public void visit(ClassOrInterfaceType n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            n.f1.accept(this);
        if(n.f2.present())
            processList(n.f2);
    }

    public void visit(TypeArguments n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        if(n.f2.present())
            processList(n.f2);
        n.f3.accept(this);
    }

    public void visit(TypeArgument n)
    {
        if(n.f0.which == 0)
            n.f0.accept(this);
        else
            processList((NodeSequence)n.f0.choice, space());
    }

    public void visit(WildcardBounds n)
    {
        processList((NodeSequence)n.f0.choice, space());
    }

    public void visit(MemberSelector n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
    }

    public void visit(Statement n)
    {
        n.f0.accept(this);
    }

    public void visit(AssertStatement n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        if(n.f2.present())
            processList((NodeSequence)n.f2.node, space());
        n.f3.accept(this);
    }

    public void visit(BlockStatement n)
    {
        n.f0.accept(this);
    }

    public void visit(ForStatement n)
    {
        boolean isBlock = n.f4.f0.which == 2;
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        add(space());
        processList((NodeSequence)n.f2.choice, space());
        n.f3.accept(this);
        if(!isBlock)
            add(indent());
        add(force());
        n.f4.accept(this);
        if(!isBlock)
            add(outdent());
    }

    public void visit(RUNSIGNEDSHIFT n)
    {
        n.f0.accept(this);
    }

    public void visit(RSIGNEDSHIFT n)
    {
        n.f0.accept(this);
    }

    public void visit(Annotation n)
    {
        n.f0.accept(this);
    }

    public void visit(NormalAnnotation n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        add(space());
        if(n.f3.present())
            n.f3.accept(this);
        n.f4.accept(this);
    }

    public void visit(MarkerAnnotation n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
    }

    public void visit(SingleMemberAnnotation n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
    }

    public void visit(MemberValuePairs n)
    {
        n.f0.accept(this);
        if(n.f1.present())
            processList(n.f1, space());
    }

    public void visit(MemberValuePair n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
    }

    public void visit(MemberValue n)
    {
        n.f0.accept(this);
    }

    public void visit(MemberValueArrayInitializer n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
        if(n.f2.present())
            processList(n.f2);
        if(n.f3.present())
            n.f3.accept(this);
        add(space());
        n.f4.accept(this);
    }

    public void visit(AnnotationTypeDeclaration n)
    {
        n.f0.accept(this);
        n.f1.accept(this);
        add(space());
        n.f2.accept(this);
        add(space());
        n.f3.accept(this);
    }

    public void visit(AnnotationTypeBody n)
    {
        n.f0.accept(this);
        add(space());
        if(n.f1.present())
            processList(n.f1, space());
        n.f2.accept(this);
    }

    public void visit(AnnotationTypeMemberDeclaration n)
    {
        n.f0.accept(this);
    }

    public void visit(DefaultValue n)
    {
        n.f0.accept(this);
        add(space());
        n.f1.accept(this);
    }

}

class FormatCommand {
   public static final int FORCE = 0;
   public static final int INDENT = 1;
   public static final int OUTDENT = 2;
   public static final int SPACE = 3;

   private int command;
   private int numCommands;

   FormatCommand(int command, int numCommands) {
      this.command = command;
      this.numCommands = numCommands;
   }

   public int getCommand()             { return command; }
   public int getNumCommands()         { return numCommands; }
   public void setCommand(int i)       { command = i; }
   public void setNumCommands(int i)   { numCommands = i; }
}

class TreeFormatterException extends RuntimeException {
   TreeFormatterException()         { super(); }
   TreeFormatterException(String s) { super(s); }
}
