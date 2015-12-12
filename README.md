# Codeviz

## Motivation

Trying to understand someone else's code&mdash;especially
at a large scale&mdash;can be an intimidating, difficult,
and overwhelming task: having to distinguish keywords
and follow control flow in raw source code can be tiring,
even if the code is well-commented. Documentation can ease this process,
but the norm for documentation is text-based,
which isn't always the best or most accessible way to express an idea;
and there aren't many popular tools for making documentation that is more accessible.

Codeviz&mdash;short for "Code Visualizer"&mdash;addresses this problem
by providing a language that makes it easier for programmers
to create flowcharts that represent the control flow of their code,
offering a more visually- and spatially-appealing representation of code:
normally code-based algorithms are expressed in 1D, but with flowcharts,
these algorithms can be expressed in 2D.

### Similar Projects

Codeviz is _very_ similar to [Flowgen], which works for C++;
however, Codeviz distinguishes itself from Flowgen in its philosophy:
Codeviz is entirely opt-in oriented, and thus _only_ includes
conditional control flow structures, such as `if` statements,
in generated flowcharts if the user provides a description for the construct;
whereas, Flowgen includes _every_ `if` statement in its flowcharts,
causing C++'s syntax to leak into the diagram
when users do not provide a more human-readable description for the condition.


## Usage

Currently, Codeviz requires [Java 8], [Maven], and [DOT] to be installed.
To run Codeviz on a file, move the desired file into the `Codeviz` directory,
and run `mvn exec:java -Dexec.args="<input file>.java"`.
If everything runs smoothly, a file named `out.dot` should be generated.
To turn this DOT file into a graphical flowchart,
run `dot -T<output format> out.dot -o out.<output format>`.
For instance, if you want the output image to be a JPEG,
you would use the command `dot -Tjpg out.dot -o out.jpg`.
Noth that the input Codeviz comments in the input file
must be in the `main` method.


[DOT]: http://www.graphviz.org/content/dot-language
[Flowgen]: http://jlopezvi.github.io/Flowgen/index.html
[Java 8]: http://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html
[Maven]: https://maven.apache.org/index.html
