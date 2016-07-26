package com.github.codeviz.flowchart.common;

import com.google.common.collect.ImmutableMap;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;

/** Constants related to the OWL flowchart ontology. */
public final class Constants {

  public static final String FLOWCHART_ONTOLOGY_PATH = "file:src/main/owl/FlowchartModel.owl";

  public static final ImmutableMap<String, OntClass> FLOWCHART_CLASS_REFERENCES =
      enumerateFlowchartClassReferences();
  public static final ImmutableMap<String, OntProperty> FLOWCHART_PROPERTY_REFERENCES =
      enumerateFlowchartPropertyReferences();

  /**
   * The flowchart model's owl namespace and its local names.
   *
   * TODO: Consider putting these in a separate file
   * that is programatically generated/updated based on the OWL model.
   */

  public static final String FLOWCHART_ONTOLOGY_NAMESPACE =
      "https://github.com/JustisAllen/Codeviz#";

  // Classes
  public static final String CONNECTOR_LN = "Connector";
  public static final String DECISION_LN = "Decision";
  public static final String PROCESS_LN = "Process";
  public static final String SINGLE_EXIT_NODE_LN = "SingleExitNode";
  public static final String TERMINAL_LN = "Terminal";

  // Object Properties
  public static final String HAS_FALSE_BRANCH_LN = "hasFalseBranch";
  public static final String HAS_NEXT_NODE_LN = "hasNextNode";
  public static final String HAS_TRUE_BRANCH_LN = "hasTrueBranch";

  // Data Properties
  public static final String HAS_CONDITION_LN = "hasCondition";
  public static final String HAS_DESCRIPTION_LN = "hasDescription";


  public static final String LITERAL_LANG_TAG = "en";


  private static final OntModel flowchartModel = Utilities.createFlowchartModel();

  private static ImmutableMap<String, OntClass> enumerateFlowchartClassReferences() {
    ImmutableMap.Builder flowchartClassReferencesBuilder =
        ImmutableMap.<String, OntClass>builder();
    flowchartModel
        .listNamedClasses()
        .forEachRemaining(
            clazz -> flowchartClassReferencesBuilder.put(clazz.getLocalName(), clazz));
    return flowchartClassReferencesBuilder.build();
  }

  private static ImmutableMap<String, OntProperty> enumerateFlowchartPropertyReferences() {
    ImmutableMap.Builder flowchartPropertyReferencesBuilder =
        ImmutableMap.<String, OntProperty>builder();
    flowchartModel
        /**
         * This method returns properties outside of Codeviz's namespace.
         * If for any reason (i.e., a name colision) it is decided to only 'put' properties
         * that are inside Codeviz's namespace, consider using ExtendedIterator#filterKeep.
         */
        .listOntProperties()
        .forEachRemaining(
            property -> flowchartPropertyReferencesBuilder.put(property.getLocalName(), property));
    return flowchartPropertyReferencesBuilder.build();
  }

  // This class should not be instantiated since it is simply a collection of static constants.
  private Constants() {}
}
