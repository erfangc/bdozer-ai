package co.bdozer.utils

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types

object DataAccess {
    
    private val jdbcUrl = System.getenv("JDBC_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
    private val jdbcUsername = System.getenv("JDBC_USERNAME") ?: "postgres"
    private val jdbcPassword = System.getenv("JDBC_PASSWORD")
    val connection: Connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword)
    
    fun query(sql: String): Sequence<Map<String, Any>> {
        
        val stmt = connection.createStatement()
        val resultSet = stmt.executeQuery(sql)

        return generateSequence {
            if (resultSet.next()) {
                val metaData = resultSet.metaData
                val columnCount = metaData.columnCount

                (1..columnCount).associate { columnIdx ->
                    val columnType = metaData.getColumnType(columnIdx)
                    val columnName = metaData.getColumnName(columnIdx)

                    val columnValue: Any = when (columnType) {
                        Types.BINARY ->
                            resultSet.getBlob(columnIdx)
                        Types.VARCHAR ->
                            resultSet.getString(columnIdx)
                        Types.NUMERIC ->
                            resultSet.getDouble(columnIdx)
                        Types.DOUBLE ->
                            resultSet.getDouble(columnIdx)
                        Types.FLOAT ->
                            resultSet.getDouble(columnIdx)
                        Types.DECIMAL ->
                            resultSet.getDouble(columnIdx)
                        Types.INTEGER ->
                            resultSet.getInt(columnIdx)
                        Types.BIGINT ->
                            resultSet.getLong(columnIdx)
                        Types.SMALLINT ->
                            resultSet.getInt(columnIdx)
                        Types.DATE ->
                            resultSet.getDate(columnIdx)
                        Types.TIMESTAMP ->
                            resultSet.getTimestamp(columnIdx)
                        Types.TIME ->
                            resultSet.getTime(columnIdx)
                        Types.CHAR ->
                            resultSet.getString(columnIdx)
                        else ->
                            resultSet.getString(columnCount)
                    }
                    columnName to columnValue
                }
            } else {
                null
            }
        }
        
    }
}