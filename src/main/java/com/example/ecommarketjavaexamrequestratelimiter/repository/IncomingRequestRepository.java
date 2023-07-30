package com.example.ecommarketjavaexamrequestratelimiter.repository;

import com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis.IncomingRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomingRequestRepository extends CrudRepository<IncomingRequest, String> {
    List<IncomingRequest> findByPathAndRequesterIp(String path, String requesterIp);

}
