package com.rakuten.hackathon.data.feed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.rakuten.hackathon.data.feed.command.DataFeedCommand;

import picocli.CommandLine;

@SpringBootApplication
@ComponentScan({ "com.rakuten.hackathon" })
public class DataFeedApplication implements CommandLineRunner, ExitCodeGenerator {

    private DataFeedCommand dataFeedCommand;
    private int exitCode;
    
    // constructor injection
    DataFeedApplication(DataFeedCommand dataFeedCommand) {
        this.dataFeedCommand = dataFeedCommand;
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(dataFeedCommand).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        // let Spring instantiate and inject dependencies
        System.exit(SpringApplication.exit(SpringApplication.run(DataFeedApplication.class, args)));
    }

}