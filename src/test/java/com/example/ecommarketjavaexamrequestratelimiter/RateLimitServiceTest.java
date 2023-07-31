package com.example.ecommarketjavaexamrequestratelimiter;

import com.example.ecommarketjavaexamrequestratelimiter.exception.RateLimitExceededException;
import com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis.BlockedHost;
import com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis.IncomingRequest;
import com.example.ecommarketjavaexamrequestratelimiter.repository.BlockedHostRepository;
import com.example.ecommarketjavaexamrequestratelimiter.repository.IncomingRequestRepository;
import com.example.ecommarketjavaexamrequestratelimiter.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SpringBootTest
public class RateLimitServiceTest {
    @Autowired
    private RateLimitService rateLimitService;
    @MockBean
    private IncomingRequestRepository incomingRequestRepository;
    @MockBean
    private BlockedHostRepository blockedHostRepository;
    @Value("${rate.limit.period.minutes}")
    private long rateLimitPeriodMinutes;
    @Value("${host.block.period.minutes}")
    private long blockHostPeriodMinutes;
    @Value("${rate.limit}")
    private long rateLimit;
    private final String dummyPath = "af7c1fe6-d669-414e-b066-e9733f0de7a8";
    private final String dummyRequestIp = "08c71152-c552-42e7-b094-f510ff44e9cb";

    @Test
    public void saveIncomingRequestFirstlyOccurredTest(){
        IncomingRequest incomingRequest = new IncomingRequest(dummyPath, dummyRequestIp);
        IncomingRequest incomingRequestSpy = spy(incomingRequest);
        when(blockedHostRepository.findByIp(incomingRequestSpy.getRequesterIp())).thenReturn(Optional.empty());
        when(incomingRequestRepository.findByPathAndRequesterIp(incomingRequestSpy.getPath(), incomingRequestSpy.getRequesterIp()))
                .thenReturn(emptyList());

        rateLimitService.saveIncomingRequest(incomingRequest);

        verify(blockedHostRepository,times(1)).findByIp(incomingRequestSpy.getRequesterIp());
        verify(incomingRequestRepository,times(1)).findByPathAndRequesterIp(incomingRequestSpy.getPath(), incomingRequestSpy.getRequesterIp());
        verifyNoMoreInteractions(blockedHostRepository);
        assertThat(incomingRequest.getPath()).isEqualTo(dummyPath);
        assertThat(incomingRequest.getRequesterIp()).isEqualTo(dummyRequestIp);
        assertThat(incomingRequest.getTtlInMinutes()).isEqualTo(rateLimitPeriodMinutes);
        assertThat(incomingRequest.getCreated()).isNotNull();
        verify(incomingRequestRepository,times(1)).save(incomingRequest);
        verifyNoMoreInteractions(incomingRequestRepository);
    }

    @Test
    public void saveIncomingRequestBlockedHostRateLimitExceededExceptionTest(){
        IncomingRequest incomingRequest = new IncomingRequest(dummyPath, dummyRequestIp);
        IncomingRequest incomingRequestSpy = spy(incomingRequest);
        when(blockedHostRepository.findByIp(incomingRequestSpy.getRequesterIp())).thenReturn(Optional.of(new BlockedHost()));

        assertThrows(RateLimitExceededException.class, () -> rateLimitService.saveIncomingRequest(incomingRequest));

        verify(blockedHostRepository,times(1)).findByIp(incomingRequestSpy.getRequesterIp());
        verifyNoMoreInteractions(blockedHostRepository);
        verifyNoMoreInteractions(incomingRequestRepository);
    }

    @Test
    public void saveIncomingRequestBlockHostTest(){
        IncomingRequest incomingRequest = new IncomingRequest(dummyPath, dummyRequestIp);
        IncomingRequest incomingRequestSpy = spy(incomingRequest);
        when(blockedHostRepository.findByIp(incomingRequestSpy.getRequesterIp())).thenReturn(Optional.empty());
        List<IncomingRequest> previouslyRegisteredRequests = new LinkedList<>();
        for (int i = 0; i < rateLimit; i++) {
            IncomingRequest previouslyRegisteredRequest = new IncomingRequest(dummyPath,dummyRequestIp);
            previouslyRegisteredRequest.setCreated(LocalDateTime.now().minusSeconds(1));
            previouslyRegisteredRequest.setTtlInMinutes(rateLimitPeriodMinutes);
            previouslyRegisteredRequests.add(previouslyRegisteredRequest);
        }
        assertThat(previouslyRegisteredRequests.size()).isEqualTo(rateLimit);
        when(incomingRequestRepository.findByPathAndRequesterIp(incomingRequestSpy.getPath(), incomingRequestSpy.getRequesterIp()))
                .thenReturn(previouslyRegisteredRequests);

        rateLimitService.saveIncomingRequest(incomingRequest);

        verify(blockedHostRepository,times(1)).findByIp(incomingRequestSpy.getRequesterIp());
        verify(incomingRequestRepository,times(1)).findByPathAndRequesterIp(incomingRequestSpy.getPath(), incomingRequestSpy.getRequesterIp());
        final ArgumentCaptor<BlockedHost> blockedHostArgumentCaptor = ArgumentCaptor.forClass(BlockedHost.class);
        verify(blockedHostRepository,times(1)).save(blockedHostArgumentCaptor.capture());
        final BlockedHost blockedHost = blockedHostArgumentCaptor.getValue();
        assertThat(blockedHost.getIp()).isEqualTo(dummyRequestIp);
        assertThat(blockedHost.getTtlInMinutes()).isEqualTo(blockHostPeriodMinutes);
        verifyNoMoreInteractions(blockedHostRepository);
        assertThat(incomingRequest.getPath()).isEqualTo(dummyPath);
        assertThat(incomingRequest.getRequesterIp()).isEqualTo(dummyRequestIp);
        assertThat(incomingRequest.getTtlInMinutes()).isEqualTo(rateLimitPeriodMinutes);
        assertThat(incomingRequest.getCreated()).isNotNull();
        verify(incomingRequestRepository,times(1)).save(incomingRequest);
        verifyNoMoreInteractions(incomingRequestRepository);
    }

}
