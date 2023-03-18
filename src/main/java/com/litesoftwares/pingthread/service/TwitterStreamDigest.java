package com.litesoftwares.pingthread.service;

import com.litesoftwares.pingthread.utils.TwitterUtils;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.stream.StreamRules;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twitter4j.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Service
public class TwitterStreamDigest {

    private static final TwitterClient twitterV2 = TwitterUtils.twitterV2Client();
    private final Twitter twitter = TwitterFactory.getSingleton();

    @Inject
    private ThreadService threadService;

    @Value("${keyword.track}")
    private String keywordToTrack;

    @Value("${twitterProcessing.enabled}")
    private boolean twitterProcessing;

    private void run() {

        String botUsername = keywordToTrack;

        try {
            List<StreamRules.StreamRule> rules = twitterV2.retrieveFilteredStreamRules();

            if (rules == null) {
                twitterV2.addFilteredStreamRule(botUsername, "");
            } else {
                for (StreamRules.StreamRule rule : rules) {
                    if (!Objects.equals(rule.getValue(), botUsername)) {
                        twitterV2.deleteFilteredStreamRuleId(rule.getId());
                        twitterV2.addFilteredStreamRule(botUsername, "");
                    }
                }
            }

            System.out.println("Stream connected to: " + botUsername);


        } catch (Exception e) {
            e.printStackTrace();
        }

        twitterV2.startFilteredStream(new IAPIEventListener() {
            @Override
            public void onStreamError(int i, String s) {
                System.out.println("Stream error: " + s);
            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                System.out.println(tweet.getId());
                // Twitter deprecated V1 filtered streams
                // For us not to mess the other parts of the code, I will only use v2 streams to get the tweet
                // then pass it to the v1 api to proceed as usual
                try {
                    Status v1Status = twitter.showStatus(Long.parseLong(tweet.getId()));
                    threadService.processMentions(v1Status);
                } catch (Exception e) {
                    System.out.println("V2 Streaming error: " + e.getMessage());
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

            if (twitterProcessing) {
                run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("PostConstruct error");
        }
    }
}
