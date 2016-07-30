package com.github.codeviz.flowchart.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

/** Common functions for dealing with the OWL flowchart ontology. */
public final class Utilities {

  public static OntModel createFlowchartModel() {
    return readTurtleOwlFile(
        ModelFactory.createOntologyModel(), Constants.FLOWCHART_ONTOLOGY_PATH);
  }

  public static OntModel readTurtleOwlFile(OntModel model, String filePath) {
    model.read(Constants.URI_FILE_PREFIX + filePath, FileUtils.langTurtle);
    return model;
  }

  /**
   * Returns the desired property if it exists in the ontology; otherwise, an exception is thrown.
   */
  public static OntProperty getProperty(String propertyLocalName) {
    return checkNotNull(
        flowchartModel.getOntProperty(Constants.FLOWCHART_ONTOLOGY_NAMESPACE + propertyLocalName),
        "The property %s does not exist in the ontology.",
        propertyLocalName);
  }

  /** Decides whether {@code flowchartNode} has the given property set. */
  public static boolean hasProperty(Individual flowchartNode, String propertyLocalName) {
    return flowchartNode.hasProperty(getProperty(propertyLocalName));
  }

  /**
   * A local reference to the flowchart ontology.
   *
   * Instances of {@link OntModel} are modified as concrete individuals are added,
   * so they are mutable and therefore not good candidates for true constants.
   * Ideally, we would maintain an immutable copy of the persistent ontology in {@link Constants},
   * but it's unclear to me how we might do that.
   */
  private static OntModel flowchartModel = createFlowchartModel();

  // This class should not be instantiated since it is simply a collection of utility functions.
  private Utilities() {}
}
