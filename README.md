# sqlBuilde
Sql 脚本组装器.

## 使用范例
```
 SqlBuilder b = SqlBuilders.build().append("select * from table_a)
    .append(where()
    .and(param1, " param1 = ? ")
    .and(param2, " param2 = ? "))
    .append(" order by id desc ");
 return jdbcTemplate.query(b.getSql(), b.params());
```
