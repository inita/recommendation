package com.redhat.developer.demos.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Random;

@RestController
public class RecommendationController {

    private static final String RESPONSE_STRING_FORMAT = "recommendation v1 from '%s': %d";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Counter to help us see the lifecycle
     */
    private int count = 0;

    /**
     * Flag for throwing a 503 when enabled
     */
    private boolean misbehave = false;

    private static final String HOSTNAME = parseContainerIdFromHostname(
            System.getenv().getOrDefault("HOSTNAME", "unknown"));

    static String parseContainerIdFromHostname(String hostname) {
        return hostname.replaceAll("recommendation-v\\d+-", "");
    }

    // SB 1.5.X actuator does not allow subpaths on custom health checks URL/do in easy way
    @RequestMapping("/health/ready")
    @ResponseStatus(HttpStatus.OK)
    public void ready() {}

    // SB 1.5.X actuator does not allow subpaths on custom health checks URL/do in
    // easy way
    @RequestMapping("/health/live")
    @ResponseStatus(HttpStatus.OK)
    public void live() {}

    @RequestMapping("/")
    public ResponseEntity<Recommendation> getRecommendations() {
        count++;
        logger.debug(String.format("recommendation request from %s: %d", HOSTNAME, count));

        timeout();

        logger.debug("recommendation service ready to return");
        if (misbehave) {
            return doMisbehavior();
        }

        Recommendation r = new Recommendation();
        Random rand = new Random();
        Integer id = rand.nextInt(1000000);
        r.setId(id);
        r.setComment(String.format(RecommendationController.RESPONSE_STRING_FORMAT, HOSTNAME, count));
        boolean isOk = id % 2 == 0 ? Boolean.TRUE: Boolean.FALSE;
        r.setOk(isOk);

        return ResponseEntity.ok(r);
    }

    private void timeout() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.info("Thread interrupted");
        }
    }

    private ResponseEntity<Recommendation> doMisbehavior() {
        logger.debug(String.format("Misbehaving %d", count));
        Recommendation r = new Recommendation();
        r.setComment(String.format("recommendation misbehavior from '%s'\n", HOSTNAME));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(r);
    }

    @RequestMapping("/misbehave")
    public ResponseEntity<String> flagMisbehave() {
        this.misbehave = true;
        logger.debug("'misbehave' has been set to 'true'");
        return ResponseEntity.ok("Next request to / will return a 503\n");
    }

    @RequestMapping("/behave")
    public ResponseEntity<String> flagBehave() {
        this.misbehave = false;
        logger.debug("'misbehave' has been set to 'false'");
        return ResponseEntity.ok("Next request to / will return 200\n");
    }

}
