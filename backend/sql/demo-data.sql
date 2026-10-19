-- Local development data only. Existing rows and accounts are preserved.
BEGIN;
DO $$
DECLARE
    origin_id bigint;
    destination_id bigint;
    demo_route_id bigint;
    demo_train_id bigint;
    demo_car_id bigint;
    demo_schedule_id bigint;
    departure timestamp;
    day_offset integer;
    seat_label text;
BEGIN
    INSERT INTO public.stations(name, code, city, created_at, updated_at)
        VALUES ('서울(데모)', 'DEMO_SEOUL', '서울', now(), now()),
               ('부산(데모)', 'DEMO_BUSAN', '부산', now(), now())
        ON CONFLICT (code) DO NOTHING;
    SELECT id INTO origin_id FROM public.stations WHERE code = 'DEMO_SEOUL';
    SELECT id INTO destination_id FROM public.stations WHERE code = 'DEMO_BUSAN';
    SELECT id INTO demo_route_id FROM public.routes WHERE name = '[DEMO] 서울-부산'
        AND origin_station_id = origin_id AND destination_station_id = destination_id LIMIT 1;
    IF demo_route_id IS NULL THEN
        INSERT INTO public.routes(name, origin_station_id, destination_station_id, created_at, updated_at)
            VALUES ('[DEMO] 서울-부산', origin_id, destination_id, now(), now()) RETURNING id INTO demo_route_id;
    END IF;
    INSERT INTO public.trains(train_no, train_type, name, created_at, updated_at)
        VALUES ('DEMO-101', 'TEST', '로컬 검증용 열차', now(), now()) ON CONFLICT (train_no) DO NOTHING;
    SELECT id INTO demo_train_id FROM public.trains WHERE train_no = 'DEMO-101';
    INSERT INTO public.cars(train_id, car_no, seat_count, created_at, updated_at)
        VALUES (demo_train_id, 1, 8, now(), now()) ON CONFLICT (train_id, car_no) DO NOTHING;
    SELECT id INTO demo_car_id FROM public.cars WHERE train_id = demo_train_id AND car_no = 1;
    FOR seat_label IN SELECT n::text || letter FROM generate_series(1, 4) n CROSS JOIN (VALUES ('A'), ('B')) v(letter) LOOP
        INSERT INTO public.seats(car_id, seat_no, seat_type, created_at, updated_at)
            VALUES (demo_car_id, seat_label, 'STANDARD', now(), now()) ON CONFLICT (car_id, seat_no) DO NOTHING;
    END LOOP;
    FOR day_offset IN 1..2 LOOP
        departure := (now() AT TIME ZONE 'Asia/Seoul')::date + day_offset + time '12:00';
        SELECT id INTO demo_schedule_id FROM public.train_schedules
            WHERE train_id = demo_train_id AND departure_time = departure LIMIT 1;
        IF demo_schedule_id IS NULL THEN
            INSERT INTO public.train_schedules(train_id, route_id, operation_date, departure_time, arrival_time, status, created_at, updated_at)
                VALUES (demo_train_id, demo_route_id, departure::date, departure, departure + interval '2 hours 30 minutes', 'SCHEDULED', now(), now())
                RETURNING id INTO demo_schedule_id;
        END IF;
        INSERT INTO public.schedule_seats(train_schedule_id, seat_id, status, version, created_at, updated_at)
            SELECT demo_schedule_id, id, 'AVAILABLE', 0, now(), now() FROM public.seats WHERE car_id = demo_car_id
            ON CONFLICT (train_schedule_id, seat_id) DO NOTHING;
    END LOOP;
END $$;
COMMIT;
