package edu.wpi.checksims;

import edu.wpi.checksims.algorithm.AlgorithmRegistry;
import edu.wpi.checksims.algorithm.PlagiarismDetector;
import edu.wpi.checksims.algorithm.output.OutputRegistry;
import edu.wpi.checksims.algorithm.output.SimilarityMatrix;
import edu.wpi.checksims.algorithm.output.SimilarityMatrixPrinter;
import edu.wpi.checksims.algorithm.preprocessor.PreprocessSubmissions;
import edu.wpi.checksims.algorithm.preprocessor.PreprocessorRegistry;
import edu.wpi.checksims.algorithm.preprocessor.SubmissionPreprocessor;
import edu.wpi.checksims.util.file.FileStringWriter;
import edu.wpi.checksims.util.token.FileTokenizer;
import edu.wpi.checksims.util.token.TokenType;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Entry point for Checksims
 */
public class ChecksimRunner {
    public static void main(String[] args) throws IOException {
        // TODO should split CLI handling into separate function and add unit tests

        // CLI Argument Handling
        Options opts = new Options();
        Parser parser = new GnuParser();
        CommandLine cli = null;

        Option alg = new Option("a", "algorithm", true, "algorithm to use");
        Option token = new Option("t", "token", true, "tokenization type to use");
        Option out = new Option("o", "output", true, "output format");
        Option file = new Option("f", "file", true, "file to output to");
        Option preprocess = new Option("p", "preprocess", true, "preprocessors to apply");
        Option jobs = new Option("j", "jobs", true, "number of threads to use");
        Option verbose = new Option("v", "verbose", false, "specify verbose output");
        Option help = new Option("h", "help", false, "show usage information");

        opts.addOption(alg);
        opts.addOption(token);
        opts.addOption(out);
        opts.addOption(file);
        opts.addOption(preprocess);
        opts.addOption(jobs);
        opts.addOption(verbose);
        opts.addOption(help);

        // Parse the CLI args
        try {
            cli = parser.parse(opts, args);
        } catch (ParseException e) {
            System.err.println("Error parsing CLI arguments: " + e.getMessage());
            System.exit(-1);
        }

        // The help!
        if(cli.hasOption("h")) {
            HelpFormatter f = new HelpFormatter();
            PrintWriter systemErr = new PrintWriter(System.err, true);

            f.printHelp(systemErr, 80, "checksims [args] glob directory [directory2 ...]", "checksims: check similarity of student submissions", opts, 2, 4, "");

            System.exit(0);
        }

        // Get unconsumed arguments
        String[] unusedArgs = cli.getArgs();

        if(unusedArgs.length < 2) {
            System.err.println("Expecting at least two arguments: File match glob, and folder(s) to check");
            System.exit(-1);
        }

        // First non-flag argument is the glob matcher
        // All the rest are directories containing student submissions
        String glob = unusedArgs[0];
        List<File> submissionDirs = new LinkedList<>();

        System.out.println("Got glob matcher as " + glob);

        for(int i = 1; i < unusedArgs.length; i++) {
            System.out.println("Adding directory " + args[i]);
            submissionDirs.add(new File(unusedArgs[i]));
        }

        // Parse plagiarism detection algorithm
        PlagiarismDetector algorithm = null;
        String algorithmName = cli.getOptionValue("a");
        if(algorithmName == null) {
            algorithm = AlgorithmRegistry.getInstance().getDefaultAlgorithm();
        } else {
            try {
                algorithm = AlgorithmRegistry.getInstance().getAlgorithmInstance(algorithmName);
            } catch(ChecksimException e) {
                System.err.println("Error getting algorithm: " + e.getMessage());
                System.exit(-1);
            }
        }

        String stringTokenization = cli.getOptionValue("t");
        TokenType tokenization = null;
        if(stringTokenization != null) {
            try {
                tokenization = TokenType.fromString(stringTokenization);
            } catch(ChecksimException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        } else {
            // If the user didn't specify, use the algorithm's default tokenization
            tokenization = algorithm.getDefaultTokenType();
        }

        // Parse file output value
        boolean outputToFile = false;
        String outputFile = cli.getOptionValue("f");
        File outputFileAsFile = null;
        if(outputFile != null) {
            outputToFile = true;
            outputFileAsFile = new File(outputFile);
            System.out.println("Outputting to file " + outputFileAsFile.getName());
        }

        String numJobs = cli.getOptionValue("j");
        if(numJobs != null) {
            int threads = Integer.parseInt(numJobs);

            if(threads < 1) {
                System.err.println("Must specify positive number of threads, instead got " + threads);
                System.exit(-1);
            }

            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "" + threads);
        }

        boolean verboseLogging = false;
        // Parse verbose setting
        if(cli.hasOption("v")) {
            verboseLogging = true;
        }

        List<SubmissionPreprocessor> preprocessors = new LinkedList<>();
        String allPreprocessors = cli.getOptionValue("p");
        if(allPreprocessors != null) {
            String[] splitPreprocessors = allPreprocessors.split(",");
            try {
                for (String s : splitPreprocessors) {
                    SubmissionPreprocessor p = PreprocessorRegistry.getInstance().getPreprocessor(s);
                    preprocessors.add(p);
                }
            } catch(ChecksimException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }

        SimilarityMatrixPrinter outputPrinter = null;
        String similarityMatrixPrinterString = cli.getOptionValue("o");
        if(similarityMatrixPrinterString == null) {
            outputPrinter = OutputRegistry.getInstance().getDefaultStrategy();
        } else {
            try {
                outputPrinter = OutputRegistry.getInstance().getOutputStrategy(similarityMatrixPrinterString);
            } catch(ChecksimException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }

        ChecksimConfig config = new ChecksimConfig(algorithm, tokenization, preprocessors, submissionDirs, glob,
                verboseLogging, outputPrinter, outputToFile, outputFileAsFile);

        runChecksims(config);

        System.exit(0);
    }

    public static void runChecksims(ChecksimConfig config) {
        FileTokenizer tokenizer = FileTokenizer.getTokenizer(config.tokenization);

        List<Submission> submissions = new LinkedList<>();

        try {
            for (File f : config.submissionDirectories) {
                submissions.addAll(Submission.submissionsFromDir(f, config.globMatcher, tokenizer));
            }
        } catch(IOException e) {
            System.err.println("Error creating submissions from directory: " + e.getMessage());
            System.exit(-1);
        }

        // Apply all preprocessors
        for(SubmissionPreprocessor p : config.preprocessors) {
            submissions = PreprocessSubmissions.process(p::process, submissions);
        }

        // Apply algorithm to submission
        SimilarityMatrix results = SimilarityMatrix.generate(submissions, config.algorithm);

        String output = config.outputPrinter.printMatrix(results);

        if(config.outputToFile) {
            try {
                FileStringWriter.writeStringToFile(config.outputFile, output);
            } catch (IOException e) {
                System.err.println("Error printing output to file: " + e.getMessage());
                System.exit(-1);
            }
        } else {
            System.out.println(output);
        }
    }
}
