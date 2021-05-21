package wooteco.subway.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import wooteco.subway.domain.Distance;
import wooteco.subway.domain.Id;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Station;

@Repository
public class SectionDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Section> rowMapper = (rs, rowNum) -> {
        Long sectionId = rs.getLong("section_id");
        Long lineId = rs.getLong("line_id");
        String lineName = rs.getString("line_name");
        String lineColor = rs.getString("line_color");
        Long upStationId = rs.getLong("up_id");
        String upStationName = rs.getString("up_name");
        Long downStationId = rs.getLong("down_id");
        String downStationName = rs.getString("down_name");
        int distance = rs.getInt("distance");

        return new Section(
            new Id(sectionId),
            new Line(lineId, lineName, lineColor),
            new Station(upStationId, upStationName),
            new Station(downStationId, downStationName),
            new Distance(distance)
        );
    };

    public SectionDao(JdbcTemplate jdbcTemplate, DataSource source) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(source)
            .withTableName("SECTION")
            .usingGeneratedKeyColumns("id");
    }

    public Section insert(Section section) {
        Map<String, Object> params = new HashMap<>();
        params.put("line_id", section.getLineId());
        params.put("up_station_id", section.getUpStationId());
        params.put("down_station_id", section.getDownStationId());
        params.put("distance", section.getDistanceValue());

        long key = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Section(key, section);
    }

    public List<Section> findAllByLineId(Long lineId) {
        String sql =
            "SELECT s.id AS section_id, line.id AS line_id, line.name AS line_name, line.color AS line_color, "
                + "up_station.id AS up_id, up_station.name AS up_name, "
                + "down_station.id AS down_id, down_station.name AS down_name, "
                + "distance "
                + "FROM section AS s "
                + "LEFT JOIN line ON s.line_id = line.id "
                + "LEFT JOIN station AS up_station ON s.up_station_id = up_station.id "
                + "LEFT JOIN station AS down_station ON s.down_station_id = down_station.id "
                + "WHERE s.line_id = ?";
        return jdbcTemplate.query(sql, rowMapper, lineId);
    }

    public Optional<Section> findByLineIdAndUpStationId(Long lineId, Long upStationId) {
        try {
            String sql =
                "SELECT s.id AS section_id, line.id AS line_id, line.name AS line_name, line.color AS line_color, "
                    + "up_station.id AS up_id, up_station.name AS up_name, "
                    + "down_station.id AS down_id, down_station.name AS down_name, "
                    + "distance "
                    + "FROM section AS s "
                    + "LEFT JOIN line ON s.line_id = line.id "
                    + "LEFT JOIN station AS up_station ON s.up_station_id = up_station.id "
                    + "LEFT JOIN station AS down_station ON s.down_station_id = down_station.id "
                    + "WHERE s.line_id = ? AND s.up_station_id = ?";
            return Optional
                .ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, lineId, upStationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Section> findByLineIdAndDownStationId(Long lineId, Long downStationId) {
        try {
            String sql =
                "SELECT s.id AS section_id, line.id AS line_id, line.name AS line_name, line.color AS line_color, "
                    + "up_station.id AS up_id, up_station.name AS up_name, "
                    + "down_station.id AS down_id, down_station.name AS down_name, "
                    + "distance "
                    + "FROM section AS s "
                    + "LEFT JOIN line ON s.line_id = line.id "
                    + "LEFT JOIN station AS up_station ON s.up_station_id = up_station.id "
                    + "LEFT JOIN station AS down_station ON s.down_station_id = down_station.id "
                    + "WHERE s.line_id = ? AND s.down_station_id = ?";
            return Optional
                .ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, lineId, downStationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int deleteByLineIdAndUpStationId(Long lineId, Long upStationId) {
        String sql = "DELETE FROM section WHERE line_id = ? AND up_station_id = ?";
        return jdbcTemplate.update(sql, lineId, upStationId);
    }

    public int deleteByLineIdAndDownStationId(Long lineId, Long downStationId) {
        String sql = "DELETE FROM section WHERE line_id = ? AND down_station_id = ?";
        return jdbcTemplate.update(sql, lineId, downStationId);
    }

    public int delete(Section section) {
        String sql = "DELETE FROM section WHERE id = ?";
        return jdbcTemplate.update(sql, section.getId());
    }

    public List<Section> findAll() {
        String sql = "SELECT s.id AS section_id, "
            + "line.id AS line_id, line.name AS line_name, line.color AS line_color, "
            + "up_station.id AS up_id, "
            + "up_station.name AS up_name, "
            + "down_station.id AS down_id, "
            + "down_station.name AS down_name, "
            + "distance "
            + "FROM section AS s "
            + "LEFT JOIN line ON s.line_id = line.id "
            + "LEFT JOIN station AS up_station ON s.up_station_id = up_station.id "
            + "LEFT JOIN station AS down_station ON s.down_station_id = down_station.id";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
