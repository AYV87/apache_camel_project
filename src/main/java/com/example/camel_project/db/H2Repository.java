package com.example.camel_project.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Handler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class H2Repository {

    private final JdbcTemplate jdbcTemplate;

    // инжектим через конструктор jdbcTemplate
    public H2Repository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // метод который пишет сообщение в бд
    @Handler
    public void writeMessageToDb(InputStream inputStream) {
        String sql = "INSERT into msg (message) values (?)";
        KeyHolder holder = new GeneratedKeyHolder(); // занимается хранением созданого id-шника

        // вставка в таблицу msg
        jdbcTemplate.update(
            connection -> {PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS);
            try {
                ps.setBlob(1, inputStream);
            } catch (Exception e) {
                log.error("Ошибка вставки сообщения в классе H2Repository", e);
            }
            return ps;
        }, holder);
        // вставка в таблицу headers
        insertHeader(holder.getKey().intValue(), "customType");
    }

    // вставка в таблицу headers
    private void insertHeader(int recordId, String value) {
        String sql = "INSERT into headers (headers_id, head) values (?,?)";
        jdbcTemplate.update(sql, recordId, value);
    }

    public void showDataInH2Db(){
        log.info("------ Показать результат в базе данных");
        String sql = "SELECT headers.head, msg.message " +
                " FROM msg" +
                " JOIN headers" +
                " ON msg.msg_id = headers.headers_id";

        // выполняем запрос на получение данных из бд и сохраняем это в list
        List<Map<String, byte[]>> list = jdbcTemplate.query(
                sql, resultSet -> {
                    List<Map<String, byte[]>> list1 = new ArrayList<>();
                    while (resultSet.next()) {
                        Map<String, byte[]> map = new HashMap<>();
                        map.put(
                                resultSet.getString(1),
                                resultSet.getBytes(2));
                        list1.add(map);
                    }
                    return list1;
                });

        // пробежаться по листу и вывести все данные в консоль
        for (Map<String, byte[]> map : list) {
            for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                String str = new String(entry.getValue());
                log.info(
                        String.format(
                                "type: %s  body: %s", entry.getKey(),str));
            }
        }
    }

    // очищением базы данных
    public void cleadH2Db() {
        try {
            String deleteTable1 = "DROP TABLE msg";
            String deleteTable2 = "DROP TABLE headers";
            jdbcTemplate.execute(deleteTable1);
            jdbcTemplate.execute(deleteTable2);
            log.info("Таблица успешно удалена...");
        } catch (Exception e) {
            log.error("Невозможно удалить таблицу", e);
        }
    }
}
