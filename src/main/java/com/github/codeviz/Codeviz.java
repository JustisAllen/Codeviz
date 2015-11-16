package com.github.codeviz;

import com.github.codeviz.ir.Node;
import com.github.codeviz.ir.Process;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.io.FileInputStream;
import java.util.Iterator;

public class Codeviz {
    public static void main(String[] args) throws Exception {

        //$ Create an input stream for the file to be parsed
        FileInputStream in = new FileInputStream(
        	System.getProperty("user.dir") +
        	System.getProperty("file.separator") +
        	args[0]);

        CompilationUnit cu;
        try {
            //$ Parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        //$ Retrieve the comments
        Iterator<Comment> comments = cu.getComments().iterator();
        Node flowchart = null;

        //$ Translate the comments to the abstract flowchart syntax
        while (comments.hasNext()) {
        	String comment = comments.next().getContent();
        	if (comment.startsWith("$ ")) {
        		flowchart = new Process(comment.substring(2));
        		break;
        	}
        }

        if (flowchart == null) {
        	System.out.println("No comments to parse.");
        	System.exit(1);
        }

        Node currentNode = flowchart;
        while (comments.hasNext()) {
        	String comment = comments.next().getContent();
        	if (comment.startsWith("$ ")) {
        		Node nextNode = new Process(comment.substring(2));
        		currentNode.setNextNode(nextNode);
        		currentNode = nextNode;
        	}
        }

        //$ Print the resulting AST
        for (currentNode = flowchart; currentNode != null; currentNode = currentNode.getNextNode()) {
        	System.out.println(currentNode);
        }
    }
}
