package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.entity.Gender;
import hu.pogany.freshPotato.repository.GenderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GenderService {
    private final GenderRepository genderRepository;

    public GenderService(GenderRepository genderRepository) {
        this.genderRepository = genderRepository;
    }

    Gender findByName(String name) {
        Gender gender = genderRepository
                .findByName(name)
                .getFirst();

        if (gender == null)
            throw new EntityNotFoundException("No gender with this name");

        return gender;
    }
}
