package com.codeit.mission.deokhugam.comment.batch;

import com.codeit.mission.deokhugam.comment.entity.CommentStatus;
import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommentBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    private static final int CHUNK_SIZE = 100;

    // job 설정 : 댓글 물리 삭제 job 설정
    @Bean
    public Job commentHardDeleteJob(Step commentHardDeleteStep) {
        return new JobBuilder("commentHardDeleteJob", jobRepository)
                .start(commentHardDeleteStep)
                // job 시작 전 사전 작업을 수행할 객체
                .listener(commentHardDeleteJobListener())
                .build();
    }

    // job 리스너 설정 : Job 실행 직전에 기준 시간 (threshold) 계산하여 컨텍스트에 저장
    @Bean
    public JobExecutionListener commentHardDeleteJobListener() {
        return new JobExecutionListener() {     // 익명 클래스 구현

            @Override
            // 사전 작업: 삭제 기준 날짜 지정
            public void beforeJob(@NonNull JobExecution jobExecution) {
                LocalDateTime threshold = LocalDateTime.now().minusDays(1);
                jobExecution.getExecutionContext().put("threshold", threshold.toString());
            }
        };
    }

    // step 설정 : Job 내부에서 수행될 읽기 (Reader) / 쓰기 (Writer) 조합
    @Bean
    public Step commentHardDeleteStep(
            JpaCursorItemReader commentHardDeleteReader,
            ItemWriter<UUID> commentHardDeleteWriter
    ) {
        return new StepBuilder("commentHardDeleteStep", jobRepository)
                .<UUID, UUID>chunk(CHUNK_SIZE, transactionManager)
                .reader(commentHardDeleteReader)
                .writer(commentHardDeleteWriter)
                .build();
    }

    // reader 설정 :
    @Bean
    @StepScope
    public JpaCursorItemReader<UUID> commentHardDeleteReader(@Value("#{jobExecutionContext['threshold']}") String thresholdStr) {
        // 삭제 기준 날짜 설정
        LocalDateTime threshold = LocalDateTime.parse(thresholdStr);

        // reader 객체 반환
        return new JpaCursorItemReaderBuilder<UUID>()
                .name("commentHardDeleteReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT comment.id FROM Comment comment WHERE comment.status = :status AND comment.updatedAt <= :threshold")
                .parameterValues(Map.of("status", CommentStatus.DELETED, "threshold", threshold))
                .build();
    }

    // writer 설정
    @Bean
    @StepScope
    public ItemWriter<UUID> commentHardDeleteWriter() {
        return chunk -> {
            // 지워야 할 id 목록 조회
            List<UUID> commentIds = (List<UUID>) chunk.getItems();
            // 없으면 종료
            if (commentIds.isEmpty()) {
                return;
            }

            // 댓글 물리 삭제
            commentRepository.deleteAllByIdInBatch(commentIds);

            // 로그 기록
            log.info("[COMMENT_BATCH] 보관 기간 만료된 댓글 {}개 물리 삭제 진행", chunk.size());
        };
    }
}
