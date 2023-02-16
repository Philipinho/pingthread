package com.litesoftwares.pingthread.service;

import com.litesoftwares.pingthread.utils.TwitterUtils;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.stream.StreamRules;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import twitter4j.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class TwitterStreamDigest {

    private static final TwitterClient twitterV2 = TwitterUtils.twitterV2Client();
    private final Twitter twitter = TwitterFactory.getSingleton();

    @Inject
    private ThreadService threadService;

    @Named("taskExecutor")
    @Inject
    private ThreadPoolTaskExecutor taskExecutor;

    private BlockingQueue<Status> queue = new ArrayBlockingQueue<>(200);
    @Value("${keyword.track}")
    private String keywordToTrack;

    @Value("${twitterProcessing.enabled}")
    private boolean twitterProcessing;

    private void run() {

        String botUsername = keywordToTrack;

        try{
            List<StreamRules.StreamRule> rules = twitterV2.retrieveFilteredStreamRules();

            if (rules == null){
                twitterV2.addFilteredStreamRule(botUsername, "");
            } else {
                for (StreamRules.StreamRule rule : rules) {
                    twitterV2.deleteFilteredStreamRuleId(rule.getId());
                }
                twitterV2.addFilteredStreamRule(botUsername, "");
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        twitterV2.startFilteredStream(new IAPIEventListener() {
            @Override
            public void onStreamError(int i, String s) {
                System.out.println("Stream error: " + s);
            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                // Twitter deprecated V1 filtered streams
                // For me not to mess the other parts of the code, I will only use v2 streams to get the tweet
                // then pass it to the v1 api to proceed as usual

                try {

                    Status v1Status = twitter.showStatus(Long.parseLong(tweet.getId()));

                    queue.offer(v1Status);
                } catch (TwitterException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onUnknownDataStreamed(String s) {

            }

            @Override
            public void onStreamEnded(Exception e) {

            }
        });
    }

    @PostConstruct
    public void afterPropertiesSet() {
        try {

            if(twitterProcessing) {
                for (int i = 0; i < taskExecutor.getMaxPoolSize(); i++) {
                    taskExecutor.execute(new TweetProcessor(threadService,queue));
                }
                run();
            }

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("What happened?");
        }
    }
}
