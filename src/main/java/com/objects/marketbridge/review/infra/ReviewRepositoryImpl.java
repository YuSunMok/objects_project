package com.objects.marketbridge.review.infra;

import com.objects.marketbridge.review.domain.Review;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public void save(Review review) {
        reviewJpaRepository.save(review);
    }

    @Override
    public Review findById(Long id) {
        return reviewJpaRepository.findById(id)
                .orElseThrow(() -> new JpaObjectRetrievalFailureException(new EntityNotFoundException()));
    }

    @Override
    public void delete(Review review) {
        reviewJpaRepository.delete(review);
    }

    @Override
    public Page<Review> findAllByProductId(Long productId, Pageable pageable) {
        return reviewJpaRepository.findAllByProductId(productId, pageable);
    }

    @Override
    public Page<Review> findAllByMemberId(Long memberId, Pageable pageable) {
        return reviewJpaRepository.findAllByMemberId(memberId, pageable);
    }

    public Long countByProductId(Long productId) {
        return reviewJpaRepository.countByProductId(productId);
    }

    public Long countByMemberId(Long memberId) {
        return reviewJpaRepository.countByMemberId(memberId);
    }
}