package com.dslab.parking.service;

import com.dslab.parking.datastructures.DriverActionStack;
import com.dslab.parking.model.Driver;
import com.dslab.parking.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired private DriverRepository driverRepo;
    @Autowired private DriverActionStack actionStack;

    /** Returns the new driver_id, or throws if email is taken / fields missing. */
    public int register(String fullName, String email, String phoneNumber, String password) {
        if (fullName == null || email == null || phoneNumber == null || password == null
            || fullName.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Missing required fields");
        }
        if (driverRepo.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        int id = driverRepo.insert(fullName, email, phoneNumber, hash);

        actionStack.push(id, "REGISTER", "Registered with email " + email);
        return id;
    }

    /** Returns the matched driver, or empty if credentials are bad. */
    public Optional<Driver> login(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        Optional<Driver> opt = driverRepo.findByEmail(email);
        if (opt.isEmpty()) return Optional.empty();

        Driver d = opt.get();
        if (!BCrypt.checkpw(password, d.getPasswordHash())) {
            return Optional.empty();
        }

        actionStack.push(d.getDriverId(), "LOGIN", "Logged in as " + email);
        return opt;
    }
}
