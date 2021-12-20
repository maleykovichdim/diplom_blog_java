package blog.model.repository;

import blog.model.CaptchaCode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CaptchaRepository extends CrudRepository<CaptchaCode, Integer> {
    Optional<CaptchaCode> findByCode(String code);

    @Transactional
    @Modifying
    @Query("DELETE FROM CaptchaCode c WHERE c.time < :expirationTime")
    void cleanExpired(@Param("expirationTime") LocalDateTime expirationTime);

    void deleteByTimeBefore(LocalDateTime time);
    Optional<CaptchaCode> findCaptchaCodeBySecretCode(String secretCode);

    @Query(value = "SELECT * FROM captcha_codes " +
            "WHERE captcha_codes.code = :code " +
            "AND captcha_codes.secret_code = :secret_code", nativeQuery = true)
    CaptchaCode getCaptchaCode(@Param("code") String code, @Param("secret_code") String secretCode);

}



