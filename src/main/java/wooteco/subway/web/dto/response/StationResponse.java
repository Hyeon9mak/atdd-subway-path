package wooteco.subway.web.dto.response;

import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.StationServiceDto;

public class StationResponse {

    private Long id;
    private String name;

    public StationResponse() {
    }

    public StationResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static StationResponse from(StationServiceDto stationServiceDto) {
        return new StationResponse(stationServiceDto.getId(), stationServiceDto.getName());
    }

    public static StationResponse from(Station station) {
        return new StationResponse(station.getId(), station.getName());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
