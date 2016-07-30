package com.github.codeviz.flowchart.generators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.codeviz.flowchart.common.Constants;
import com.github.codeviz.flowchart.common.Utilities;
import java.io.File;
import java.io.IOException;
import org.anarres.graphviz.builder.GraphVizGraph;
import org.anarres.graphviz.builder.GraphVizNode;
import org.anarres.graphviz.builder.GraphVizScope;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.ValidityReport;

/** Generates a Graphviz DOT file that represents the flowchart of a Codeviz OWL encoding. */
public class GraphvizDotGenerator {

  private static final OntModel flowchartModel = Utilities.createFlowchartModel();

  private static final GraphVizGraph flowchart = new GraphVizGraph();
  private static final GraphVizScope scope = new GraphVizScope.Impl();

  public static void main(String[] args) throws IOException {

    //$ Parse the input OWL flowchart encoding file into the flowchart model
    Utilities.readTurtleOwlFile(flowchartModel, args[0] /* input file path */);

    ValidityReport report = flowchartModel.validate();

    //? Is the encoding invalid?
    if (!report.isClean()) {
      //X Exit and report why the encoding is invalid
      System.out.println("The given abstract flowchart is invalid:");
      report.getReports().forEachRemaining(System.out::println);
      System.exit(1);
    }

    //$ Build the flowchart
    flowchartModel.listIndividuals().forEachRemaining(GraphvizDotGenerator::toGraphviz);

    //X Write the Graphviz DOT file based on the flowchart encoding
    flowchart.writeTo(new File("out.dot"));
  }

  private static void toGraphviz(Individual flowchartNode) {
    GraphVizNode thisGraphVizNode = flowchart.node(scope, flowchartNode.asResource());

    switch (flowchartNode.getOntClass(true).getLocalName()) {
      case Constants.CONNECTOR_LN:
        thisGraphVizNode.shape("circle").label("");
        break;
      case Constants.DECISION_LN:
        RDFNode condition =
            checkNotNull(
                flowchartNode.getPropertyValue(
                    Utilities.getProperty(Constants.HAS_CONDITION_LN)),
                "Decision node %s does not have required data property %s.",
                flowchartNode.getLocalName(),
                Constants.HAS_CONDITION_LN);

        thisGraphVizNode.shape("diamond").label(condition.asLiteral().getString());

        Resource trueBranch =
            checkNotNull(
                flowchartNode.getPropertyResourceValue(
                    Utilities.getProperty(Constants.HAS_TRUE_BRANCH_LN)),
                "Decision node %s does not have required object property %s.",
                flowchartNode.getLocalName(),
                Constants.HAS_TRUE_BRANCH_LN);

        flowchart.edge(scope, flowchartNode.asResource(), trueBranch);

        if (Utilities.hasProperty(flowchartNode, Constants.HAS_FALSE_BRANCH_LN)) {
          flowchart.edge(
              scope,
              flowchartNode.asResource(),
              flowchartNode.getPropertyResourceValue(
                  Utilities.getProperty(Constants.HAS_FALSE_BRANCH_LN)));
        }
        break;
      case Constants.PROCESS_LN:
      case Constants.TERMINAL_LN:
        String className = flowchartNode.getOntClass(true).getLocalName();
        RDFNode description = 
            checkNotNull(
                flowchartNode.getPropertyValue(
                    Utilities.getProperty(Constants.HAS_DESCRIPTION_LN)),
                "%s node %s does not have required data property %s.",
                className,
                flowchartNode.getLocalName(),
                Constants.HAS_DESCRIPTION_LN);

        thisGraphVizNode
            .shape(className.equals(Constants.PROCESS_LN) ? "box" : "oval")
            .label(description.asLiteral().getString());
        break;
      default:
        // Do nothing
    }

    if (Utilities.hasProperty(flowchartNode, Constants.HAS_NEXT_NODE_LN)) {
      flowchart.edge(
          scope,
          flowchartNode.asResource(),
          flowchartNode.getPropertyResourceValue(
              Utilities.getProperty(Constants.HAS_NEXT_NODE_LN)));
    }
  }
}
