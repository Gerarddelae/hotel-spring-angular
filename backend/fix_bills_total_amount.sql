-- ================================================================
-- SCRIPT DE CORRECCIÓN: Facturas con Total Incorrecto
-- ================================================================
-- Este script corrige las facturas que solo tienen el total de
-- addons y no incluyen el costo de la estadía (booking.totalAmount)
-- ================================================================

-- 1. PRIMERO: Verificar cuántas facturas están afectadas
SELECT
    COUNT(*) as total_facturas_incorrectas,
    SUM(b.total_amount) as total_incorrecto_actual,
    SUM(bk.total_amount + COALESCE((
        SELECT SUM(ba.quantity * ba.unit_price)
        FROM bill_addon ba
        WHERE ba.bill_id = b.id AND ba.deleted = false
    ), 0)) as total_correcto_deberia_ser,
    SUM(bk.total_amount + COALESCE((
        SELECT SUM(ba.quantity * ba.unit_price)
        FROM bill_addon ba
        WHERE ba.bill_id = b.id AND ba.deleted = false
    ), 0)) - SUM(b.total_amount) as diferencia_total
FROM bills b
JOIN bookings bk ON b.booking_id = bk.id
WHERE b.deleted = false
  AND b.status = 'PAID'
  AND b.total_amount < bk.total_amount;  -- Facturas con total menor a la estadía (bug)

-- 2. REVISAR: Ver facturas específicas afectadas
SELECT
    b.id as bill_id,
    b.booking_id,
    b.created_at as fecha_factura,
    b.total_amount as total_actual_incorrecto,
    bk.total_amount as costo_estadia,
    COALESCE((
        SELECT SUM(ba.quantity * ba.unit_price)
        FROM bill_addon ba
        WHERE ba.bill_id = b.id AND ba.deleted = false
    ), 0) as total_addons,
    bk.total_amount + COALESCE((
        SELECT SUM(ba.quantity * ba.unit_price)
        FROM bill_addon ba
        WHERE ba.bill_id = b.id AND ba.deleted = false
    ), 0) as total_correcto,
    (bk.total_amount + COALESCE((
        SELECT SUM(ba.quantity * ba.unit_price)
        FROM bill_addon ba
        WHERE ba.bill_id = b.id AND ba.deleted = false
    ), 0)) - b.total_amount as diferencia
FROM bills b
JOIN bookings bk ON b.booking_id = bk.id
WHERE b.deleted = false
  AND b.status = 'PAID'
  AND b.total_amount < bk.total_amount
ORDER BY b.created_at DESC;

-- 3. BACKUP: Crear tabla de respaldo antes de actualizar
CREATE TABLE IF NOT EXISTS bills_backup_20251125 AS
SELECT * FROM bills
WHERE id IN (
    SELECT b.id
    FROM bills b
    JOIN bookings bk ON b.booking_id = bk.id
    WHERE b.deleted = false
      AND b.status = 'PAID'
      AND b.total_amount < bk.total_amount
);

-- Verificar que se creó el backup
SELECT COUNT(*) as registros_respaldados
FROM bills_backup_20251125;

-- 4. CORRECCIÓN: Actualizar facturas con el total correcto
-- ⚠️ EJECUTAR SOLO DESPUÉS DE VERIFICAR LOS PASOS ANTERIORES
UPDATE bills b
SET total_amount = (
    SELECT
        bk.total_amount + COALESCE((
            SELECT SUM(ba.quantity * ba.unit_price)
            FROM bill_addon ba
            WHERE ba.bill_id = b.id AND ba.deleted = false
        ), 0)
    FROM bookings bk
    WHERE bk.id = b.booking_id
)
WHERE b.deleted = false
  AND b.status = 'PAID'
  AND b.id IN (
    SELECT b2.id
    FROM bills b2
    JOIN bookings bk2 ON b2.booking_id = bk2.id
    WHERE b2.total_amount < bk2.total_amount
  );

-- 5. VERIFICACIÓN: Confirmar que se corrigieron
SELECT
    COUNT(*) as facturas_corregidas,
    SUM(b.total_amount) as nuevo_total_correcto
FROM bills b
WHERE b.id IN (
    SELECT id FROM bills_backup_20251125
);

-- 6. COMPARACIÓN: Antes vs Después
SELECT
    'ANTES' as momento,
    SUM(total_amount) as total_revenue
FROM bills_backup_20251125
UNION ALL
SELECT
    'DESPUÉS' as momento,
    SUM(b.total_amount) as total_revenue
FROM bills b
WHERE b.id IN (
    SELECT id FROM bills_backup_20251125
);

-- 7. DETALLE: Ver facturas corregidas una por una
SELECT
    b.id,
    backup.total_amount as total_antes,
    b.total_amount as total_despues,
    b.total_amount - backup.total_amount as diferencia,
    ROUND((b.total_amount - backup.total_amount) / backup.total_amount * 100, 2) as porcentaje_incremento
FROM bills b
JOIN bills_backup_20251125 backup ON b.id = backup.id
ORDER BY diferencia DESC;

-- ================================================================
-- NOTAS IMPORTANTES:
-- ================================================================
-- 1. Este script SOLO corrige facturas con status = 'PAID'
-- 2. Solo corrige facturas donde total_amount < booking.total_amount
--    (indicador claro del bug)
-- 3. Se crea un backup automático antes de actualizar
-- 4. Puedes revertir copiando desde bills_backup_20251125 si algo sale mal
--
-- ROLLBACK (si es necesario):
-- UPDATE bills b
-- SET total_amount = backup.total_amount
-- FROM bills_backup_20251125 backup
-- WHERE b.id = backup.id;
-- ================================================================

-- 8. LIMPIEZA (Opcional): Eliminar backup después de confirmar
-- DROP TABLE bills_backup_20251125;

