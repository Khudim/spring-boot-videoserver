package com.khudim.controller;

import com.khudim.parser.IHtmlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@CrossOrigin
public class JobController {

    @Value("${controller.threads}")
    private int threadCount = 10;

    private final IHtmlParser parser;
    private ExecutorService executorService;

    @Autowired
    public JobController(IHtmlParser parser) {
        this.parser = parser;
    }

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    @GetMapping(value = "/download")
    public void download() {
        executorService.submit(parser::findVideos);
    }

}
