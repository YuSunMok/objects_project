package com.objects.marketbridge.order.domain;

import com.objects.marketbridge.common.domain.BaseEntity;
import com.objects.marketbridge.common.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    private String orderName;

    private String orderNo;

    private Long totalUsedCouponPrice; // 총 사용된 쿠폰 금액

    private Long totalPrice; // 찐 최종 주문 금액

    private Long realPrice; // 쿠폰, 포인트사용 뺀 진짜 결제된 금액

    private Long usedPoint; // 구매하는데 사용한 포인트

    private String tid;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Builder
    public Order(Member member, Address address, String orderName, String orderNo, Long realPrice, Long totalPrice, Long totalUsedCouponPrice, Long usedPoint, String tid) {
        this.member = member;
        this.address = address;
        this.orderName = orderName;
        this.orderNo = orderNo;
        this.realPrice = realPrice;
        this.totalPrice = totalPrice;
        this.totalUsedCouponPrice = totalUsedCouponPrice;
        this.usedPoint = usedPoint;
        this.tid = tid;
    }

    public void addOrderDetail(OrderDetail orderDetail) {
        orderDetails.add(orderDetail);
        orderDetail.setOrder(this);
    }

    //== 비즈니스 로직==//
    public void cancelReturn(String reason, String statusCode) {
        orderDetails.forEach(orderDetail -> orderDetail.cancel(reason, statusCode));
    }

    public void returnCoupon() {
        orderDetails.forEach(OrderDetail::returnCoupon);
    }

    public void setTotalUsedCouponPrice(Long totalUsedCouponPrice) {
        this.totalUsedCouponPrice = totalUsedCouponPrice;
    }

    public static Order create(Member member, Address address, String orderName, String orderNo, Long totalPrice, Long realPrice, String tid){
        return Order.builder()
                .member(member)
                .address(address)
                .orderName(orderName)
                .orderNo(orderNo)
                .totalPrice(totalPrice)
                .realPrice(realPrice)
                .tid(tid)
                .build();
    }
}
