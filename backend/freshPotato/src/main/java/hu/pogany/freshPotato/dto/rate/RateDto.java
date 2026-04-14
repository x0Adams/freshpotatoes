package hu.pogany.freshPotato.dto.rate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class RateDto extends GenericRateDto<Integer> {


    public RateDto(int userId, int movieId, Integer rating) {
        super(userId, movieId, rating);
    }

    @Min(1)
    @Max(5)
    @Override
    public Integer rating() {
        return super.rating();
    }
}
