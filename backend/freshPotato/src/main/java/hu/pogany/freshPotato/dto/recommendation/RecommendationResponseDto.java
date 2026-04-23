package hu.pogany.freshPotato.dto.recommendation;

import java.util.List;

public record RecommendationResponseDto(
        int user_id,
        int requested,
        List<Integer> movie_ids
) {
}

