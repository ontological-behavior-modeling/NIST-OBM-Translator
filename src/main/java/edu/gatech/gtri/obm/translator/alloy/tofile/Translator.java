package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;

public class Translator {

  private final ExprVisitor exprVisitor;
  private final Set<Func> ignoredFuncs;
  private final Set<Sig> ignoredSigs;

  public Translator(Set<Expr> ignoredExprs, Set<Func> ignoredFuncs, Set<Sig> ignoredSigs) {
    exprVisitor = new ExprVisitor(ignoredExprs);
    this.ignoredFuncs = ignoredFuncs;
    this.ignoredSigs = ignoredSigs;
  }

  public void generateAlsFileContents(AlloyModule alloyModule, String outFilename) {
    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with code.\n\n").append("module ")
        .append(alloyModule.getModuleName()).append('\n').append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n").append("abstract sig Occurrence {}\n\n")
        .append("// Signatures:\n");

    for (Sig sig : alloyModule.getSignatures()) {
      if (!ignoredSigs.contains(sig)) {
        exprVisitor.isRootSig = true;
        sb.append(exprVisitor.visit(sig));
      }
    }

    sb.append("\n// Facts:\n");
    sb.append(exprVisitor.visitThis(alloyModule.getFacts()));
    // sb.append("\n// Functions and predicates:\n");

    Command[] commands = alloyModule.getCommands();
    Set<String> visitedFuncs = new HashSet<>();

    // Get the functions and predicates from the commands.
    for (Command command : commands) {

      for (Func func : command.formula.findAllFunctions()) {

        if (ignoredFuncs.contains(func) || visitedFuncs.contains(func.toString())) {
          continue;
        }

        visitedFuncs.add(func.toString());

        if (func.isPred) {
          sb.append("pred ").append(MyAlloyLibrary.removeSlash(func.label));

          if (!func.decls.isEmpty()) {
            sb.append('[');

            for (int j = 0; j < func.decls.size(); j++) {
              Decl decl = func.decls.get(j);
              String[] declarations = new String[decl.names.size()];
              for (int i = 0; i < decl.names.size(); i++) {
                declarations[i] = decl.names.get(i).toString();
              }
              sb.append(String.join(",", declarations));
              sb.append(": ");
              sb.append(exprVisitor.visitThis(decl.expr));

              if (j != func.decls.size() - 1) {
                sb.append(", ");
              }
            }

            sb.append(']');
          }

          sb.append('{').append(MyAlloyLibrary.removeSlash(exprVisitor.visitThis(func.getBody())))
              .append("}\n");

        } else if (!func.isPred) {
          sb.append("fun ").append(MyAlloyLibrary.removeSlash(func.label));

          if (!func.decls.isEmpty()) {
            sb.append('[');

            for (int j = 0; j < func.decls.size(); j++) {
              Decl decl = func.decls.get(j);
              String[] declarations = new String[decl.names.size()];
              for (int i = 0; i < decl.names.size(); i++) {
                declarations[i] = decl.names.get(i).toString();
              }
              sb.append(String.join(",", declarations));
              sb.append(": ");
              sb.append(exprVisitor.visitThis(decl.expr));

              if (j != func.decls.size() - 1) {
                sb.append(", ");
              }
            }

            sb.append(']');
          }

          sb.append(": ").append(MyAlloyLibrary.removeSlash(exprVisitor.visitThis(func.returnDecl)))
              .append(" {")
              .append(MyAlloyLibrary.removeSlash(exprVisitor.visitThis(func.getBody())))
              .append("}\n");
        }
      }
    }

    // sb.append("\n// Commands:\n");
    //
    // for(Command command : commands) {
    //
    // if(command.check) {
    // sb.append("check ");
    // }
    // else if(!command.check) {
    // sb.append("run ");
    // }
    //
    // sb.append(command.label).append('{')
    // .append(exprVisitor.visitThis(command.nameExpr)).append("} for ")
    // .append(command.overall);
    //
    // if(!command.scope.isEmpty()) {
    // sb.append(" but ");
    //
    // for(CommandScope cs : command.scope) {
    // sb.append("exactly ").append(cs.startingScope).append(' ')
    // .append(cs.sig);
    // }
    // }
    //
    // sb.append('\n');
    // }

    try {
      PrintWriter pw = new PrintWriter(outFilename);
      pw.println(sb.toString());
      pw.close();
    } catch (IOException e) {
      System.err.println(e);
    }
  }

}
