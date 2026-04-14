package hu.pogany.freshPotato.dto.rate;

public class ReviewDto extends GenericRateDto<String>{
    public ReviewDto(int userId, int movieId, String rating) {
        super(userId, movieId, rating);
    }
}
