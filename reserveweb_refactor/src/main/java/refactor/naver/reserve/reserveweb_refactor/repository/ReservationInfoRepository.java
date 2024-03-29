package refactor.naver.reserve.reserveweb_refactor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import refactor.naver.reserve.reserveweb_refactor.entity.ReservationInfo;

import java.util.List;

@Repository
public interface ReservationInfoRepository extends JpaRepository<ReservationInfo, Integer> {
    List<ReservationInfo> findByUserId(Long userId);
}
