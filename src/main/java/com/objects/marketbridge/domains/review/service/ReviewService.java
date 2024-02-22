package com.objects.marketbridge.domains.review.service;

import com.objects.marketbridge.domains.image.domain.Image;
import com.objects.marketbridge.domains.image.infra.ImageRepository;
import com.objects.marketbridge.domains.member.domain.Member;
import com.objects.marketbridge.domains.member.service.port.MemberRepository;
import com.objects.marketbridge.domains.product.domain.Product;
import com.objects.marketbridge.domains.review.domain.*;
import com.objects.marketbridge.domains.review.dto.*;
import com.objects.marketbridge.domains.review.service.port.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewSurveyRepository reviewSurveyRepository;
    private final ReviewSurveyCategoryRepository reviewSurveyCategoryRepository;
    private final SurveyContentRepository surveyContentRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberRepository memberRepository;

    //리뷰 서베이 선택창 조회
    @Transactional
    public List<ReviewSurveyQuestionAndOptionsDto> getReviewSurveyQuestionAndOptionsList(Long productId) {
        //해당 상품에 대한 reviewSurveyCategory을 가져옴(선택창+입력창)
        //해당 상품에 대한 SurveyContent를 가져옴(선택창)
        //해당 상품에 대한 입력창에 입력할 SurveyContent는 프론트에서 처리

        List<ReviewSurveyCategory> reviewSurveyCategoryList
                = reviewSurveyCategoryRepository.findAllByProductId(productId);

        ReviewSurveyQuestionAndOptionsDto reviewSurveyQuestionAndOptionsDto;

        List<ReviewSurveyQuestionAndOptionsDto> reviewSurveyQuestionAndOptionsDtoList = new ArrayList<>();
        //reviewSurveyQuestionsAndOptions을 담을 for문
        for (ReviewSurveyCategory reviewSurveyCategory : reviewSurveyCategoryList) {
            //리뷰작성자가 옵션 중에 선택하는 경우의 옵션들
            if (surveyContentRepository.existsBySurveyCategoryId(reviewSurveyCategory.getId())) {
                List<SurveyContent> surveyContentList
                        = surveyContentRepository.findAllBySurveyCategoryId(reviewSurveyCategory.getId());
                List<String> surveyContentContentListForDto = new ArrayList<>();

                for (SurveyContent surveyContent : surveyContentList) {
                    surveyContentContentListForDto.add(surveyContent.getContent());
                }
                reviewSurveyQuestionAndOptionsDto
                        = ReviewSurveyQuestionAndOptionsDto.builder()
                        .reviewSurveyQuestion(reviewSurveyCategory.getName())
                        .reviewSurveyOptionList(surveyContentContentListForDto)
                        .build();
                reviewSurveyQuestionAndOptionsDtoList.add(reviewSurveyQuestionAndOptionsDto);
                //리뷰작성자가 선택하지 않고 직접 입력하는 경우는 빈값(null)
            } else {
                reviewSurveyQuestionAndOptionsDto
                        = ReviewSurveyQuestionAndOptionsDto.builder()
                        .reviewSurveyQuestion(reviewSurveyCategory.getName())
                        .reviewSurveyOptionList(null)
                        .build();
                reviewSurveyQuestionAndOptionsDtoList.add(reviewSurveyQuestionAndOptionsDto);
            }
        }
        return reviewSurveyQuestionAndOptionsDtoList;
    }


    //리뷰 등록
    @Transactional
    public void createReview(CreateReviewDto request, Long memberId) {

        //로그인한 유저여야 하므로, memberId는 파라미터로 받아 사용.
        Integer rating = request.getRating();
        String content = request.getContent();
        String summary = request.getSummary();

        Member member = Member.builder().id(memberId).build();
        Product product = Product.builder().id(request.getProductId()).build();

        //이미지 저장 및 리뷰이미지 저장
        Review review = Review.builder()
                .member(member)
                .product(product)
                .rating(rating)
                .content(content)
                .summary(summary)
                .build();

        createReviewImages(request.getReviewImages(), review);

        //선택&입력된 리뷰 서베이들 등록
        createReviewSurveys(request, review);

        reviewRepository.save(review);
    }


    @Transactional
    public void updateReview(UpdateReviewDto request) {

        Long reviewId = request.getReviewId();

        Review review = reviewRepository.findById(reviewId);

        deleteReviewImages(review);

        updateReviewSurveys(request, review);
        createReviewImages(request.getReviewImages(), review);

        review.update(request.getRating(), request.getContent(), request.getSummary());
    }


    @Transactional
    public void deleteReview(Long reviewId){
        reviewRepository.deleteById(reviewId);
    }

//    @Transactional
//    public void getReviews(Long productId) {
//        Page<Review> reviews;
//        if (sortBy.equals("likes")) {
//            reviews = reviewRepository.findAllByProductIdOrderByLikesDesc(productId, pageable);
//        } else {
//            reviews = reviewRepository.findAllByProductId(productId, pageable);
//        }
//
//        List<ReviewWholeInfoDto> reviewWholeInfoDtoList = reviews.getContent().stream().map(
//                        review -> ReviewWholeInfoDto.builder()
//                                .productName(review.getProduct().getName())
//                                .memberName(review.getMember().getName())
//                                .rating(review.getRating())
//                                .reviewSurveyList(reviewSurveyRepository.findAllByReviewId(review.getId()))
//                                .content(review.getContent())
//                                .createdAt(review.getCreatedAt())
//                                .reviewImgUrls(review.getReviewImages().stream()
//                                        .map(reviewImage -> reviewImage.getImage().getUrl()).collect(Collectors.toList()))
//                                .sellerName("MarketBridge")
////                                //LIKE관련//
////                                .likes(review.getLikes()) // 변경된 부분: Review 엔티티의 likes 필드 사용.
//                                .build())
//                .collect(Collectors.toList());
//        return new PageImpl<>(reviewWholeInfoDtoList, pageable, reviews.getTotalElements());
//    }


    //리뷰아이디로 리뷰상세 단건 조회
//    @Transactional
//    public ReviewSingleReadDto getReview(Long reviewId, Long memberId){
//        Review findReview = reviewRepository.findById(reviewId);
//        List<ReviewImage> reviewImages = findReview.getReviewImages();
//        List<String> reviewImgUrls = new ArrayList<>();
//        for (ReviewImage reviewImage : reviewImages) {
//            reviewImgUrls.add(reviewImage.getImage().getUrl());
//        }
//        ReviewSingleReadDto reviewSingleReadDto
//                = ReviewSingleReadDto.builder()
//                .reviewId(reviewId)
//                .memberId(memberId)
//                .productId(findReview.getProduct().getId())
//                .reviewImgUrls(reviewImgUrls)
//                .rating(findReview.getRating())
//                .content(findReview.getContent())
//                .build();
//        return reviewSingleReadDto;
//    }


//    //LIKE관련//
//        //회원별 리뷰 리스트 조회(createdAt 최신순 내림차순 정렬 또는 liked 많은순 내림차순 정렬)
//    @Transactional
//    public Page<ReviewWholeInfoDto> getMemberReviews(Long memberId, Pageable pageable, String sortBy) {
//        Page<Review> reviews;
//        if (sortBy.equals("likes")) {
//            reviews = reviewRepository.findAllByMemberIdOrderByLikesDesc(memberId, pageable);
//        } else {
//            reviews = reviewRepository.findAllByMemberId(memberId, pageable);
//        }
//
//        List<ReviewWholeInfoDto> reviewWholeInfoDtoList = reviews.getContent().stream().map(
//                        review -> ReviewWholeInfoDto.builder()
//                                .productName(review.getProduct().getName())
//                                .memberName(review.getMember().getName())
//                                .rating(review.getRating())
//                                .createdAt(review.getCreatedAt())
//                                .sellerName("MarketBridge")
//                                .reviewImgUrls(review.getReviewImages().stream()
//                                        .map(reviewImage -> reviewImage.getImage().getUrl()).collect(Collectors.toList()))
//                                .content(review.getContent())
////                                //LIKE관련//
////                                .likes(reviewLikesRepository.countByReviewIdAndLikedIsTrue(review.getId()))
//                                .build())
//                .collect(Collectors.toList());
//        return new PageImpl<>(reviewWholeInfoDtoList, pageable, reviews.getTotalElements());
//    }


    //상품별 리뷰 총갯수 조회
    @Transactional
    public ReviewsCountDto getProductReviewsCount(Long productId) {
        Long count = reviewRepository.countByProductId(productId);
        return ReviewsCountDto.builder().count(count).build();
    }


    //회원별 리뷰 총갯수 조회
    @Transactional
    public ReviewsCountDto getMemberReviewsCount(Long memberId) {
        Long count = reviewRepository.countByMemberId(memberId);
        return ReviewsCountDto.builder().count(count).build();
    }


    //review_like upsert(없으면 create, 있으면 delete)
    @Transactional
    public void upsertReviewLike(Long reviewId, Long memberId){

        Review review = Review.builder().id(reviewId).build();
        Member member = Member.builder().id(memberId).build();

        if(reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId)){
            reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
        } else {
            ReviewLike reviewLike = ReviewLike.builder()
                    .review(review)
                    .member(member)
                    .build();
            reviewLikeRepository.save(reviewLike);
        }
    }


//    //LIKE관련//
//    //리뷰 좋아요 등록 또는 변경(True화/False화)
//    @Transactional
//    public ReviewLikeDto addOrChangeReviewLike(Long reviewId, Long memberId) {
//        if (reviewLikesRepository.existsByReviewIdAndMemberId(reviewId, memberId)) {
//            // 이미 좋아요가 있는 경우에는 좋아요 상태 변경 로직을 수행
//            return changeReviewLike(reviewId, memberId);
//        } else {
//            // 좋아요가 없는 경우에는 새로운 좋아요 추가
//            return addReviewLike(reviewId, memberId);
//        }
//    }
//    @Transactional
//    public ReviewLikeDto addReviewLike(Long reviewId, Long memberId) {
//        // 좋아요 추가 로직 추가
//
//        Review findReview = reviewRepository.findById(reviewId);
//        ReviewLikes reviewLikes = ReviewLikes.builder()
//                .review(findReview)
//                .member(memberRepository.findById(memberId))
////                .product(findReview.getProduct())
//                .liked(true) // 처음에는 좋아요 상태를 true로 설정(등록자체가 좋아요를 누른것이므로)
//                .build();
//
//        findReview.increaseLikes();
//        reviewLikesRepository.save(reviewLikes);
//
//        return ReviewLikeDto.builder()
//                .reviewId(reviewId)
//                .memberId(memberId)
//                .liked(true)
//                .build();
//    }
//    @Transactional
//    public ReviewLikeDto changeReviewLike(Long reviewId, Long memberId) {
//        ReviewLikes findReviewLikes = reviewLikesRepository.findByReviewIdAndMemberId(reviewId, memberId);
//
//        System.out.println("findReviewLikes.getLiked() == " + findReviewLikes.getLiked());
//        Boolean changedLiked = findReviewLikes.changeLiked();
//        System.out.println("changedLiked == " + changedLiked);
//
//        Review findReview = reviewRepository.findById(reviewId);
//        if(changedLiked == true){
//            findReview.increaseLikes();
//        } else {
//            findReview.decreaseLikes();
//        }
//
//        reviewLikesRepository.save(findReviewLikes);
//        System.out.println("after Change:findLiked == " + findReviewLikes.getLiked());
//        reviewRepository.save(findReview);
//
//        ReviewLikeDto reviewLikeDto = ReviewLikeDto.builder().reviewId(reviewId).memberId(memberId).liked(changedLiked).build();
//        return reviewLikeDto;
//    }


    //    //LIKE관련//
//    //리뷰 좋아요 총갯수 조회
//    @Transactional
//    public ReviewLikesCountDto countReviewLikes(Long reviewId) {
//        Review findReview = reviewRepository.findById(reviewId);
//        Long count = reviewLikesRepository.countByReviewIdAndLikedIsTrue(reviewId);
//        ReviewLikesCountDto reviewLikesCountDto
//                = ReviewLikesCountDto.builder().reviewId(reviewId).count(count).build();
//        return reviewLikesCountDto;
//    }
    private void createReviewSurveys(CreateReviewDto request, Review review) {
        request.getReviewSurveys().forEach(obj -> {
            ReviewSurvey reviewSurvey = ReviewSurvey.builder()
                    .review(review)
                    .reviewSurveyCategoryId(obj.getReviewSurveyCategoryId())
                    .surveyCategory(obj.getReviewSurveyCategoryName())
                    .content(obj.getContent())
                    .build();

            review.addReviewSurveys(reviewSurvey);
        });
    }

    private void createReviewImages(List<ReviewImageDto> request, Review review) {
        request.forEach(obj -> {
            Image image = Image.builder()
                    .url(obj.getImgUrl())
                    .build();
            imageRepository.save(image);

            ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .image(image)
                    .seqNo(obj.getSeqNo())
                    .description(obj.getDescription())
                    .build();
            image.setReviewImage(reviewImage);
            review.addReviewImages(reviewImage);
        });
    }

    private void updateReviewSurveys(UpdateReviewDto request, Review review) {
        review.getReviewSurveys().forEach(reviewSurvey -> {
            UpdateReviewSurveyDto updateReviewSurveyDto =
                    request.getUpdateReviewSurveys().stream()
                            .filter(obj -> Objects.equals(obj.getReviewSurveyId(), reviewSurvey.getId()))
                            .findFirst()
                            .orElse(new UpdateReviewSurveyDto(reviewSurvey.getId(), reviewSurvey.getContent()));
            reviewSurvey.update(updateReviewSurveyDto.getContent());
        });

    }

    private void deleteReviewImages(Review review) {
        List<ReviewImage> reviewImages = review.getReviewImages();
        reviewImageRepository.deleteAllByIdInBatch(reviewImages.stream().map(ReviewImage::getId).toList());
        imageRepository.deleteAllByIdInBatch(reviewImages.stream().map(obj -> obj.getImage().getId()).toList());
    }

}