package erokhin.openai.chat_gpt.repository;

import erokhin.openai.chat_gpt.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "select context_id from users where user_id = :userId order by context_id desc limit 1", nativeQuery = true)
    Long findContextIdByUserId(@Param("userId") String userId);
}
