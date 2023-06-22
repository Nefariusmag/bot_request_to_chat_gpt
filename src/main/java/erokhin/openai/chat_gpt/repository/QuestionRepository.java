package erokhin.openai.chat_gpt.repository;

import erokhin.openai.chat_gpt.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    @Query(value = "select * from questions " +
            "where user_id = :userId and context_id = :contextId order by date_time",
            nativeQuery = true)
    List<QuestionEntity> getHistoryByUserIdAndContextId(
            @Param("userId") String userId, @Param("contextId") Long contextId);
}
