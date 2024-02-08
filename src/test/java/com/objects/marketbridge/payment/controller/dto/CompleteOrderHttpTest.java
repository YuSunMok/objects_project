package com.objects.marketbridge.payment.controller.dto;

import com.objects.marketbridge.member.domain.AddressValue;
import com.objects.marketbridge.member.domain.Address;
import com.objects.marketbridge.order.domain.Order;
import com.objects.marketbridge.payment.domain.Amount;
import com.objects.marketbridge.payment.domain.CardInfo;
import com.objects.marketbridge.payment.domain.Payment;
import com.objects.marketbridge.payment.service.dto.ProductInfoDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

class CompleteOrderHttpTest {

    @DisplayName("Order, Payment 로 CompleteOrderHttp.Response 를 생성할 수 있어야함")
    @Test
    void of() {
        // given
        Address address = createAddress(createAddressValue());
        Order order = createOrder(address);
        CardInfo cardInfo = createCardInfo();
        Amount amount = createAmount();
        Payment payment = createPayment(order, cardInfo, amount);

        // when
        CompleteOrderHttp.Response response = CompleteOrderHttp.Response.of(payment);

        //then
        Assertions.assertThat(response).extracting(
                "paymentMethodType",
                "orderName",
                "approvedAt",
                "amount",
                "cardInfo",
                "addressValue",
                "productInfos"
        ).containsExactlyInAnyOrder(
                payment.getPaymentMethod(),
                payment.getOrder().getOrderName(),
                payment.getApprovedAt(),
                payment.getAmount(),
                payment.getCardInfo(),
                payment.getOrder().getAddress().getAddressValue(),
                payment.getOrder().getOrderDetails().stream().map(o -> ProductInfoDto.of(o.getProduct())).collect(Collectors.toList())
        );
    }

    private  Order createOrder(Address address) {
        return Order.builder()
                .address(address)
                .orderName("가방")
                .orderNo("1234")
                .totalDiscount(4000L)
                .totalPrice(10000L)
                .realPrice(6000L)
                .tid("tid")
                .build();
    }

    private  Payment createPayment(Order order, CardInfo cardInfo, Amount amount) {
        return Payment.builder()
                .order(order)
                .orderNo("1234")
                .paymentMethod("CARD")
                .tid("tid")
                .approvedAt(LocalDateTime.of(2023, 12, 3, 12, 20))
                .cardInfo(cardInfo)
                .amount(amount)
                .build();
    }

    private  Amount createAmount() {
        return Amount.builder()
                .totalAmount(10000L)
                .discountAmount(4000L)
                .build();
    }

    private  CardInfo createCardInfo() {
        return CardInfo.builder()
                .cardIssuerName("카카오뱅크")
                .cardPurchaseName("국민은행")
                .cardInstallMonth("0")
                .build();
    }

    private  Address createAddress(AddressValue addressValue) {
        return Address.builder()
                .addressValue(addressValue)
                .build();
    }

    private AddressValue createAddressValue() {
        return AddressValue.builder()
                .name("홍길동")
                .phoneNo("01012341234")
                .city("서울")
                .street("상도로")
                .zipcode("12345")
                .detail("101동 1212호")
                .alias("우리집")
                .build();
    }

}