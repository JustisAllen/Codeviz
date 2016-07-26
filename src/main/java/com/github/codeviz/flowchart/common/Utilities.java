package com.github.codeviz.flowchart.common;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

/** Common functions for dealing with the OWL flowchart ontology. */
public final class Utilities {

  public static OntModel createFlowchartModel() {
    OntModel flowchartModel = ModelFactory.createOntologyModel();
    flowchartModel.read(Constants.FLOWCHART_ONTOLOGY_PATH, FileUtils.langTurtle);
    return flowchartModel;
  }

  // This class should not be instantiated since it is simply a collection of utility functions.
  private Utilities() {}
}
