package com.example.ecommarketjavaexamrequestratelimiter;

import com.example.ecommarketjavaexamrequestratelimiter.exception.RateLimitExceededException;
import com.example.ecommarketjavaexamrequestratelimiter.model.BlockedHost;
import com.example.ecommarketjavaexamrequestratelimiter.model.IncomingRequest;
import com.example.ecommarketjavaexamrequestratelimiter.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@SpringBootTest
public class RateLimitServiceTest {
    @Autowired
    private RateLimitService rateLimitService;

    @Value("${rate.limit.period.minutes}")
    private long rateLimitPeriodMinutes;
    @Value("${host.block.period.minutes}")
    private long blockHostPeriodMinutes;
    @Value("${rate.limit}")
    private long rateLimit;
    private final String dummyPath = "af7c1fe6-d669-414e-b066-e9733f0de7a8";
    private final String dummyRequestIp = "08c71152-c552-42e7-b094-f510ff44e9cb";

    @Test
    public void saveIncomingRequestFirstlyOccurredTest() {
        IncomingRequest incomingRequest = new IncomingRequest(dummyPath, dummyRequestIp);
        IncomingRequest incomingRequestSpy = spy(incomingRequest);
        ConcurrentHashMap<String, BlockedHost> ipBlockedHostMapMock = mock(ConcurrentHashMap.class);
        ReflectionTestUtils.setField(rateLimitService, "ipBlockedHostMap", ipBlockedHostMapMock);
        when(ipBlockedHostMapMock.containsKey(incomingRequestSpy.getRequesterIp())).thenReturn(false);
        ConcurrentLinkedQueue<IncomingRequest> incomingRequestsMock = mock(ConcurrentLinkedQueue.class);
        ReflectionTestUtils.setField(rateLimitService, "incomingRequests", incomingRequestsMock);
        when(incomingRequestsMock.stream()).thenReturn(Stream.empty());

        rateLimitService.saveIncomingRequest(incomingRequest);

        verify(ipBlockedHostMapMock, times(1)).containsKey(incomingRequestSpy.getRequesterIp());
        verify(incomingRequestsMock, times(1)).stream();
        verifyNoMoreInteractions(ipBlockedHostMapMock);
        assertThat(incomingRequest.getPath()).isEqualTo(dummyPath);
        assertThat(incomingRequest.getRequesterIp()).isEqualTo(dummyRequestIp);
        assertThat(incomingRequest.getCreated()).isNotNull();
        verify(incomingRequestsMock, times(1)).add(incomingRequest);
        verifyNoMoreInteractions(incomingRequestsMock);
    }

    @Test
    public void saveIncomingRequestBlockedHostRateLimitExceededExceptionTest() {
        IncomingRequest incomingRequest = new IncomingRequest(dummyPath, dummyRequestIp);
        IncomingRequest incomingRequestSpy = spy(incomingRequest);
        ConcurrentHashMap<String, BlockedHost> ipBlockedHostMapMock = mock(ConcurrentHashMap.class);
        ReflectionTestUtils.setField(rateLimitService, "ipBlockedHostMap", ipBlockedHostMapMock);
        when(ipBlockedHostMapMock.containsKey(incomingRequestSpy.getRequesterIp())).thenReturn(true);
        ConcurrentLinkedQueue<IncomingRequest> incomingRequestsMock = mock(ConcurrentLinkedQueue.class);
        ReflectionTestUtils.setField(rateLimitService, "incomingRequests", incomingRequestsMock);

        assertThrows(RateLimitExceededException.class, () -> rateLimitService.saveIncomingRequest(incomingRequest));

        verify(ipBlockedHostMapMock, times(1)).containsKey(incomingRequestSpy.getRequesterIp());
        verifyNoMoreInteractions(ipBlockedHostMapMock);
        verifyNoInteractions(incomingRequestsMock);
    }

    @Test
    public void saveIncomingRequestBlockHostTest() {
        IncomingRequest incomingRequest = new IncomingRequest(dummyPath, dummyRequestIp);
        IncomingRequest incomingRequestSpy = spy(incomingRequest);
        ConcurrentHashMap<String, BlockedHost> ipBlockedHostMapMock = mock(ConcurrentHashMap.class);
        ReflectionTestUtils.setField(rateLimitService, "ipBlockedHostMap", ipBlockedHostMapMock);
        when(ipBlockedHostMapMock.containsKey(incomingRequestSpy.getRequesterIp())).thenReturn(false);
        List<IncomingRequest> previouslyRegisteredRequests = new LinkedList<>();
        for (int i = 0; i < rateLimit; i++) {
            IncomingRequest previouslyRegisteredRequest = new IncomingRequest(dummyPath, dummyRequestIp);
            previouslyRegisteredRequest.setCreated(LocalDateTime.now().minusSeconds(1));
            previouslyRegisteredRequests.add(previouslyRegisteredRequest);
        }
        assertThat(previouslyRegisteredRequests.size()).isEqualTo(rateLimit);
        ConcurrentLinkedQueue<IncomingRequest> incomingRequestsMock = mock(ConcurrentLinkedQueue.class);
        ReflectionTestUtils.setField(rateLimitService, "incomingRequests", incomingRequestsMock);
        when(incomingRequestsMock.stream()).thenReturn(previouslyRegisteredRequests.stream());

        rateLimitService.saveIncomingRequest(incomingRequest);

        verify(ipBlockedHostMapMock, times(1)).containsKey(incomingRequestSpy.getRequesterIp());
        verify(incomingRequestsMock, times(1)).stream();
        ArgumentCaptor<String> requesterIpArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BlockedHost> blockedHostArgumentCaptor = ArgumentCaptor.forClass(BlockedHost.class);
        verify(ipBlockedHostMapMock, times(1)).put(requesterIpArgumentCaptor.capture(), blockedHostArgumentCaptor.capture());
        String requesterIp = requesterIpArgumentCaptor.getValue();
        BlockedHost blockedHost = blockedHostArgumentCaptor.getValue();
        assertThat(requesterIp).isEqualTo(dummyRequestIp);
        assertThat(blockedHost.getIp()).isEqualTo(dummyRequestIp);
        verifyNoMoreInteractions(ipBlockedHostMapMock);
        assertThat(incomingRequest.getPath()).isEqualTo(dummyPath);
        assertThat(incomingRequest.getRequesterIp()).isEqualTo(dummyRequestIp);
        assertThat(incomingRequest.getCreated()).isNotNull();
        verify(incomingRequestsMock, times(1)).add(incomingRequest);
        verifyNoMoreInteractions(incomingRequestsMock);
    }

}
