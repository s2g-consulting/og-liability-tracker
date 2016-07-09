package com.inferwerx.ogliabilitytracker.queries

class InternalQueries {
    companion object Factory {
        val GET_ALL_PROVINCES = "SELECT id, name, short_name FROM provinces ORDER BY name"
        val GET_PROVINCE_BY_NAME = "SELECT id, name, short_name FROM provinces WHERE name = ?"
        val GET_PROVINCE_BY_ID = "SELECT name, short_name FROM provinces WHERE id = ?"

        val GET_ENTITIES_BY_PROVINCE = "SELECT e.id, e.type, e.licence FROM entities e WHERE e.province_id = ?"
        val DELETE_RATINGS_BY_PROVINCE = "DELETE FROM entity_ratings WHERE entity_id in (SELECT id FROM entities WHERE province_id = ?)"

        val INSERT_ENTITY = "INSERT INTO entities (province_id, type, licence, location_identifier) VALUES (?, ?, ?, ?)"
        val INSERT_RATING = "INSERT INTO entity_ratings (entity_id, report_date, entity_status, calculation_type, pvs_value_type, asset_value, liability_value, abandonment_basic, abandonment_additional_event, abandonment_gwp, abandonment_gas_migration, abandonment_vent_flow, abandonment_site_specific, reclamation_basic, reclamation_site_specific) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val INSERT_HIERARCHY_LOOKUP = "INSERT INTO hierarchy_lookup (type, licence, hierarchy_value) VALUES (?, ?, ?)"
        val DELETE_HIERARCHY_LOOKUPS = "DELETE FROM hierarchy_lookup"

        val INSERT_DISPOSITION = "INSERT INTO dispositions (active, description, effective_date, sale_price) VALUES (?, ?, ?, ?)"
        val INSERT_DISPOSITION_ENTITY = "INSERT INTO disposed_entities (province_id, disposition_id, type, licence) VALUES (?, ?, ?, ?)"

        val GET_PROFORMA_HISTORY = """
            SELECT
              r.report_date,
              sum(r.asset_value)                          AS asset_value,
              sum(r.liability_value)                      AS liability_value,
              sum(r.asset_value) / sum(r.liability_value) AS rating,
              sum(r.asset_value) - sum(r.liability_value) AS deposit
            FROM
              entity_ratings r INNER JOIN
              entities e
                ON e.id = r.entity_id
            WHERE
              e.id IN (
                SELECT
                  entity_id
                FROM
                  entity_ratings
                WHERE
                  report_date IN (
                    SELECT
                      max(report_date) latest_month
                    FROM
                      entity_ratings))
              AND e.id NOT IN (
                SELECT
                  DISTINCT
                  e.id
                FROM
                  entities e
                  INNER JOIN
                  disposed_entities de
                    ON de.type = e.type AND de.licence = e.licence
                  INNER JOIN
                  dispositions d
                    ON d.id = de.disposition_id
              )
              AND e.province_id = ?
            GROUP BY
              r.report_date
            ORDER BY
              r.report_date
        """

        val GET_HISTORY = """
            SELECT
              r.report_date,
              sum(r.asset_value)                AS asset_value,
              sum(r.liability_value)            AS liability_value,
              sum(r.asset_value) / sum(r.liability_value) AS rating,
              sum(r.asset_value) - sum(r.liability_value) AS deposit
            FROM entity_ratings r INNER JOIN entities e ON e.id = r.entity_id
            WHERE e.province_id = ?
            GROUP BY r.report_date
            ORDER BY r.report_date
        """

        val GET_REPORT_DATES = """
            SELECT DISTINCT report_date
            FROM entity_ratings r INNER JOIN entities e ON r.entity_id = e.id
            WHERE e.province_id = ?
            ORDER BY report_date DESC
        """

        val GET_NETBACKS = "SELECT effective_date, netback, shrinkage_factor, oil_equivalent_conversion FROM historical_netbacks WHERE province_id = ? ORDER BY effective_date ASC"

        val GET_REPORT_DETAILS = """
            SELECT
              e.type,
              e.licence,
              e.location_identifier,
              h.hierarchy_value,
              r.entity_status,
              r.pvs_value_type,
              r.asset_value,
              r.liability_value,
              r.abandonment_basic,
              r.abandonment_additional_event,
              r.abandonment_gwp,
              r.abandonment_gas_migration,
              r.abandonment_vent_flow,
              r.abandonment_site_specific,
              r.reclamation_basic,
              r.reclamation_site_specific
            FROM entity_ratings r INNER JOIN entities e ON e.id = r.entity_id
              LEFT OUTER JOIN hierarchy_lookup h ON h.type = e.type AND h.licence = e.licence
            WHERE r.report_date = ?
                  AND e.province_id = ?
            ORDER BY h.hierarchy_value, e.licence
        """
    }
}