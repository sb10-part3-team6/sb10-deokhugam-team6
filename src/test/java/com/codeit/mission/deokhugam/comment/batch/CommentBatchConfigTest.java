package com.codeit.mission.deokhugam.comment.batch;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentBatchConfigTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentBatchConfig commentBatchConfig;

    @Test
    @DisplayName("Chunk 데이터(UUID 리스트)에 담긴 댓글 데이터 삭제 성공")
    void commentHardDeleteWriterSuccess() throws Exception{
        // given
        ItemWriter<UUID> writer = commentBatchConfig.commentHardDeleteWriter();

        List<UUID> targetIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        Chunk<UUID> chunk = new Chunk<>(targetIds);

        // when
        writer.write(chunk);

        // then
        verify(commentRepository).deleteAllByIdInBatch(targetIds);
    }

    @Test
    @DisplayName("Chunk가 비어 있으면 어떤 삭제도 수행하지 않음")
    void commentHardDeleteWriterFailWithEmptyChunk() throws Exception {
        // given
        ItemWriter<UUID> writer = commentBatchConfig.commentHardDeleteWriter();
        Chunk<UUID> chunk = new Chunk<>(List.of());

        // when & then
        writer.write(chunk);
    }

    @Test
    @DisplayName("Writer 실행 도중 데이터베이스 오류가 발생 시, 예외 반환")
    void commentHardDeleteWriterFailWithDbError() {
        // given
        ItemWriter<UUID> writer = commentBatchConfig.commentHardDeleteWriter();
        List<UUID> targetIds = List.of(UUID.randomUUID());
        Chunk<UUID> chunk = new Chunk<>(targetIds);

        willThrow(new RuntimeException("DataBase Timeout Error"))
                .given(commentRepository).deleteAllByIdInBatch(targetIds);

        // when & then
        assertThatThrownBy(() -> writer.write(chunk))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DataBase Timeout Error");

        verify(commentRepository).deleteAllByIdInBatch(targetIds);
    }
}
