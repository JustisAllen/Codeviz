package com.github.codeviz;

import com.github.codeviz.ir.FlowchartNode;
import com.github.codeviz.ir.Process;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;

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
    } catch (IOException | ParseException e) {
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
    Optional<FlowchartNode> abstractFlowchart = parse(mainMethod.get());

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
  public static Optional<FlowchartNode> parse(MethodDeclaration method) {

    //$ Get the top-level constructs of the method
    Iterator<Node> topLevelNodes = method.getBody().getChildrenNodes().iterator();

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<FlowchartNode> firstFlowchartNode = findFlowchartNode(topLevelNodes);

    //? No such comment?
    if (!firstFlowchartNode.isPresent()) {
      //X Finish prematurely
      return Optional.empty();
    }

    //% Save the flowchart node representing the found comment
    //> to return at the end of the function,
    //> then continue looking through the top-level constructs for appropriate comments

    FlowchartNode currentNode = firstFlowchartNode.get();
    Optional<FlowchartNode> nextNode;

    //? Is there another appropriate top-level comment?
    while ((nextNode = findFlowchartNode(topLevelNodes)).isPresent()) {
      //$ Link the corresponding flowchart node to the previous node
      currentNode.setNextNode(nextNode);
      currentNode = nextNode.get();
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
  private static Optional<FlowchartNode> findFlowchartNode(Iterator<Node> nodes) {

    //? Is there another node to consider?
    while (nodes.hasNext()) {

      //$ Attempt to extract a comment from the node
      Optional<Comment> comment = getComment(nodes.next());

      //? Failure?
      if (!comment.isPresent()) {
        continue;
      }

      //$ Attempt to parse the comment to a node in the flowchart abstract syntax
      Optional<FlowchartNode> flowchartNode = parseComment(comment.get().getContent());

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
  private static Optional<FlowchartNode> parseComment(String comment) {
    FlowchartNode flowchartNode = null;
    if (comment.startsWith("$ ")) {
      flowchartNode = new Process(comment.substring(2));
    }
    return Optional.ofNullable(flowchartNode);
  }
}
