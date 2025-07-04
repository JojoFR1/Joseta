package joseta.database.type;

import arc.struct.Seq;
import org.hibernate.type.descriptor.*;
import org.hibernate.type.descriptor.java.*;
import org.hibernate.type.descriptor.jdbc.*;
import java.sql.*;

public class SeqLongType implements JdbcType {

    @Override
    public int getJdbcTypeCode() {
        return Types.CLOB; // Store as CLOB (text)
    }

    @Override
    public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
        return new ValueBinder<X>() {
            @Override
            public void bind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                if (value == null) {
                    st.setNull(index, Types.CLOB);
                } else {
                    Seq<?> seq = (Seq<?>) value;
                    String serialized = seq.toString();
                    st.setString(index, serialized);
                }
            }

            @Override
            public void bind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException {
                if (value == null) {
                    st.setNull(name, Types.CLOB);
                } else {
                    Seq<?> seq = (Seq<?>) value;
                    String serialized = seq.toString();
                    st.setString(name, serialized);
                }
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
        return new ValueExtractor<X>() {
            @Override
            public X extract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
                String value = rs.getString(paramIndex);
                if (value == null) {
                    return null;
                }

                return (X) parseSeq(value);
            }

            @Override
            public X extract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                String value = statement.getString(index);
                if (value == null) {
                    return null;
                }
                
                return (X) parseSeq(value);
            }

            @Override
            public X extract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                String value = statement.getString(name);
                if (value == null) {
                    return null;
                }
                
                return (X) parseSeq(value);
            }
        };
    }

    private Seq<Long> parseSeq(String str) {
        if (str == null || str.isEmpty() || str.equals("[]")) return new Seq<>();
        str = str.replace("[", "").replace("]", "").trim();
        String[] values = str.split(",");

        Seq<Long> result = new Seq<>(values.length);
        for (String value : values) {
            if (!value.isEmpty()) result.add(Long.parseLong(value.trim()));
        }

        return result;
    }
}
