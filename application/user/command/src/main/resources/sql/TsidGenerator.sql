CREATE OR REPLACE FUNCTION generate_tsid() RETURNS BIGINT AS $$
DECLARE
epoch BIGINT := 0;  -- Unix Epoch 기준 (1970-01-01 00:00:00 UTC)
    timestamp_ms BIGINT;
    node_id BIGINT := 0;  -- Node ID (0~1023 범위 *DB Default : 0 * ApplicationServer 순차증가)
BEGIN
    timestamp_ms := (extract(epoch FROM now()) * 1000)::BIGINT; -- 현재 시간 밀리초

    -- TSID 생성:
    -- 41비트: (timestamp_ms - epoch) << 22
    -- 10비트: node_id << 12
    -- 12비트: 랜덤값 (0 ~ 4095)
RETURN ((timestamp_ms - epoch) << 22)  -- 타임스탬프를 22비트 시프트 (상위 41비트)
    | (node_id << 12)                 -- Node ID를 12비트 시프트 (그 다음 10비트)
    | ((random() * 4096)::BIGINT);      -- 하위 12비트에 랜덤값
END;
$$ LANGUAGE plpgsql;