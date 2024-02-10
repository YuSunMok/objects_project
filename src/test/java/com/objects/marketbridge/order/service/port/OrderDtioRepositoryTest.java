package com.objects.marketbridge.order.service.port;

import com.objects.marketbridge.member.domain.Address;
import com.objects.marketbridge.member.domain.AddressValue;
import com.objects.marketbridge.member.domain.Member;
import com.objects.marketbridge.member.service.port.MemberRepository;
import com.objects.marketbridge.order.controller.dto.select.GetOrderHttp;
import com.objects.marketbridge.order.domain.Order;
import com.objects.marketbridge.order.domain.OrderDetail;
import com.objects.marketbridge.order.infra.dtio.GetCancelReturnListDtio;
import com.objects.marketbridge.order.infra.dtio.OrderDtio;
import com.objects.marketbridge.payment.domain.CardInfo;
import com.objects.marketbridge.payment.domain.Payment;
import com.objects.marketbridge.product.domain.Product;
import com.objects.marketbridge.product.infra.coupon.CouponRepository;
import com.objects.marketbridge.product.infra.product.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.objects.marketbridge.order.domain.StatusCodeType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@Slf4j
class OrderDtioRepositoryTest {

    @Autowired
    OrderCommendRepository orderCommendRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    OrderDtoRepository orderDtoRepository;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("유저가 반품, 취소한 상품들을 조회할 수 있다.")
    public void findOrdersByMemberId() {
        // given
        Member member = Member.builder().build();

        Order order1 = Order.builder()
                .member(member)
                .orderNo("123")
                .build();

        Order order2 = Order.builder()
                .member(member)
                .orderNo("456")
                .build();

        Product product1 = Product.builder()
                .productNo("1")
                .name("옷")
                .price(1000L)
                .build();
        Product product2 = Product.builder()
                .productNo("2")
                .name("신발")
                .price(2000L)
                .build();
        Product product3 = Product.builder()
                .productNo("3")
                .name("바지")
                .price(3000L)
                .build();

        OrderDetail orderDetail1 = OrderDetail.builder()
                .order(order1)
                .product(product1)
                .quantity(1L)
                .orderNo(order1.getOrderNo())
                .statusCode(RETURN_COMPLETED.getCode())
                .build();
        OrderDetail orderDetail2 = OrderDetail.builder()
                .order(order1)
                .product(product2)
                .quantity(2L)
                .orderNo(order1.getOrderNo())
                .statusCode(ORDER_CANCEL.getCode())
                .build();
        OrderDetail orderDetail3 = OrderDetail.builder()
                .order(order2)
                .product(product3)
                .quantity(3L)
                .orderNo(order2.getOrderNo())
                .statusCode(ORDER_CANCEL.getCode())
                .build();
        OrderDetail orderDetail4 = OrderDetail.builder()
                .order(order2)
                .product(product2)
                .quantity(4L)
                .orderNo(order2.getOrderNo())
                .statusCode(DELIVERY_ING.getCode())
                .build();

        order1.addOrderDetail(orderDetail1);
        order1.addOrderDetail(orderDetail2);
        order2.addOrderDetail(orderDetail3);
        order2.addOrderDetail(orderDetail4);

        productRepository.saveAll(List.of(product1, product2, product3));
        memberRepository.save(member);
        orderCommendRepository.save(order1);
        orderCommendRepository.save(order2);

        // when
        Page<GetCancelReturnListDtio.Response> orderCancelReturnListResponsePage = orderDtoRepository.findOrdersByMemberId(member.getId(), PageRequest.of(0, 3));
        List<GetCancelReturnListDtio.Response> content = orderCancelReturnListResponsePage.getContent();
        // then
        assertThat(content).hasSize(2)
                .extracting("orderNo")
                .contains("123", "456");

        List<GetCancelReturnListDtio.OrderDetailInfo> detailResponses1Dao = content.get(0).getOrderDetailInfos();
        List<GetCancelReturnListDtio.OrderDetailInfo> detailResponses2Dao = content.get(1).getOrderDetailInfos();

        assertThat(detailResponses1Dao).hasSize(2)
                .extracting("orderNo", "productNo", "name", "price", "quantity", "orderStatus")
                .contains(
                        tuple("123", "1", "옷", 1000L, 1L, RETURN_COMPLETED.getCode()),
                        tuple("123", "2", "신발", 2000L, 2L, ORDER_CANCEL.getCode())
                );

        assertThat(detailResponses2Dao).hasSize(1)
                .extracting("orderNo", "productNo", "name", "price", "quantity", "orderStatus")
                .contains(
                        tuple("456", "3", "바지", 3000L, 3L, ORDER_CANCEL.getCode())
                );
    }

    @DisplayName("전체 주문 목록을 조회 할 경우 페이징과 조건 필터링을 할 수 있다")
    @Test
    @Rollback(value = false)
    void findByMemberIdWithMemberAddress_paging_filter(){

        //given
        Member member = createMember("1");
        Address address = createAddress("서울", "세종대로", "민들레아파트");
        member.addAddress(address);
        memberRepository.save(member);

        Product product1 = createProduct(1000L, "1");
        Product product2 = createProduct(2000L, "2");
        Product product3 = createProduct(3000L, "3");
        Product product4 = createProduct(4000L, "4");
        productRepository.saveAll(List.of(product1, product2, product3, product4));

        OrderDetail orderDetail1 = createOrderDetail(product1,  1L, "1");
        OrderDetail orderDetail2 = createOrderDetail(product2,  1L, "1");
        OrderDetail orderDetail3 = createOrderDetail(product3,  1L, "1");

        OrderDetail orderDetail4 = createOrderDetail(product1,  2L, "2");
        OrderDetail orderDetail5 = createOrderDetail(product2,  2L, "2");
        OrderDetail orderDetail6 = createOrderDetail(product4,  2L, "2");

        OrderDetail orderDetail7 = createOrderDetail(product1,  3L, "3");
        OrderDetail orderDetail8 = createOrderDetail(product3,  3L, "3");
        OrderDetail orderDetail9 = createOrderDetail(product4,  3L, "3");

        OrderDetail orderDetail10 = createOrderDetail(product2, 4L, "4");
        OrderDetail orderDetail11 = createOrderDetail(product3, 4L, "4");
        OrderDetail orderDetail12 = createOrderDetail(product4, 4L, "4");

        Payment payment = createPayment("카드", "카카오뱅크");

                                                                                                                                   // 상품 번호
        Order order1 = createOrder(member, address, "1", List.of(orderDetail1, orderDetail2, orderDetail3), payment); // 1,2,3
        Order order2 = createOrder(member, address, "2", List.of(orderDetail4, orderDetail5, orderDetail6), payment); // 1,2,4
        Order order3 = createOrder(member, address, "3", List.of(orderDetail7, orderDetail8, orderDetail9), payment); // 1,3,4
        Order order4 = createOrder(member, address, "4", List.of(orderDetail10, orderDetail11, orderDetail12), payment); // 2,3,4
        orderCommendRepository.saveAll(List.of(order1, order2, order3, order4));

        Pageable pageSize1_3 = PageRequest.of(1, 3);

        GetOrderHttp.Condition condition1
                = createCondition(member.getId(), "상품", String.valueOf(LocalDateTime.now().getYear()));

        //when
        Page<OrderDtio> orders1_3_c1 = orderDtoRepository.findAllPaged(condition1, pageSize1_3);

        //then
        assertThat(orders1_3_c1.getSize()).isEqualTo(3);
        assertThat(orders1_3_c1.getContent().size()).isEqualTo(1);
        assertThat(orders1_3_c1.getTotalElements()).isEqualTo(4);
        assertThat(orders1_3_c1.getNumberOfElements()).isEqualTo(1);
        assertThat(orders1_3_c1.getTotalPages()).isEqualTo(2);

    }

    private GetOrderHttp.Condition createCondition(Long memberId, String keyword, String year) {
        return GetOrderHttp.Condition.builder()
                .memberId(memberId)
                .keyword(keyword)
                .year(year)
                .build();
    }



    @DisplayName("상세 주문 조회 하기(member, address, orderDetails, product, payment 전부 fetch join)")
    @Test
    void getOrderDetails() {
        // given
        Member member = createMember("1");
        Address address = createAddress("서울", "세종대로", "민들레아파트");
        member.addAddress(address);
        memberRepository.save(member);

        Product product1 = createProduct(1000L, "1");
        Product product2 = createProduct(2000L, "2");
        Product product3 = createProduct(3000L, "3");
        productRepository.saveAll(List.of(product1, product2, product3));

        OrderDetail orderDetail1 = createOrderDetail(product1,  1L, "1");
        OrderDetail orderDetail2 = createOrderDetail(product2,  1L, "1");
        OrderDetail orderDetail3 = createOrderDetail(product3,  1L, "1");

        Payment payment = createPayment("카드", "카카오뱅크");

        Order order = createOrder(member, address, "1", List.of(orderDetail1, orderDetail2, orderDetail3), payment);

        orderCommendRepository.save(order);

        // when
        OrderDtio orderDtio = orderDtoRepository.findByOrderNo(order.getOrderNo());

        //then
        assertThat(orderDtio.getMemberId()).isEqualTo(member.getId());
        assertThat(orderDtio.getAddress()).isEqualTo(address.getAddressValue());

        assertThat(orderDtio.getOrderDetails()).hasSize(3);
        assertThat(orderDtio.getOrderDetails().get(0).getOrderDetailId()).isEqualTo(orderDetail1.getId());
        assertThat(orderDtio.getOrderDetails().get(1).getOrderDetailId()).isEqualTo(orderDetail2.getId());
        assertThat(orderDtio.getOrderDetails().get(2).getOrderDetailId()).isEqualTo(orderDetail3.getId());

        assertThat(orderDtio.getOrderDetails().get(0).getProduct().getProductId()).isEqualTo(product1.getId());
        assertThat(orderDtio.getOrderDetails().get(1).getProduct().getProductId()).isEqualTo(product2.getId());
        assertThat(orderDtio.getOrderDetails().get(2).getProduct().getProductId()).isEqualTo(product3.getId());

        assertThat(orderDtio.getPaymentMethod()).isEqualTo(payment.getPaymentMethod());
        assertThat(orderDtio.getCardIssuerName()).isEqualTo(payment.getCardInfo().getCardIssuerName());

    }

    private Payment createPayment(String paymentMethod, String cardIssuerName) {
        CardInfo cardInfo = CardInfo.builder()
                .cardIssuerName(cardIssuerName)
                .build();

        return Payment.builder()
                .paymentMethod(paymentMethod)
                .cardInfo(cardInfo)
                .build();
    }

    private Order createOrder(Member member1, Address address, String orderNo, List<OrderDetail> orderDetails, Payment payment) {

        Order order = Order.builder()
                .member(member1)
                .address(address)
                .orderNo(orderNo)
                .build();

        // order <-> orderDetail 연관관계
        orderDetails.forEach(order::addOrderDetail);

        // payment <-> payment 연관관계
        if (payment != null) {
            order.linkPayment(payment);
        }

        return order;
    }

    private OrderDetail createOrderDetail(Product product,  Long quantity, String orderNo) {
        return OrderDetail.builder()
                .product(product)
                .quantity(quantity)
                .price(product.getPrice() * quantity)
                .orderNo(orderNo)
                .build();
    }

    private Product createProduct(Long price, String no) {
        return Product.builder()
                .price(price)
                .thumbImg("썸네일"+no)
                .name("상품"+no)
                .build();
    }

    private Member createMember(String no) {
        return Member.builder()
                .name("홍길동"+no)
                .build();
    }

    private Address createAddress(String city, String street, String detail) {
        return Address.builder().addressValue(AddressValue.builder()
                        .city(city)
                        .street(street)
                        .detail(detail).build())
                .build();
    }

}