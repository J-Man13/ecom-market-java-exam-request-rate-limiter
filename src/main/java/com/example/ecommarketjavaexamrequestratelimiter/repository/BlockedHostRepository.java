package com.example.ecommarketjavaexamrequestratelimiter.repository;

import com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis.BlockedHost;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BlockedHostRepository extends CrudRepository<BlockedHost, String> {
    Optional<BlockedHost> findByIp(String ip);
}
