package com.github.codeviz.flowchart.parsers;

import com.github.codeviz.flowchart.common.Constants;
import com.github.codeviz.flowchart.common.Utilities;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/** Parses a Java file into the Codeviz OWL representation of a flowchart. */
public final class JavaParser {

  private static final OntModel flowchartModel = Utilities.createFlowchartModel();

  public static void main(String[] args) throws IOException {    

    //$ Parse the input Java file into a {@link com.github.javaparser.JavaParser} AST
    CompilationUnit javaAst = null;
    try (FileInputStream inputFile = new FileInputStream(args[0] /* input file path */)) {
      javaAst = com.github.javaparser.JavaParser.parse(inputFile);
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
    Optional<Individual> abstractFlowchart = Optional.empty();
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

    //$ Write the OWL file based on the flowchart AST
    RDFDataMgr.write(
        new FileOutputStream("out.owl"), flowchartModel.getBaseModel(), RDFFormat.TURTLE_BLOCKS);
  }

  /**
   * @return The {@link MethodDeclaration} (wrapped in an {@link Optional}) in {@code javaAst}
   *    corresponding to the method with the specified {@code methodName}
   *    --{@link Optional#empty()} if a method with the name does not exist.
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
   * @return The {@link Individual} (wrapped in an {@link Optional})
   *    representing the first flowchart node of the resulting AST if it exists;
   *    {@link Optional#empty()} otherwise.
   */
  private static Optional<Individual> parse(MethodDeclaration method)
      throws java.text.ParseException {

    //$ Get the top-level constructs of the method
    Iterator<Node> topLevelNodes = method.getBody().getChildrenNodes().iterator();

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<Individual> firstFlowchartNode = findFlowchartNode(topLevelNodes, Optional.empty());

    //? No such comment?
    if (!firstFlowchartNode.isPresent()) {
      //X Finish prematurely
      return Optional.empty();
    }

    //% The flowchart node representing the found comment is saved
    //> to return at the end of the function

    Optional<Individual> currentNode = firstFlowchartNode;
    Optional<Individual> nextNode;

    //? Can we flow from the current node to another node,
    //> and is there another appropriate top-level comment
    //> that can be parsed to a node in the flowchart abstract syntax?
    while (currentNode.filter(node -> hasSingleExit(node)).isPresent()
        && (nextNode = findFlowchartNode(topLevelNodes, currentNode)).isPresent()) {
      
      //% The current node flows to the next node
      //$ Set this new node as the current node
      currentNode = nextNode;
    }

    //X Return the first flowchart node
    return firstFlowchartNode;
  }

  /**
   * Searches {@code nodes} for the first comment that can be parsed
   * into an {@link Individual} of the flowchart abstract syntax,
   * and if found, causes the {@code currentNode} to flow to the new node.
   *
   * @param nodes The iterator over the top-level nodes of a Java construct
   *      (e.g., method, if statement). The iterator is mutated as a side effect
   *      of searching it.
   * @param currentNode The node to which the found node is intended to flow.
   *      If present, this node must satisfy {@link #hasSingleExit(FlowchartNode)}.
   * @return The first {@link Individual} (wrapped in an {@link Optional})
   *    of an appropriate {@link Comment} if one exists; {@link Optional#empty()} otherwise.
   *
   * TODO: Optimize. Consider using a data structure to reduce/remove recursion.
   */
  private static Optional<Individual> findFlowchartNode(
      Iterator<Node> nodes, Optional<Individual> currentNode) throws java.text.ParseException {

    //? Is there another node to consider?
    while (nodes.hasNext()) {

      //$ Attempt to extract a comment from the node
      Optional<Comment> comment = getComment(nodes.next());

      //? Failure?
      if (!comment.isPresent()) {
        continue;
      }

      //$ Attempt to parse the comment to a node in the flowchart abstract syntax
      Optional<Individual> flowchartNode = parseComment(comment.get(), currentNode);

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
   * Returns the {@link Comment} (wrapped in an {@link Optional}) associated with {@code node}
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
   * Attempts to parse {@code comment} to an appropriate {@link Individual}
   * of the flowchart abstract syntax, and if successfully parsed,
   * causes the {@code currentNode} to flow to the new node.
   *
   * @param currentNode The node to which the found node is intended to flow.
   *      If present, this node must satisfy {@link #hasSingleExit(FlowchartNode)}.
   * @return The {@link Individual} (wrapped in an {@link Optional})
   *    representing {@code comment} if {@code comment} can be parsed to one; 
   *    {@link Optional#empty()} otherwise.
   */
  private static Optional<Individual> parseComment(
      Comment comment, Optional<Individual> currentNode) throws java.text.ParseException {

    Optional<Individual> flowchartNode;
    String commentStr = comment.getContent();
    switch (commentStr.charAt(0)) {
      case '$':
        Individual processNode = createFlowchartNode(Constants.PROCESS_LN);
        setProperty(processNode, Constants.HAS_DESCRIPTION_LN, commentStr.substring(1).trim());
        flowchartNode = Optional.of(processNode);
        currentNode.ifPresent(node -> setNextNode(node, flowchartNode.get()));
        break;
      case '?':
        flowchartNode = parseConditional(comment, currentNode);
        break;
      case 'X':
        Individual terminalNode = createFlowchartNode(Constants.TERMINAL_LN);
        setProperty(terminalNode, Constants.HAS_DESCRIPTION_LN, commentStr.substring(1).trim());
        flowchartNode = Optional.of(terminalNode);
        currentNode.ifPresent(node -> setNextNode(node, flowchartNode.get()));
        break;
      default:
        // Not a Codeviz comment
        flowchartNode = Optional.empty();
    }
    return flowchartNode;
  }

  /**
   * Attempts to parse {@code comment} to an appropriate {@code Decision},
   * and if successfully parsed, causes the {@code currentNode} to flow to the new node.
   */
  private static Optional<Individual> parseConditional(
      Comment comment, Optional<Individual> currentNode) throws java.text.ParseException {

    Node commentedNode = comment.getCommentedNode();

    //? Is the conditional an if statement?
    if (commentedNode instanceof IfStmt) {
      Individual ifFlowchartNode = createFlowchartNode(Constants.DECISION_LN);
      setProperty(
        ifFlowchartNode, Constants.HAS_CONDITION_LN, comment.getContent().substring(1).trim());
      currentNode.ifPresent(node -> setNextNode(node, ifFlowchartNode));
      IfStmt ifStmt = (IfStmt) commentedNode;
      Individual lastTrueBranchNode = parseTrueBranch(ifStmt.getThenStmt(), ifFlowchartNode);
      Optional<Individual> lastFalseBranchNode =
          parseFalseBranch(Optional.ofNullable(ifStmt.getElseStmt()), ifFlowchartNode);

      if ((!lastFalseBranchNode.isPresent()
              || lastFalseBranchNode.filter(node -> isTerminal(node)).isPresent())
          && isTerminal(lastTrueBranchNode)) {
        return Optional.of(ifFlowchartNode);
      } else if (lastFalseBranchNode.filter(node -> isTerminal(node)).isPresent()
          && hasSingleExit(lastTrueBranchNode)) {
        return Optional.of(lastTrueBranchNode);
      } else if (lastFalseBranchNode.filter(node -> hasSingleExit(node)).isPresent()
          && isTerminal(lastTrueBranchNode)) {
        return lastFalseBranchNode;
      } else {
        Individual connectorNode = createFlowchartNode(Constants.CONNECTOR_LN);
        setNextNode(lastTrueBranchNode, connectorNode);
        setNextNode(lastFalseBranchNode.get(), connectorNode);
        return Optional.of(connectorNode);
      }
    }

    //X Indicate failure to parse the conditional to nodes in the flowchart abstract syntax
    return Optional.empty();
  }

  private static Individual parseTrueBranch(Statement stmt, Individual decisionNode)
      throws java.text.ParseException {

    Preconditions.checkArgument(
        hasClass(decisionNode, Constants.DECISION_LN),
        "Argument is of class %s, but expected Decision",
        decisionNode.getOntClass(true).getLocalName());

    //$ Get the top-level constructs of the true branch
    Iterator<Node> topLevelNodes = stmt instanceof BlockStmt
        ? ((BlockStmt) stmt).getChildrenNodes().iterator()
        : Iterators.singletonIterator(stmt);

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<Individual> firstFlowchartNode =
        findFlowchartNode(topLevelNodes, Optional.empty());

    setProperty(
        decisionNode,
        Constants.HAS_TRUE_BRANCH_LN,
        firstFlowchartNode.orElseThrow(
            () -> new java.text.ParseException(
                "No comments within indicated if statement.", stmt.getBeginLine())));

    return parseRestOfBranch(topLevelNodes, firstFlowchartNode.get());
  }

  private static Optional<Individual> parseFalseBranch(
      Optional<Statement> maybeStmt, Individual decisionNode) throws java.text.ParseException {

    Preconditions.checkArgument(
        hasClass(decisionNode, Constants.DECISION_LN),
        "Argument is of class %s, but expected Decision",
        decisionNode.getOntClass(true).getLocalName());

    if (!maybeStmt.isPresent()) {
      return Optional.empty();
    }
    Statement stmt = maybeStmt.get();

    //$ Get the top-level constructs of the false branch
    Iterator<Node> topLevelNodes = stmt instanceof BlockStmt
        ? ((BlockStmt) stmt).getChildrenNodes().iterator()
        : Iterators.singletonIterator(stmt);

    //$ Find the first top-level comment that parses to a node in the flowchart abstract syntax
    Optional<Individual> firstFlowchartNode = findFlowchartNode(topLevelNodes, Optional.empty());

    //? No such comment?
    if (!firstFlowchartNode.isPresent()) {
      //X Finish prematurely
      return Optional.empty();
    }

    setProperty(decisionNode, Constants.HAS_FALSE_BRANCH_LN, firstFlowchartNode.get());
    return Optional.of(parseRestOfBranch(topLevelNodes, firstFlowchartNode.get()));
  }

  private static Individual parseRestOfBranch(Iterator<Node> nodes, Individual firstNode)
      throws java.text.ParseException {

    if (isTerminal(firstNode)) {
      return firstNode;
    }

    Optional<Individual> currentNode = Optional.of(firstNode);
    Optional<Individual> nextNode;

    //? Is there another appropriate top-level comment?
    while ((nextNode = findFlowchartNode(nodes, currentNode))
        .filter(node -> hasSingleExit(node)).isPresent()) {
      currentNode = nextNode;
    }

    //X Return the last flowchart node
    return currentNode.get();
  }

  /**
   * @param currentNode Must satisfy {@link #hasSingleExit(Individual)}.
   */
  private static void setNextNode(Individual currentNode, Individual nextNode) {
    setProperty(
        currentNode,
        hasClass(currentNode, Constants.SINGLE_EXIT_NODE_LN)
            ? Constants.HAS_NEXT_NODE_LN
            : Constants.HAS_FALSE_BRANCH_LN,
        nextNode);
  }

  private static boolean isTerminal(Individual flowchartNode) {
    return hasClass(flowchartNode, Constants.TERMINAL_LN)
        || (hasClass(flowchartNode, Constants.DECISION_LN)
            && hasProperty(flowchartNode, Constants.HAS_FALSE_BRANCH_LN));
  }

  private static boolean hasSingleExit(Individual flowchartNode) {
    return hasClass(flowchartNode, Constants.SINGLE_EXIT_NODE_LN)
        || (hasClass(flowchartNode, Constants.DECISION_LN)
            && !hasProperty(flowchartNode, Constants.HAS_FALSE_BRANCH_LN));
  }

  private static Individual createFlowchartNode(String classLocalName) {
    return Constants.FLOWCHART_CLASS_REFERENCES.get(classLocalName).createIndividual();
  }

  private static void setProperty(
      Individual flowchartNode, String propertyLocalName, String value) {

    flowchartNode.setPropertyValue(
        Constants.FLOWCHART_PROPERTY_REFERENCES.get(propertyLocalName),
        flowchartModel.createLiteral(value, Constants.LITERAL_LANG_TAG));
  }

  private static void setProperty(
      Individual thisNode, String propertyLocalName, Individual thatNode) {

    thisNode.setPropertyValue(
        Constants.FLOWCHART_PROPERTY_REFERENCES.get(propertyLocalName), thatNode);
  }

  /**
   * Decides whether {@code flowchartNode} is a member of the given class.
   */
  private static boolean hasClass(Individual flowchartNode, String classLocalName) {
    return flowchartNode.hasOntClass(Constants.FLOWCHART_CLASS_REFERENCES.get(classLocalName));
  }

  /**
   * Decides whether {@code flowchartNode} has the given property set.
   */
  private static boolean hasProperty(Individual flowchartNode, String propertyLocalName) {
    return flowchartNode.hasProperty(
        Constants.FLOWCHART_PROPERTY_REFERENCES.get(propertyLocalName));
  }
}
