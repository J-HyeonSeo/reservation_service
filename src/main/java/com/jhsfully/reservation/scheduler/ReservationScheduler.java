package com.jhsfully.reservation.scheduler;

/*
새벽마다, 예약 상태를 확인하고, 이를 다시 정정하는 스케줄러

READY상태가 예약 승인해야하는 날까지 승인이 안되었다면,
READY => REJECT 로 상태 변경 (파트너가 미승인했으므로 거절했다는 증거를 남김.)

ASSIGN상태가 예약시간까지, VISITED가 되지 않았다면,
ASSIGN => EXPIRE 로 상태 변경 (노쇼이므로, 사용자에게 좋지 않음)
*/

import com.jhsfully.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    /*
        매일 새벽 0시 0분 5초에 스케줄러 가동
        해당 스케줄은 예약에 대한 상태를 업데이트 하기위한 스케줄러임.

        예약일이 오늘이라면, READY => REJECT (자동으로 거절로 처리함)

        예약일 + 1 이 오늘이라면, ASSIGN => EXPIRED (자동으로 노쇼 및 예약 파기로 처리함.)
     */

    @Transactional
    @Scheduled(cron = "5 0 0 * * *")
    public void setReservationsState(){

        //쿼리에게 책임 위임.
        reservationRepository.updateReservationState(LocalDate.now());

    }

}
