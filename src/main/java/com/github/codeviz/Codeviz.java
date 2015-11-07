package com.github.codeviz;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.io.FileInputStream;

public class Codeviz {
    public static void main(String[] args) throws Exception {

        // Create an input stream for the file to be parsed
        FileInputStream in = new FileInputStream(
        	System.getProperty("user.dir") +
        	System.getProperty("file.separator") +
        	args[0]);

        CompilationUnit cu;
        try {
            // Parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        // Print the comments within the file
        for (Comment comment : cu.getComments()) {
        	System.out.println(comment.getContent());
        }
    }
}
