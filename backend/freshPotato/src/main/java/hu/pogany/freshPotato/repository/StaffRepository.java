package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
}
