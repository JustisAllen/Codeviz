package com.github.codeviz;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.io.InputStream;

public class CommentParsingTest {
    public static void main(String[] args) throws Exception {

        // Create an input stream for the file to be parsed
        InputStream in = CommentParsingTest.class.getResourceAsStream("CommentParsingTest.java");

        CompilationUnit cu;
        try {
            // Parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        // Print the comments within the file
        for (Comment comment : cu.getComments()) {
        	System.out.print(comment);
        }
    }
}
