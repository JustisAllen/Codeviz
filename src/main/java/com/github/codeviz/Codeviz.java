package com.github.codeviz;

import com.github.codeviz.ast.*;
import com.github.codeviz.ast.Process; // Explicit import due to conflict with java.lang.Process

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

import com.google.common.collect.Iterators;

import org.anarres.graphviz.builder.GraphVizUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public class Codeviz {
  public static void main(String[] args) throws IOException {

    //$ Parse the file into JavaParser's abstract syntax
    CompilationUnit javaAst = null;
    try (FileInputStream inputFile = new FileInputStream(
        System.getProperty("user.dir")
            + System.getProperty("file.separator")
            + args[0])) {
      javaAst = JavaParser.parse(inputFile);
    } catch (IOException | com.github.javaparser.ParseException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }

    //$ Find the "main" method
    String methodToParse = "main";
    Optional<MethodDeclaration> mainMethod = getMethod(methodToParse, javaAst);

    //? No "main" method detected?
    if (!mainMethod.isPresent()) {
      //X Exit with an error message
      System.out.println("No \"" + methodToParse + "\" method detected in file.");
      System.exit(1);
    }

    //$ Parse the method to the flowchart abstract syntax
    Optional<FlowchartNode> abstractFlowchart = null;
    try {
      abstractFlowchart = parse(mainMethod.get());
    } catch (java.text.ParseException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }

    //? No top-level comments in method?
    if (!abstractFlowchart.isPresent()) {
      //X Exit with an error message
      System.out.println("No top-level comments to parse in \"" + methodToParse + "\" method.");
      System.exit(1);
    }

    //$ Write the Graphviz DOT file based on the flowchart AST
    GraphVizUtils.toGraphVizFile(new File("out.dot"), abstractFlowchart.get());
  }

  /**
   * @return The {@link MethodDeclaration} (wrapped in an {@link Optional}) in {@code javaAst}
   *    corresponding to the method with the specified {@code methodName}
   *    --{@link Optional#empty()} if the method does not exist.
   */
  public static Optional<MethodDeclaration> getMethod(String methodName, CompilationUnit javaAst) {
    for (Node node : javaAst.getChildrenNodes()) {
      if (node instanceof ClassOrInterfaceDeclaration) {
        for (Node classMember : node.getChildrenNodes()) {
          if (classMember instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) classMember;
            if (method.getName().equals(methodName)) {
              return Optional.of(method);
            }
          }
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Attempts to parse the given {@link MethodDeclaration} into the flowchart abstract syntax.
   *
   * @return The first {@link FlowchartNode} (wrapped in an {@link Optional})
   *    of the resulting AST if it exists; {@link Optional#empty()} otherwise.
   */
  public static Optional<FlowchartNode> parse(MethodDeclaration method)
      throws java.text.ParseException {

    //$ Get the top-level constructs of the method
    Iterator<Node> topLevelNodes = method.getBody().getChildrenNodes().iterator();

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<FlowchartNode> firstFlowchartNode =
        findFlowchartNode(topLevelNodes, Optional.empty());

    //? No such comment?
    if (!firstFlowchartNode.filter(node -> hasSingleExit(node)).isPresent()) {
      //X Finish prematurely
      return Optional.empty();
    }

    //% Save the flowchart node representing the found comment
    //> to return at the end of the function,
    //> then continue looking through the top-level constructs for appropriate comments

    Optional<FlowchartNode> currentNode = firstFlowchartNode;
    Optional<FlowchartNode> nextNode;

    //? Is there another appropriate top-level comment?
    while ((nextNode = findFlowchartNode(topLevelNodes, currentNode))
        .filter(node -> hasSingleExit(node)).isPresent()) {
      currentNode = nextNode;
    }

    //X Return the first flowchart node
    return firstFlowchartNode;
  }

  /**
   * Searches {@code nodes} for the first comment that can be parsed
   * into a {@link FlowchartNode}. Note that {@code nodes} is mutated
   * as a side effect of searching it.
   *
   * @return The first possible {@link FlowchartNode} (wrapped in an {@link Optional})
   *    of an appropriate {@link Comment} if one exists; {@link Optional#empty()} otherwise.
   */
  private static Optional<FlowchartNode> findFlowchartNode(
      Iterator<Node> nodes, Optional<FlowchartNode> currentNode)
          throws java.text.ParseException {

    //? Is there another node to consider?
    while (nodes.hasNext()) {

      //$ Attempt to extract a comment from the node
      Optional<Comment> comment = getComment(nodes.next());

      //? Failure?
      if (!comment.isPresent()) {
        continue;
      }

      //$ Attempt to parse the comment to a node in the flowchart abstract syntax
      Optional<FlowchartNode> flowchartNode = parseComment(comment.get(), currentNode);

      //? Success?
      if (flowchartNode.isPresent()) {
        //X Return the node
        return flowchartNode;
      }         
    }

    //X Indicate failure to find another comment that can be parsed
    //> into a node in the flowchart abstract syntax
    return Optional.empty();
  }

  /**
   * @return The {@link Comment} (wrapped in an {@link Optional}) associated with {@code node}
   *    (potentially itself) if one exists; {@link Optional#empty()} otherwise.
   */
  private static Optional<Comment> getComment(Node node) {
    Comment comment = null;
    if (node instanceof Comment) {
      comment = (Comment) node;           
    } else if (node.hasComment()) {
      comment = node.getComment();
    }
    return Optional.ofNullable(comment);
  }

  /**
   * Attempts to parse {@code comment} to an appropriate {@link FlowchartNode}.
   *
   * @return The {@link FlowchartNode} (wrapped in an {@link Optional})
   *    representing {@code comment} if {@code comment} can be parsed to one; 
   *    {@link Optional#empty()} otherwise.
   */
  private static Optional<FlowchartNode> parseComment(
      Comment comment, Optional<FlowchartNode> currentNode)
          throws java.text.ParseException {

    Optional<FlowchartNode> flowchartNode;
    String commentStr = comment.getContent();
    switch (commentStr.charAt(0)) {
      case '$':
        flowchartNode = Optional.of(new Process(commentStr.substring(1).trim()));
        currentNode.ifPresent(node -> setNextNode(node, flowchartNode.get()));
        break;
      case '?':
        flowchartNode = parseConditional(comment, currentNode);
        break;
      case 'X':
        flowchartNode = Optional.of(new Terminal(commentStr.substring(1).trim()));
        currentNode.ifPresent(node -> setNextNode(node, flowchartNode.get()));
        break;
      default:
        // Not a Codeviz comment
        flowchartNode = Optional.empty();
    }
    return flowchartNode;
  }

  private static void setNextNode(FlowchartNode currentNode, FlowchartNode nextNode) {
    if (currentNode instanceof SingleExitNode) {
      ((SingleExitNode) currentNode).setNextNode(nextNode);
    } else {
      ((Decision) currentNode).setFalseBranch(nextNode);
    }
  }

  private static Optional<FlowchartNode> parseConditional(
      Comment comment, Optional<FlowchartNode> currentNode)
          throws java.text.ParseException {

    Node commentedNode = comment.getCommentedNode();
    if (commentedNode instanceof IfStmt) {
      Decision ifFlowchartNode = new Decision(comment.getContent().substring(1).trim());
      currentNode.ifPresent(node -> setNextNode(node, ifFlowchartNode));
      IfStmt ifStmt = (IfStmt) commentedNode;
      FlowchartNode trueBranch = parseTrueBranch(ifStmt.getThenStmt(), ifFlowchartNode);
      Optional<FlowchartNode> falseBranch =
          parseFalseBranch(Optional.ofNullable(ifStmt.getElseStmt()), ifFlowchartNode);

      if ((!falseBranch.isPresent() || falseBranch.filter(node -> isTerminal(node)).isPresent())
          && isTerminal(trueBranch)) {
        return Optional.of(ifFlowchartNode);
      } else if (falseBranch.filter(node -> isTerminal(node)).isPresent()
          && hasSingleExit(trueBranch)) {
        return Optional.of(trueBranch);
      } else if (falseBranch.filter(node -> hasSingleExit(node)).isPresent()
          && isTerminal(trueBranch)) {
        return falseBranch;
      } else {
        Connector connector = new Connector();
        ifFlowchartNode.setFalseBranch(connector);
        setNextNode(trueBranch, connector);
        return Optional.of(connector);
      }
    }
    return Optional.empty();
  }

  private static FlowchartNode parseTrueBranch(Statement stmt, Decision conditional)
      throws java.text.ParseException {

    //$ Get the top-level constructs of the true branch
    Iterator<Node> topLevelNodes = stmt instanceof BlockStmt
        ? ((BlockStmt) stmt).getChildrenNodes().iterator()
        : Iterators.singletonIterator(stmt);

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<FlowchartNode> firstFlowchartNode =
        findFlowchartNode(topLevelNodes, Optional.empty());

    conditional.setTrueBranch(
        firstFlowchartNode.orElseThrow(
            () -> new java.text.ParseException(
                "No comments within indicated if statement.", stmt.getBeginLine())));

    return parseRestOfBranch(topLevelNodes, firstFlowchartNode.get());
  }

  private static Optional<FlowchartNode> parseFalseBranch(
      Optional<Statement> maybeStmt, Decision conditional)
          throws java.text.ParseException {

    if (!maybeStmt.isPresent()) {
      return Optional.empty();
    }
    Statement stmt = maybeStmt.get();

    //$ Get the top-level constructs of the true branch
    Iterator<Node> topLevelNodes = stmt instanceof BlockStmt
        ? ((BlockStmt) stmt).getChildrenNodes().iterator()
        : Iterators.singletonIterator(stmt);

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<FlowchartNode> firstFlowchartNode =
        findFlowchartNode(topLevelNodes, Optional.empty());

    //? No such comment?
    if (!firstFlowchartNode.isPresent()) {
      //X Finish prematurely
      return Optional.empty();
    }

    conditional.setFalseBranch(firstFlowchartNode.get());

    return Optional.of(parseRestOfBranch(topLevelNodes, firstFlowchartNode.get()));
  }

  private static FlowchartNode parseRestOfBranch(Iterator<Node> nodes, FlowchartNode firstNode)
      throws java.text.ParseException {

    Optional<FlowchartNode> currentNode = Optional.of(firstNode);
    Optional<FlowchartNode> nextNode;

    //? Is there another appropriate top-level comment?
    while ((nextNode = findFlowchartNode(nodes, currentNode))
        .filter(node -> hasSingleExit(node)).isPresent()) {
      currentNode = nextNode;
    }

    //X Return the last flowchart node
    return currentNode.get();
  }

  private static boolean isTerminal(FlowchartNode node) {
    return (node instanceof Terminal)
        || ((node instanceof Decision) && ((Decision) node).getFalseBranch().isPresent());
  }

  private static boolean hasSingleExit(FlowchartNode node) {
    return (node instanceof SingleExitNode)
        || ((node instanceof Decision) && !(((Decision) node).getFalseBranch().isPresent()));
  }
}
