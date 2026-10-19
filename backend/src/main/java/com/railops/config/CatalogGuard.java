package com.railops.config;

import com.railops.common.error.*;
import com.railops.route.domain.Route;
import com.railops.station.domain.Station;
import com.railops.train.domain.*;
import jakarta.persistence.*;
import org.springframework.stereotype.Component;

@Component
public class CatalogGuard {
    private final EntityManager em;
    public CatalogGuard(EntityManager em) { this.em = em; }
    public void reference(Class<?> type, Long id) { em.find(type, id, LockModeType.PESSIMISTIC_READ); }
    public void serializeTrain(Long id) { em.find(Train.class, id, LockModeType.PESSIMISTIC_WRITE); }
    public void station(Long id) { check(Station.class, id, "select exists(select 1 from routes where origin_station_id = ?1 or destination_station_id = ?1)"); }
    public void route(Long id) { check(Route.class, id, "select exists(select 1 from train_schedules where route_id = ?1)"); }
    public void train(Long id) { check(Train.class, id, "select exists(select 1 from cars where train_id = ?1) or exists(select 1 from train_schedules where train_id = ?1)"); }
    public void car(Long id) { check(Car.class, id, "select exists(select 1 from seats where car_id = ?1)"); }
    public void seat(Long id) { check(Seat.class, id, "select exists(select 1 from schedule_seats where seat_id = ?1)"); }
    private void check(Class<?> type, Long id, String query) {
        em.find(type, id, LockModeType.PESSIMISTIC_WRITE);
        if (Boolean.TRUE.equals(em.createNativeQuery(query, Boolean.class).setParameter(1, id).getSingleResult())) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE);
        }
    }
}
