package com.github.codeviz.flowchart.common;

/** Constants related to the OWL flowchart ontology. */
public final class Constants {

  public static final String URI_FILE_PREFIX = "file:";
  public static final String FLOWCHART_ONTOLOGY_PATH = "src/main/owl/FlowchartModel.owl";

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

  // This class should not be instantiated since it is simply a collection of static constants.
  private Constants() {}
}
