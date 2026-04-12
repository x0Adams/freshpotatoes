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
        return genderRepository
                .findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("no gender with this name"));
    }
}
