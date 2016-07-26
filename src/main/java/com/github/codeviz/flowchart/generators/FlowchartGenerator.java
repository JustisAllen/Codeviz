package com.github.codeviz.flowchart.generators;

import com.github.codeviz.flowchart.common.Constants;
import com.github.codeviz.flowchart.common.Utilities;
import org.anarres.graphviz.builder.GraphVizUtils;
import org.apache.jena.ontology.OntModel;
import java.io.File;

/** Generates a visual flowchart from a Codeviz OWL encoding. */
public final class FlowchartGenerator {

    private static final OntModel flowchartModel = Utilities.createFlowchartModel();

    public static void main(String[] args) {
        // Validate incoming model: Model#validate
    }
}
