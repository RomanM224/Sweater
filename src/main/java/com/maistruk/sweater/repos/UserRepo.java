package com.maistruk.sweater.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maistruk.sweater.domain.User;

public interface UserRepo extends JpaRepository<User, Long>{
    
    User findByUserName(String userName);

    User findByActivationCode(String code);

}
