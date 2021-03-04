package name.myc.sqlbuilder;

import java.util.ArrayList;
import java.util.List;
import static java.util.Optional.ofNullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * sql组装器.
 *
 * @author MaYichao
 * @version 1.0
 */
public class SqlBuilders {

    private SqlBuilders() {
        throw new IllegalStateException("Utility class");
    }

    public static SqlBuilder build() {
        return new SqlBuilder();
    }

    public static WhereBuilder where() {
        return WhereBuilder.of();
    }

    public static Compose compose() {
        return Compose.of();
    }

    /**
     * sql创建器.
     */
    @Data
    public static class SqlBuilder {

        protected List<Object> params = new ArrayList<>();
        protected StringBuilder sqlBuffer = new StringBuilder();

        public static SqlBuilder of(String sql) {
            SqlBuilder b = new SqlBuilder();
            return b.append(sql);
        }

        public String getSql() {
            return sqlBuffer.toString();
        }

        public SqlBuilder append(String sql) {
            sqlBuffer.append(sql);
            return this;
        }

        public SqlBuilder append(SqlBuilder builder) {
            sqlBuffer.append(builder.getSql());
            params.addAll(builder.getParams());
            return this;
        }

        public Object[] params() {
            return ofNullable(params).map(lst -> lst.stream().toArray()).orElse(null);
        }

        public boolean hasParams() {
            return !params.isEmpty();
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public abstract static class AbstractCompose<T extends AbstractCompose> extends SqlBuilder {

        public T and(Object param, String sql) {
            return param(param, " and  ", sql, " ");
        }

        public T or(Object param, String sql) {
            return param(param, " or ", sql, " ");

        }

        protected T param(Object param, String pre, String sql, String post) {
            if (param != null) {
                sqlBuffer.append(pre).append(sql.trim()).append(post);
                params.add(param);
            }
            return (T) this;
        }

        @Override
        public String getSql() {
            //当有值时,去除前缀
            return ofNullable(super.getSql()).filter(StringUtils::isNotBlank)
                    .map(this::formatSql).orElse("");
        }

        /**
         * 格式化sql.
         *
         * @param s
         * @return
         */
        protected String formatSql(String s) {
            return removePre(s);
        }

        /**
         * 删除前缀
         *
         * @param s
         * @return
         */
        protected String removePre(String s) {
            s = s.trim();
            String first = s.split(" ")[0];
            switch (first) {
                case "and":
                    s = s.substring(3);
                    break;
                case "or":
                    s = s.substring(2);
                    break;
                default:
                    throw new AssertionError("未识别的关联符:" + first);
            }
            return "" + s + "";
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Compose extends AbstractCompose<Compose> {

        public static Compose of() {
            return new Compose();
        }

        /**
         * 格式化sql.
         *
         * @param sql
         * @return
         */
        @Override
        protected String formatSql(String sql) {
            //加上前后的括号.
            return ofNullable(sql).filter(StringUtils::isNotBlank).map(s -> "(" + s + ")").orElse("");
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class WhereBuilder extends AbstractCompose<WhereBuilder> {

        public static WhereBuilder of() {
            return new WhereBuilder();
        }

        @Override
        protected String formatSql(String s) {
            s = super.formatSql(s);
            if (StringUtils.isNotBlank(s)) {
                s = " where " + s;
            }
            return s;
        }

    }

}
