package com.objects.marketbridge.order.service.port;


import com.objects.marketbridge.order.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderQueryRepository {



    Optional<Order> findById(Long orderId);

    Order findByOrderNo(String orderNo);

    Order findByOrderNoWithMember(String orderNo);

    Order findByOrderNoWithOrderDetails(String orderNo);

    Order findByIdWithOrderDetailsAndProduct(Long orderId);

    Order findByOrderNoWithOrderDetailsAndProduct(String orderNo);

//    Optional<Order> findWithOrderDetailsAndProduct(Long orderId);


    Order findByIdWithOrderDetail(Long orderId);

    List<Order> findDistinctWithDetailsByMemberId(Long memberId);

    Order findByTid(String tid);

}