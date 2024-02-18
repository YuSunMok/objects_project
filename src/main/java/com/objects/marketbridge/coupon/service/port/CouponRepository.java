package com.objects.marketbridge.coupon.service.port;

import com.objects.marketbridge.coupon.domain.Coupon;

import java.util.List;

public interface CouponRepository {
    Coupon findById(Long id);

    void save(Coupon coupon);

    void saveAll(List<Coupon> coupons);

    List<Coupon> findAll();
}
