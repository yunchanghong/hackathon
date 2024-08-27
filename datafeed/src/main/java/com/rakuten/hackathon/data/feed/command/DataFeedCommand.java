package com.rakuten.hackathon.data.feed.command;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rakuten.hackathon.data.feed.service.DataFeedService;

import picocli.CommandLine;

@Component
@CommandLine.Command(name = "dataFeedCommand", description = "data feed command")
public class DataFeedCommand implements Callable<Integer> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataFeedService dataFeedService;

    @Override
    public Integer call() throws Exception {
        try {
            dataFeedService.execute();
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return 1;
        }
        return 0;
    }
}
