package com.vertigo.usersmanager.repository;


import com.vertigo.usersmanager.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<OurUsers,Integer> {

Optional<OurUsers> findByEmail(String email);
}
