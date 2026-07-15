package com.example.inventory.repository;

import com.example.inventory.model.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class ItemRepository {

    private static final Logger log = LogManager.getLogger(ItemRepository.class);

    private final JdbcTemplate jdbc;

    public ItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        jdbc.execute(
            "CREATE TABLE IF NOT EXISTS items (" +
            "  id       BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "  name     VARCHAR(255)," +
            "  sku      VARCHAR(100)," +
            "  quantity INT," +
            "  price    DOUBLE" +
            ")"
        );
    }

    public List<Item> findByName(String name) {
        String sql = "SELECT * FROM items WHERE name = '" + name + "'";
        log.info("Executing query: {}", sql);   // also logs user input → Log4Shell vector
        return jdbc.query(sql, new ItemRowMapper());
    }

    public List<Item> findAll() {
        return jdbc.query("SELECT * FROM items", new ItemRowMapper());
    }

    public Item findById(Long id) {
        List<Item> results = jdbc.query(
                "SELECT * FROM items WHERE id = ?", new ItemRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Item save(Item item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO items (name, sku, quantity, price) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, item.getName());
            ps.setString(2, item.getSku());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getPrice());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("Insert did not return a generated key");
        item.setId(key.longValue());
        return item;
    }

    public Item update(Long id, Item item) {
        jdbc.update(
            "UPDATE items SET name = ?, sku = ?, quantity = ?, price = ? WHERE id = ?",
            item.getName(), item.getSku(), item.getQuantity(), item.getPrice(), id
        );
        item.setId(id);
        return item;
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM items WHERE id = ?", id);
    }

    private static class ItemRowMapper implements RowMapper<Item> {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Item(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("sku"),
                rs.getInt("quantity"),
                rs.getDouble("price")
            );
        }
    }
}
