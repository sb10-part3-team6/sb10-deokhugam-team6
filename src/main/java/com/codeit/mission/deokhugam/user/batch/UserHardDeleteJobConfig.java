package com.codeit.mission.deokhugam.user.batch;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 탈퇴 유저 물리 삭제를 위한 Spring Batch 설정
 * 논리 삭제 후 1일이 경과한 유저와 연관 데이터를 영구 삭제함
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserHardDeleteJobConfig {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;

    private static final int CHUNK_SIZE = 100; // 한 번에 처리할 유저 수

    @Bean
    public Job userHardDeleteJob(JobRepository jobRepository, Step userHardDeleteStep) {
        return new JobBuilder("userHardDeleteJob", jobRepository)
                .start(userHardDeleteStep)
                .build();
    }

    @Bean
    public Step userHardDeleteStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("userHardDeleteStep", jobRepository)
                .<UUID, UUID>chunk(CHUNK_SIZE, transactionManager)
                .reader(userHardDeleteReader())
                .writer(userHardDeleteWriter())
                .build();
    }

    /**
     * 탈퇴한 지 1일이 지난 유저 ID들을 읽어오는 Reader
     */
    @Bean
    @StepScope
    public ItemReader<UUID> userHardDeleteReader() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        List<UUID> deletedUserIds = userRepository.findDeletedUserIdsOlderThan(threshold, null);
        log.info("[Batch] 물리 삭제 대상 유저 수: {}명 (기준 시간: {})", deletedUserIds.size(), threshold);
        
        return new ListItemReader<>(deletedUserIds);
    }

    /**
     * 읽어온 유저 ID들을 기반으로 모든 연관 데이터를 정합성에 맞게 삭제하는 Writer
     */
    @Bean
    public ItemWriter<UUID> userHardDeleteWriter() {
        return chunk -> {
            List<UUID> userIds = (List<UUID>) chunk.getItems();
            log.info("[Batch] {}명의 유저 및 연관 데이터 물리 삭제 시작", userIds.size());

            // 1. 삭제 대상 유저들이 작성한 리뷰 ID 목록 미리 조회
            List<UUID> reviewIds = reviewRepository.findReviewIdsByUserIds(userIds);
            
            if (!reviewIds.isEmpty()) {
                // 2. 삭제 대상 리뷰들에 달린 '좋아요' 먼저 삭제 (자식의 자식)
                reviewRepository.deleteLikesByReviewIds(reviewIds);
                
                // 3. 삭제 대상 리뷰들에 달린 '댓글' 삭제 (자식의 자식)
                commentRepository.deleteByReviewIds(reviewIds);
                
                // 4. 삭제 대상 리뷰들과 관련된 '알림' 삭제 (자식의 자식)
                notificationRepository.deleteByReviewIds(reviewIds);
            }

            // 5. 유저가 누른 '좋아요' 기록 삭제
            reviewRepository.deleteLikesByUserIds(userIds);
            
            // 6. 유저가 작성한 '댓글' 삭제
            commentRepository.deleteByUserIds(userIds);
            
            // 7. 유저에게 온 '알림' 삭제
            notificationRepository.deleteByUserIds(userIds);
            
            // 8. 유저가 작성한 '리뷰' 삭제
            reviewRepository.deleteByUserIds(userIds);
            
            // 9. 최종 유저 본인 삭제 (물리 삭제)
            userRepository.hardDeleteByIds(userIds);
            
            log.info("[Batch] {}명의 물리 삭제가 완벽하게 완료되었습니다.", userIds.size());
        };
    }
}
