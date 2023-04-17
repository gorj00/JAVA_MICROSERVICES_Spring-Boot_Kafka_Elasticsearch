package dev.gorj00.runner.impl;

import dev.gorj00.config.TwitterToKafkaServiceConfigData;
import dev.gorj00.listener.TwitterToKafkaStatusListener;
import dev.gorj00.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.*;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Component
public class TwitterKafkaStreamRunner implements StreamRunner {
    private static final Logger LOG =
            LoggerFactory.getLogger(TwitterKafkaStreamRunner.class);
    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;
    private final TwitterToKafkaStatusListener twitterToKafkaStatusListener;
    private TwitterStream twitterStream;

    public TwitterKafkaStreamRunner(TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData,
                                    TwitterToKafkaStatusListener twitterToKafkaStatusListener) {
        this.twitterToKafkaServiceConfigData = twitterToKafkaServiceConfigData;
        this.twitterToKafkaStatusListener = twitterToKafkaStatusListener;
    }

    @Override
    public void start() throws TwitterException {
        twitterStream = new TwitterStreamFactory().getInstance();
        Twitter twitter = new TwitterFactory().getInstance();
        try {
            User user = twitter.verifyCredentials();
            System.out.println("Authentication successful. User: " + user.getScreenName());
        } catch (TwitterException e) {
            System.out.println("Error: Authentication failed - " + e.getMessage());
        }
        twitterStream.addListener(twitterToKafkaStatusListener);
        addFilter();

    }

    private void addFilter() {
        String[] keywords = twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        LOG.info("Started filtering twitter stream for keywords {}",
                Arrays.toString(keywords));
    }

    @PreDestroy
    public void shutdown() {
        if (twitterStream != null) {
            LOG.info("Closing twitter stream!");
            twitterStream.shutdown();
        }
    }
}
