BEGIN;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS refunded_at timestamp(6);
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS refunded_amount numeric(12,2);
ALTER TABLE public.payments DROP CONSTRAINT IF EXISTS payments_status_check;
ALTER TABLE public.payments ADD CONSTRAINT payments_status_check
    CHECK (status IN ('READY', 'SUCCESS', 'FAILED', 'CANCELED', 'EXPIRED', 'REFUNDED'));
COMMIT;
