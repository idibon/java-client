/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.io.*;
import java.util.Map;
import java.util.stream.*;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.JsonObject;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;
import com.idibon.api.util.Either;

import org.apache.commons.cli.*;

/**
 * Iterates through documents in a collection and predicts every document
 * against a task in parallel.
 *
 * Allows for command-line control of the number of parallel connections
 * to use, and limiting the number of documents to a subset of the
 * entire collection.
 */
public class BatchPredict {

    public static void main(String[] args) throws Exception {
        CommandLine options = parseCommandLine(args);
        if (options == null) return;

        IdibonAPI client = createClient(options);

        /* count the number of classification operations performed by this
         * application, to build a performance report at the end. */
        AtomicInteger predictions = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        try (Stream<DocumentContent> source = getSourceData(client, options)) {
            Task task = getClassificationTask(client, options);

            /* classify all of the documents in the source data using a
             * lambda forEach expression */
            task.classifications(source::iterator).forEach(result -> {
                if (result.isRight()) {
                    // successful classification, just increment the counter
                    predictions.incrementAndGet();
                } else {
                    /* classification failed for some reason; print out the
                     * failing document and throw the error */
                    printFailingDocument(result.left.request);
                    /* wrap in a runtime exception, since lambdas can't throw
                     * checked exceptions */
                    throw new RuntimeException("Failed", result.left.exception);
                }
            });

            // classification complete, print out performance
            double elapsed = (System.currentTimeMillis() - start) / 1000.0;
            double bandwidth = predictions.get() / elapsed;

            System.out.printf("Made %d predictions in %.2fs (%.2f/sec)\n",
                              predictions.get(), elapsed, bandwidth);
        } finally {
            client.shutdown(0);
        }
    }

    /**
     * When a prediction fails, prints out a description of the failing
     * document.
     */
    private static void printFailingDocument(DocumentContent requested) {
        String desc = null;
        try {
            desc = (requested instanceof DocumentContent.Named)
                ? ((DocumentContent.Named)requested).getName()
                : requested.getContent();
        } catch (Exception _) {
            // suppress any exceptions thrown by getContent
            desc = requested.toString();
        }

        System.err.printf("Classification failed for %s\n", desc);
    }

    /**
     * Returns the task instance that will perform classification, based on
     * the provided command-line parameters.
     */
    private static Task getClassificationTask(IdibonAPI api, CommandLine opts) {
        return api.collection(opts.getOptionValue("c"))
            .task(opts.getOptionValue("t"));
    }

    /**
     * Generates an iterable for all of the document that needs to be
     * classified, based on the provided command-line parameters.
     */
    private static Stream<DocumentContent> getSourceData(
            IdibonAPI api, CommandLine opts) throws Exception {

        if (opts.hasOption("i")) {
            /* read a file where each line represents document content */
            return readDocuments(opts.getOptionValue("i"));
        } else {
            /* classify documents stored in the collection; configure a
             * DocumentSearcher instance to provide the iteration */

            DocumentSearcher documents =
                api.collection(opts.getOptionValue("c")).documents();

            if (opts.hasOption("n"))
                documents.first(Integer.parseInt(opts.getOptionValue("n")));

            /* suppresses all errors that occur during document iteration,
             * and returns an iteration over the documents (mapped to
             * DocumentContent instances), rather than the Either<> instance */
            return StreamSupport.stream(documents.spliterator(), false)
                .filter(result -> result.isRight())
                .map(result -> (DocumentContent)result.right);
        }
    }

    /**
     * Reads a file of new-line separated document content, returning
     * a DocumentContent instance for each line.
     */
    private static Stream<DocumentContent> readDocuments(final String name)
            throws Exception {

        return Files.lines(new File(name).toPath(), Charset.forName("UTF-8"))
            .map(content -> {
                return new DocumentContent() {
                    public String getContent() { return content; }
                    public JsonObject getMetadata() { return null; }
                };
            });
    }

    /**
     * Returns an IdibonAPI client instance.
     */
    @SuppressWarnings("unchecked")
    private static IdibonAPI createClient(CommandLine opts)
            throws ParseException, java.net.MalformedURLException {
        // configure wire-level transport
        JdkHttpInterface http = new JdkHttpInterface()
            .forServer("https://api.idibon.com/")
            .withApiKey(opts.getOptionValue("k"));

        // configure the number of parallel connections to use, if specified
        if (opts.hasOption("p"))
            http.maxConnections(Integer.parseInt(opts.getOptionValue("p")));

        return new IdibonAPI().using(http);
    }

    /**
     * Parses the command line using Apache Commons CLI.
     */
    private static CommandLine parseCommandLine(String[] args) {
        Options cli = new Options();

        cli.addOption(Option.builder("c").required().longOpt("collection")
            .desc("Collection name").hasArg().type(String.class).build());

        cli.addOption(Option.builder("k").required().longOpt("key")
            .desc("API key").hasArg().type(String.class).build());

        cli.addOption(Option.builder("t").required().longOpt("task")
            .desc("Task name").hasArg().type(String.class).build());

        cli.addOption(Option.builder("p").longOpt("parallelism")
             .desc("Number of parallel connections").hasArg()
             .type(Integer.class).build());

        cli.addOption(Option.builder("n").longOpt("num_documents")
             .desc("Number of documents to classify").hasArg()
             .type(Integer.class).build());

        cli.addOption(Option.builder("i").longOpt("input_file")
             .desc("Input file of document content").hasArg()
             .type(String.class).build());

        cli.addOption(Option.builder("h").longOpt("help")
             .desc("Show help text").build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine options = parser.parse(cli, args);
            if (options.hasOption("h")) {
                printHelpText(cli);
                return null;
            } else {
                return options;
            }
        } catch (ParseException ex) {
            System.err.printf("Unable to parse command line: %s\n\n", ex);
            printHelpText(cli);
            return null;
        }
    }

    /**
     * Prints command-line help to the console
     */
    private static void printHelpText(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BatchPredict", "Demonstrates batch prediction",
                            options, "", true);
    }
}
