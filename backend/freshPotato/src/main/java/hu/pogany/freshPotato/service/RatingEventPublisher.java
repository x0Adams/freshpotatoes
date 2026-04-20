package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.RabbitMessagingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class RatingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RatingEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingConfig rabbitMessagingConfig;
    private final ObjectMapper objectMapper;

    public RatingEventPublisher(RabbitTemplate rabbitTemplate,
                                RabbitMessagingConfig rabbitMessagingConfig,
                                ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMessagingConfig = rabbitMessagingConfig;
        this.objectMapper = objectMapper;
    }

    public void publishRateChanged(int userId, int movieId, int rating) {
        try {
            String payload = objectMapper.writeValueAsString(new RatingEventPayload(userId, movieId, rating));
            rabbitTemplate.convertAndSend(rabbitMessagingConfig.ratingQueue(), payload);
        } catch (Exception ex) {
            log.error("Failed to publish rating event to queue={} userId={} movieId={} error={}",
                    rabbitMessagingConfig.ratingQueue(), userId, movieId, ex.getMessage());
        }
    }

    private record RatingEventPayload(int userid, int movieid, int rate) {
    }
}

