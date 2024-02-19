//package com.objects.marketbridge.review.infra;
//
//import com.objects.marketbridge.review.domain.ReviewLikes;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface ReviewLikesRepository {
//
//    ReviewLikes findByReviewIdAndMemberId(Long reviewId, Long memberId);
//
//    void save(ReviewLikes reviewLikes);
//
//    Page<ReviewLikes> findAllByReview_Product_Id(Long productId, Pageable pageable);
//
//    List<Long> findReviewIdsByProductIdOrderByLikedCount(Long productId);
//
//    Page<ReviewLikes> findAllByReview_Member_Id(Long memberId, Pageable pageable);
//
//    void delete(ReviewLikes reviewLikes);
//
//    void deleteById(Long reviewLikesId);
//
//    void deleteByReviewId(Long reviewId);
//
//    Long countByReviewIdAndLikedIsTrue(Long reviewId);
//
//    Boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);
//
//    List<ReviewLikes> findAllByReviewId(Long reviewId);
//
//    void deleteAllByReviewId(Long reviewId);
//}
////LIKE관련//
