package com.clientservice.infrastructure.persistence.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

/**
 * PostgreSQL JSONB 类型处理器
 *
 * <p>用于处理 PostgreSQL 的 JSONB 类型与 Java String 的转换
 * <p>PostgreSQL JDBC驱动会自动处理JSONB类型，直接转换为String即可
 */
@MappedJdbcTypes(JdbcType.OTHER)
public class PostgresJsonTypeHandler extends BaseTypeHandler<String> {

    /**
     * 设置非空参数
     *
     * @param ps PreparedStatement
     * @param i 参数索引
     * @param parameter 参数值（JSON字符串）
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(
            final PreparedStatement ps,
            final int i,
            final String parameter,
            final JdbcType jdbcType)
            throws SQLException {
        // PostgreSQL JDBC驱动会自动处理JSONB类型
        ps.setObject(i, parameter, Types.OTHER);
    }

    /**
     * 获取可空结果
     *
     * @param rs ResultSet
     * @param columnName 列名
     * @return JSON字符串
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(final ResultSet rs, final String columnName)
            throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) {
            return null;
        }
        // PostgreSQL返回的JSONB会自动转换为String
        return value.toString();
    }

    /**
     * 获取可空结果
     *
     * @param rs ResultSet
     * @param columnIndex 列索引
     * @return JSON字符串
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(final ResultSet rs, final int columnIndex)
            throws SQLException {
        Object value = rs.getObject(columnIndex);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 获取可空结果
     *
     * @param cs CallableStatement
     * @param columnIndex 列索引
     * @return JSON字符串
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(final CallableStatement cs, final int columnIndex)
            throws SQLException {
        Object value = cs.getObject(columnIndex);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
